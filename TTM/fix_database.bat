@echo off
echo ========================================
echo  KHOI PHUC DATABASE BI HONG
echo ========================================
echo.

set DB_PATH=src\database\library.db
set BACKUP_PATH=src\database\library_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%.db
set BACKUP_PATH=%BACKUP_PATH: =0%

echo [1] Backup database cu...
if exist "%DB_PATH%" (
    copy "%DB_PATH%" "%BACKUP_PATH%"
    echo    - Da backup thanh: %BACKUP_PATH%
) else (
    echo    - Khong tim thay database!
)

echo.
echo [2] Xoa database bi hong...
if exist "%DB_PATH%" (
    del "%DB_PATH%"
    echo    - Da xoa database cu
)

if exist "build\classes\database\library.db" (
    del "build\classes\database\library.db"
    echo    - Da xoa database trong build
)

echo.
echo [3] Tao lai database moi...
echo    - Vui long chay lai chuong trinh Java de tao database moi
echo.

echo ========================================
echo  HOAN THANH!
echo ========================================
echo.
echo Database cu da duoc backup tai: %BACKUP_PATH%
echo Vui long chay lai DatabaseInit hoac InsertData de tao database moi.
echo.
pause
