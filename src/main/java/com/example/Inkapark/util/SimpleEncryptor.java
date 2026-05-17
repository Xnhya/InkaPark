package com.example.Inkapark.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class SimpleEncryptor implements AttributeConverter<String, String> {

    private static final String SECRET = "MySecretKey12345"; 

    @Override
    public String convertToDatabaseColumn(String atributoPlano) {
        if (atributoPlano == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] enc = cipher.doFinal(atributoPlano.getBytes());
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new RuntimeException("Error cifrando", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String columna) {
        if (columna == null) return null;
        try {
            SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dec = cipher.doFinal(Base64.getDecoder().decode(columna));
            return new String(dec);
        } catch (Exception e) {
            throw new RuntimeException("Error descifrando", e);
        }
    }
}
