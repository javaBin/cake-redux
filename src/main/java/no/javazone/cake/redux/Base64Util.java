package no.javazone.cake.redux;


import org.apache.commons.codec.binary.Base64;

public class Base64Util {
    public static String encode(String text) {
        return Base64.encodeBase64String(text.getBytes());
    }

    public static String decode(String encText) {
        return new String(Base64.decodeBase64(encText));
    }

}
