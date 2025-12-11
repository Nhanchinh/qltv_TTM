package smartcard;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.smartcardio.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

/**
 * Manager for uploading/downloading images to/from smart card
 * Based on Main.java reference implementation
 */
public class CardImageManager {
    
    private static final byte CLA = 0x00;
    private static final byte INS_UPLOAD_IMAGE = (byte) 0x16;
    private static final byte INS_GET_IMAGE = (byte) 0x34;
    
    // Image buffer size on card: 20KB
    private static final int DATA_IMAGE_SIZE = 20480;
    
    private CardChannel channel;
    
    public CardImageManager(CardChannel channel) {
        this.channel = channel;
    }
    
    /**
     * Upload image to card (matching Main.java uploadImageFromFile logic)
     * @param imageFile The image file to upload
     * @return true if upload successful
     */
    public boolean uploadImage(File imageFile) throws Exception {
        if (imageFile == null || !imageFile.exists()) {
            throw new Exception("File ảnh không tồn tại!");
        }
        
        long originalSize = imageFile.length();
        System.out.println("[UPLOAD] Original File Size: " + originalSize + " bytes");
        
        byte[] imageData;
        
        // Compress if > 20KB (matching Main.java logic)
        if (originalSize > DATA_IMAGE_SIZE) {
            System.out.println("[UPLOAD] Image > 20KB. Compressing...");
            File tempFile = File.createTempFile("compressed_image", ".jpg");
            try {
                compressImage(imageFile, tempFile, DATA_IMAGE_SIZE);
                imageData = readFileToBytes(tempFile);
                System.out.println("[UPLOAD] Compressed Size: " + imageData.length + " bytes");
            } finally {
                if (tempFile.exists()) tempFile.delete();
            }
        } else {
            System.out.println("[UPLOAD] Image <= 20KB. No compression needed.");
            imageData = readFileToBytes(imageFile);
        }
        
        if (imageData.length > DATA_IMAGE_SIZE) {
            throw new Exception("Không thể nén ảnh xuống dưới 20KB. Kích thước: " + imageData.length);
        }
        
        // Log data being sent
        System.out.println("[UPLOAD] Sending " + imageData.length + " bytes to card...");
        logBytes("[UPLOAD] First 16 bytes", imageData, 0, 16);
        logBytes("[UPLOAD] Last 16 bytes", imageData, imageData.length - 16, 16);
        System.out.println("[UPLOAD] APDU: CLA=0x00, INS=0x16, P1=0x00, P2=0x00, Lc=" + imageData.length);
        
        long startTime = System.currentTimeMillis();
        
        // Send image data directly (matching Main.java - no padding)
        ResponseAPDU r = channel.transmit(new CommandAPDU(CLA, INS_UPLOAD_IMAGE, 0x00, 0x00, imageData));
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[UPLOAD] Response SW: 0x" + Integer.toHexString(r.getSW()) + " (Time: " + duration + "ms)");
        
        if (r.getSW() == 0x9000) {
            System.out.println("[UPLOAD] SUCCESS!");
            return true;
        } else {
            System.out.println("[UPLOAD] FAILED!");
            return false;
        }
    }
    
    /**
     * Download image from card in chunks (matching Main.java getImage logic)
     * @return byte array of image data, or null if failed
     */
    public byte[] downloadImage() throws Exception {
        System.out.println("[DOWNLOAD] Starting chunked download (20KB)...");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int offset = 0;
        int chunkSize = 256;
        
        long startTime = System.currentTimeMillis();
        
        while (offset < DATA_IMAGE_SIZE) {
            // Calculate P1, P2 from offset (matching Main.java)
            int p1 = (offset >> 8) & 0xFF;
            int p2 = offset & 0xFF;
            int remain = DATA_IMAGE_SIZE - offset;
            int toRead = Math.min(remain, chunkSize);
            
            ResponseAPDU r = channel.transmit(new CommandAPDU(CLA, INS_GET_IMAGE, p1, p2, toRead));
            
            if (r.getSW() != 0x9000) {
                System.out.println("[DOWNLOAD] Failed at offset " + offset + ". SW: 0x" + Integer.toHexString(r.getSW()));
                return null;
            }
            
            byte[] chunk = r.getData();
            if (chunk == null || chunk.length == 0) break;
            
            baos.write(chunk);
            offset += chunk.length;
            
            // Progress log every 5KB
            if (offset % 5120 == 0) {
                System.out.println("[DOWNLOAD] Progress: " + offset + "/" + DATA_IMAGE_SIZE + " bytes");
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        byte[] data = baos.toByteArray();
        
        System.out.println("[DOWNLOAD] Received " + data.length + " bytes in " + duration + "ms");
        logBytes("[DOWNLOAD] First 16 bytes", data, 0, 16);
        logBytes("[DOWNLOAD] Last 16 bytes", data, data.length - 16, 16);
        
        // Validate JPEG
        if (data.length >= 2) {
            if (isValidJpeg(data)) {
                System.out.println("[DOWNLOAD] Valid JPEG detected!");
                // Trim to actual JPEG size (find FFD9 end marker)
                int actualSize = findJpegEnd(data);
                if (actualSize > 0 && actualSize < data.length) {
                    byte[] trimmed = new byte[actualSize];
                    System.arraycopy(data, 0, trimmed, 0, actualSize);
                    System.out.println("[DOWNLOAD] Trimmed to actual size: " + actualSize + " bytes");
                    return trimmed;
                }
                return data;
            } else {
                System.out.println("[DOWNLOAD] Data is NOT a valid JPEG (first bytes: " + 
                    String.format("%02X %02X", data[0], data[1]) + ")");
                // Return raw data anyway for debugging
                return data;
            }
        }
        
        return data;
    }
    
    /**
     * Check if data starts with JPEG header (FF D8)
     */
    public boolean isValidJpeg(byte[] data) {
        if (data == null || data.length < 2) return false;
        return (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8;
    }
    
    /**
     * Find JPEG end marker (FF D9) and return position after it
     */
    private int findJpegEnd(byte[] data) {
        for (int i = data.length - 2; i >= 0; i--) {
            if ((data[i] & 0xFF) == 0xFF && (data[i + 1] & 0xFF) == 0xD9) {
                return i + 2;
            }
        }
        return data.length;
    }
    
    /**
     * Log bytes in hex format
     */
    private void logBytes(String prefix, byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder(prefix + ": ");
        int end = Math.min(offset + length, data.length);
        int start = Math.max(0, offset);
        for (int i = start; i < end; i++) {
            sb.append(String.format("%02X ", data[i]));
        }
        System.out.println(sb.toString());
    }
    
    /**
     * Read file to byte array
     */
    private byte[] readFileToBytes(File file) throws IOException {
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }
        return data;
    }
    
    /**
     * Compress image to target size (matching Main.java compressImage)
     */
    public void compressImage(File inputFile, File outputFile, long targetSize) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Cannot read image file: " + inputFile.getName());
        }
        
        // Resize if too large
        int maxDim = 400;
        int w = image.getWidth();
        int h = image.getHeight();
        
        if (w > maxDim || h > maxDim) {
            double scale = Math.min((double) maxDim / w, (double) maxDim / h);
            int newW = (int) (w * scale);
            int newH = (int) (h * scale);
            
            BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, newW, newH, null);
            g.dispose();
            image = resized;
            
            System.out.println("[COMPRESS] Resized from " + w + "x" + h + " to " + newW + "x" + newH);
        }
        
        // Compress with decreasing quality until under target size
        float quality = 0.9f;
        while (quality > 0.1f) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IOException("No JPEG writer found");
            }
            
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            }
            writer.dispose();
            
            byte[] compressed = baos.toByteArray();
            
            if (compressed.length <= targetSize) {
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(compressed);
                }
                System.out.println("[COMPRESS] Quality " + (int)(quality * 100) + "% -> " + compressed.length + " bytes");
                return;
            }
            
            quality -= 0.1f;
        }
        
        throw new IOException("Cannot compress image below " + targetSize + " bytes");
    }
    
    /**
     * Get the image buffer size constant
     */
    public static int getImageBufferSize() {
        return DATA_IMAGE_SIZE;
    }
}
