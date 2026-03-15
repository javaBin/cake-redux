package no.javazone.cake.redux;

import org.jsonbuddy.JsonFactory;
import org.jsonbuddy.JsonObject;
import org.jsonbuddy.parse.JsonParser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

public class JwtUtil {

    private static final String HEADER = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    public static String createToken(String username) {
        String payloadJson = JsonFactory.jsonObject().put("sub", username).toJson();
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = HEADER + "." + payload;
        return signingInput + "." + sign(signingInput);
    }

    public static Cookie createCookie(String username) {
        Cookie cookie = new Cookie("auth_token", createToken(username));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public static Optional<String> getUsernameFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if ("auth_token".equals(cookie.getName())) {
                return verifyAndExtract(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private static Optional<String> verifyAndExtract(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        String signingInput = parts[0] + "." + parts[1];
        String expectedSig = sign(signingInput);
        if (!MessageDigest.isEqual(
                expectedSig.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonObject payload = JsonParser.parseToObject(new String(payloadBytes, StandardCharsets.UTF_8));
            return payload.stringValue("sub");
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = Configuration.applicationSecret().getBytes(StandardCharsets.UTF_8);
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            byte[] signature = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }
}
