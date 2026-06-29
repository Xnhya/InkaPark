package com.example.Inkapark.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;

@Converter
public class SimpleEncryptor implements AttributeConverter<String, String> {

    private static final String SECRET = "MySecretKey12345";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16; 

    @Override
    public String convertToDatabaseColumn(String atributoPlano) {
        if (atributoPlano == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] enc = cipher.doFinal(atributoPlano.getBytes());
            byte[] combined = new byte[GCM_IV_LENGTH + enc.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(enc, 0, combined, GCM_IV_LENGTH, enc.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String columna) {
        if (columna == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), "AES");
            byte[] combined = Base64.getDecoder().decode(columna);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] enc = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, enc, 0, enc.length);
            SecretKeySpec key2 = new SecretKeySpec(SECRET.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key2, spec);
            return new String(cipher.doFinal(enc));
        } catch (Exception e) {
            throw new RuntimeException("Error descifrando", e);
        }
    }
}
