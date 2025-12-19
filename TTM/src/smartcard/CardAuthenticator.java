package smartcard;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import ui.DBConnect;

public class CardAuthenticator {

    private static final byte INS_AUTH_GET_CARD_ID = (byte) 0x31;
    private static final byte INS_AUTH_CHALLENGE = (byte) 0x32;
    private static final byte CLA = (byte) 0x00;

    private CardChannel channel;

    public CardAuthenticator(CardChannel channel) {
        this.channel = channel;
    }

    /**
     * Authenticate user using Challenge-Response protocol
     * 1. Get CardID
     * 2. Get Public Key from DB
     * 3. Send Challenge
     * 4. Verify Signature
     * 
     * @return The authenticated CardID
     * @throws Exception if authentication fails
     */
    public String authenticateUser() throws Exception {
        System.out.println("\n--- STEP 1: IDENTIFICATION (Get Card ID) ---");
        ResponseAPDU r = channel.transmit(new CommandAPDU(CLA, INS_AUTH_GET_CARD_ID, 0x00, 0x00, 16));

        if (r.getSW() != 0x9000) {
            throw new Exception("Get Card ID failed: " + String.format("0x%04X", r.getSW()));
        }

        String cardId = new String(r.getData()).trim();
        System.out.println(">>> Card ID Claimed: " + cardId);

        // Fetch Public Key from DB
        PublicKey cardPublicKey = getCardPublicKeyFromDB(cardId);
        if (cardPublicKey == null) {
            throw new Exception("Public Key not found for Card ID: " + cardId);
        }

        System.out.println("\n--- STEP 2: AUTHENTICATION (Challenge-Response) ---");
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        System.out.println("Challenge Generated: " + bytesToHex(challenge));

        ResponseAPDU r2 = channel.transmit(new CommandAPDU(CLA, INS_AUTH_CHALLENGE, 0x00, 0x00, challenge));

        if (r2.getSW() != 0x9000) {
            throw new Exception("Challenge command failed: " + String.format("0x%04X", r2.getSW()));
        }

        byte[] signature = r2.getData();
        System.out.println("Signature Received: " + bytesToHex(signature));

        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(cardPublicKey);
        sig.update(challenge);

        if (sig.verify(signature)) {
            System.out.println(">>> AUTHENTICATION SUCCESSFUL!");
            return cardId;
        } else {
            System.out.println(">>> AUTHENTICATION FAILED!");
            throw new Exception("Digital Signature Verification Failed");
        }
    }

    private PublicKey getCardPublicKeyFromDB(String cardId) throws Exception {
        String sql = "SELECT CardPublicKey FROM Cards WHERE CardID = ?";
        try (Connection conn = DBConnect.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] keyBytes = rs.getBytes("CardPublicKey");
                    // Check if blob is empty or null
                    if (keyBytes == null || keyBytes.length == 0) {
                        return null;
                    }

                    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    return kf.generatePublic(spec);
                }
            }
        }
        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
