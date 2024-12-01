package com.example.user_profile_information_store.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.algorithms.Algorithm;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

public class TokenUtils {

    public static String extractAuth0UserIdFromToken(String token, String auth0Domain) {
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Create a JWK Provider to fetch the public keys
            JwkProvider provider = new UrlJwkProvider(new URL(auth0Domain + "/.well-known/jwks.json"));

            // Decode the token to get the Key ID (kid)
            DecodedJWT jwt = JWT.decode(token);
            String kid = jwt.getKeyId();

            // Fetch the public key using the kid
            RSAPublicKey publicKey = (RSAPublicKey) provider.get(kid).getPublicKey();
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);

            // Build the JWT verifier
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(auth0Domain + "/")
                    .build();

            // Verify the token
            DecodedJWT verifiedJwt = verifier.verify(token);

            // Extract the Auth0 User ID (sub claim)
            return verifiedJwt.getSubject();
        } catch (Exception e) {
            // Invalid token
            e.printStackTrace();
            return null;
        }
    }
}
