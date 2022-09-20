package com.telsoft.monitor.manager.util;

import java.io.*;
import java.security.*;
import java.util.Base64;
import javax.crypto.*;

public final class PasswordLoader {
    public static void makeKey(String keyFile) throws Exception {
        // Security.addProvider( new com.sun.crypto.provider.SunJCE() );
        KeyGenerator generator = KeyGenerator.getInstance("DES", "SunJCE");

        // generate a new random key
        generator.init(56, new SecureRandom());

        Key key = generator.generateKey();

        ByteArrayOutputStream keyStore = new ByteArrayOutputStream();
        ObjectOutputStream keyObjectStream = new ObjectOutputStream(keyStore);
        keyObjectStream.writeObject(key);

        byte[] keyBytes = keyStore.toByteArray();

        FileOutputStream fos = new FileOutputStream(keyFile);
        fos.write(keyBytes);
        fos.flush();
        fos.close();
    }

    public static String getEncryptedString(String input, String keyFile) throws Exception {
        // Security.addProvider( new com.sun.crypto.provider.SunJCE() );
        Key key = null;

        File f = new File(keyFile);
        FileInputStream fis = new FileInputStream(f);
        byte[] keyBytes = new byte[(int) f.length()];
        fis.read(keyBytes);
        fis.close();

        ByteArrayInputStream keyArrayStream = new ByteArrayInputStream(keyBytes);
        ObjectInputStream keyObjectStream = new ObjectInputStream(keyArrayStream);
        key = (Key) keyObjectStream.readObject();

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] inputBytes = input.getBytes();

        byte[] outputBytes = cipher.doFinal(inputBytes);
        String base64 = Base64.getEncoder().encodeToString(outputBytes);

        return base64;
    }

    public static String getDecryptedString(String base64Input, String keyFile) throws Exception {
        byte[] inputBytes = Base64.getDecoder().decode(base64Input);

        // Security.addProvider( new com.sun.crypto.provider.SunJCE() );
        Key key = null;

        File f = new File(keyFile);
        FileInputStream fis = new FileInputStream(f);
        byte[] keyBytes = new byte[(int) f.length()];
        fis.read(keyBytes);
        fis.close();

        ByteArrayInputStream keyArrayStream = new ByteArrayInputStream(keyBytes);
        ObjectInputStream keyObjectStream = new ObjectInputStream(keyArrayStream);
        key = (Key) keyObjectStream.readObject();

        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] outputBytes = cipher.doFinal(inputBytes);

        return new String(outputBytes);
    }
}
