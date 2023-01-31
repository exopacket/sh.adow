package com.inteliense.shadow.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {

    public static String getSha1(String input)
    {
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-1");

            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger num = new BigInteger(1, messageDigest);
            String hashtext = num.toString(16);

            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }

            return hashtext;
        }

        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";

    }

}