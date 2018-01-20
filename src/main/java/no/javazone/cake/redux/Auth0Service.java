package no.javazone.cake.redux;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class Auth0Service {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final JWTVerifier verifier;

    public Auth0Service() throws UnsupportedEncodingException {
	verifier = JWT.require(Algorithm.HMAC256(Configuration.auth0Token()))
		.withIssuer(Configuration.auth0Issuer())
		.build();
    }

    public Optional<DecodedJWT> verify(String token) {
	try {
	    return Optional.of(verifier.verify(token));
	} catch (JWTVerificationException e) {
	    LOG.info("Could not verify token: " + token, e);
	    return Optional.empty();
	}
    }
}
