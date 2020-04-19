@echo off

@set JAVA_ARGS=-Xms500m -Xmx1g
@set CAS_DIR=%cd%
@rem Call this script with DNAME and CERT_SUBJ_ALT_NAMES already set to override
@if "%DNAME%" == "" set DNAME=CN=cas.example.org,OU=Example,OU=Org,C=US
@rem List other host names or ip addresses you want in your certificate, may help with host name verification,
@rem   if client apps make https connection for ticket validation and compare name in cert (include sub. alt. names)
@rem   to name used to access CAS
@if "%CERT_SUBJ_ALT_NAMES%" == "" set CERT_SUBJ_ALT_NAMES=dns:example.org,dns:localhost,dns:%COMPUTERNAME%,ip:127.0.0.1


@if "%1" == "gencert" call:gencert


@rem function section starts here
@goto:eof
:gencert
    where /q keytool
    if ERRORLEVEL 1 (
        @echo Java keytool.exe not found in path.
        exit /b 1
    ) else (
        if not exist %CAS_DIR% mkdir %CAS_DIR%
        @echo on
        @echo Generating self-signed SSL cert for %DNAME% in %CAS_DIR%\thekeystore
        keytool -genkeypair -alias cas -keyalg RSA -keypass changeit -storepass changeit -keystore %CAS_DIR%\thekeystore -dname %DNAME% -ext SAN=%CERT_SUBJ_ALT_NAMES%
        @echo Exporting cert for use in trust store (used by cas clients)
        keytool -exportcert -alias cas -storepass changeit -keystore %CAS_DIR%\thekeystore -file %CAS_DIR%\cas.cer
    )
@goto:eof