package com.agendademais.services;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {
    private final AES256TextEncryptor encryptor;

    public CryptoService() {
        encryptor = new AES256TextEncryptor();
        String master = System.getenv("JASYPT_MASTER_KEY");
        if (master == null) {
            master = System.getenv("MASTER_KEY");
        }
        if (master == null) {
            // Leave encryptor unconfigured; decrypt will just return input
            encryptor.setPasswordCharArray(new char[] { '\0' });
        } else {
            encryptor.setPassword(master);
        }
    }

    public String decryptIfNeeded(String cipherOrPlain) {
        if (cipherOrPlain == null)
            return null;
        // Heuristic: encrypted values are like ENC(...) or base64; try decrypt but
        // fallback on error
        try {
            if (cipherOrPlain.startsWith("ENC(") && cipherOrPlain.endsWith(")")) {
                String inner = cipherOrPlain.substring(4, cipherOrPlain.length() - 1);
                return encryptor.decrypt(inner);
            }
            // attempt decrypt anyway
            try {
                return encryptor.decrypt(cipherOrPlain);
            } catch (Exception e) {
                return cipherOrPlain; // assume plain
            }
        } catch (Exception ex) {
            return cipherOrPlain;
        }
    }

    public String encryptIfNeeded(String plainOrCipher) {
        if (plainOrCipher == null)
            return null;
        try {
            if (plainOrCipher.startsWith("ENC(") && plainOrCipher.endsWith(")")) {
                return plainOrCipher; // already encrypted
            }
            // If encryptor was not configured with a real password, encrypt will fail;
            // catch and return input
            try {
                String cipher = encryptor.encrypt(plainOrCipher);
                return "ENC(" + cipher + ")";
            } catch (Exception e) {
                return plainOrCipher;
            }
        } catch (Exception ex) {
            return plainOrCipher;
        }
    }
}
