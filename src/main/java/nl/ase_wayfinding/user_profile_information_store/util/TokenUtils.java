package nl.ase_wayfinding.user_profile_information_store.util;

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
            System.out.println("Token after removing Bearer: " + token);

            // Normalize the auth0Domain (remove trailing slash if it exists)
            String normalizedDomain = auth0Domain.endsWith("/")
                    ? auth0Domain.substring(0, auth0Domain.length() - 1)
                    : auth0Domain;
            System.out.println("Normalized Auth0 Domain: " + normalizedDomain);

            // Create a JWK Provider to fetch the public keys
            URL jwksUrl = new URL(normalizedDomain + "/.well-known/jwks.json");
            System.out.println("Using JWKS URL: " + jwksUrl);
            JwkProvider provider = new UrlJwkProvider(jwksUrl);

            // Decode the token to get the Key ID (kid)
            DecodedJWT jwt = JWT.decode(token);
            System.out.println("Decoded JWT: " + jwt);
            String kid = jwt.getKeyId();
            System.out.println("Key ID (kid): " + kid);

            // Fetch the public key using the kid
            RSAPublicKey publicKey = (RSAPublicKey) provider.get(kid).getPublicKey();
            System.out.println("Fetched public key: " + publicKey);

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);

            // Build the JWT verifier. Adjust issuer if necessary.
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(normalizedDomain + "/") // or .withIssuer(normalizedDomain) if issuer doesn't include a trailing slash
                    .build();

            // Verify the token
            DecodedJWT verifiedJwt = verifier.verify(token);
            System.out.println("Verified JWT: " + verifiedJwt);

            // Extract the Auth0 User ID (sub claim)
            String subject = verifiedJwt.getSubject();
            System.out.println("Extracted subject (Auth0 User ID): " + subject);
            return subject;
        } catch (Exception e) {
            System.err.println("Error extracting Auth0 user id from token:");
            e.printStackTrace();
            return null;
        }
    }
}
