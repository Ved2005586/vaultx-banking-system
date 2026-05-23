package com.securebank.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@Component
@Slf4j
public class EncryptionUtil {

    private static final String AES = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_LENGTH = 128;

    @Value("${app.encryption.aes-key}")
    private String aesKey;

    private SecretKey getKey() {
        byte[] key = aesKey.getBytes(StandardCharsets.UTF_8);
        byte[] finalKey = new byte[16];
        System.arraycopy(key, 0, finalKey, 0, Math.min(key.length, 16));
        return new SecretKeySpec(finalKey, "AES");
    }

    public String encryptAES(String data) {
        try {
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(),
                    new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    public String decryptAES(String data) {
        try {
            byte[] decoded = Base64.getDecoder().decode(data);

            byte[] iv = new byte[IV_SIZE];
            byte[] cipher = new byte[decoded.length - IV_SIZE];

            System.arraycopy(decoded, 0, iv, 0, IV_SIZE);
            System.arraycopy(decoded, IV_SIZE, cipher, 0, cipher.length);

            Cipher c = Cipher.getInstance(AES);
            c.init(Cipher.DECRYPT_MODE, getKey(),
                    new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(c.doFinal(cipher), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    // RSA helpers
    public KeyPair generateRSA() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }
}