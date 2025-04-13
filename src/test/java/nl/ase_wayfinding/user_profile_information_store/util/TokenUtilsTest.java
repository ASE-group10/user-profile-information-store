package nl.ase_wayfinding.user_profile_information_store.util;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenUtilsTest {

    @Test
    void testExtractAuth0UserId_Success() throws Exception {
        // Prepare test data
        String token = "Bearer dummy.token.value";
        // Our method will strip the "Bearer " prefix, so we expect to work with:
        String tokenWithoutBearer = "dummy.token.value";
        String auth0Domain = "https://test-auth0.com/";

        // Create a fake decoded JWT for the JWT.decode(tokenWithoutBearer) call.
        DecodedJWT fakeDecodedJWT = mock(DecodedJWT.class);
        when(fakeDecodedJWT.getKeyId()).thenReturn("kid123");

        // Create a fake decoded JWT for the verified token.
        DecodedJWT fakeVerifiedJWT = mock(DecodedJWT.class);
        when(fakeVerifiedJWT.getSubject()).thenReturn("auth0|user123");

        // Use static mocking for the JWT class.
        try (MockedStatic<JWT> jwtMock = Mockito.mockStatic(JWT.class)) {
            // When JWT.decode is called, return our fake decoded JWT.
            jwtMock.when(() -> JWT.decode(tokenWithoutBearer)).thenReturn(fakeDecodedJWT);

            // Stub JWT.require(...) to simulate building a JWTVerifier.
            JWTVerifier.BaseVerification baseVerification = mock(JWTVerifier.BaseVerification.class);
            JWTVerifier verifierMock = mock(JWTVerifier.class);
            jwtMock.when(() -> JWT.require(any(Algorithm.class))).thenReturn(baseVerification);
            when(baseVerification.withIssuer(anyString())).thenReturn(baseVerification);
            when(baseVerification.build()).thenReturn(verifierMock);
            when(verifierMock.verify(tokenWithoutBearer)).thenReturn(fakeVerifiedJWT);

            // Use construction mocking so that the call to new UrlJwkProvider(...) returns our fake provider.
            try (MockedConstruction<UrlJwkProvider> mockedConstruction =
                         Mockito.mockConstruction(UrlJwkProvider.class, (mock, context) -> {
                             // Create a fake Jwk and fake public key.
                             Jwk fakeJwk = mock(Jwk.class);
                             RSAPublicKey fakePublicKey = mock(RSAPublicKey.class);
                             when(fakeJwk.getPublicKey()).thenReturn(fakePublicKey);
                             // When get(String kid) is called on the provider, return the fake Jwk.
                             when(mock.get("kid123")).thenReturn(fakeJwk);
                         })) {
                // Call the method under test.
                String result = TokenUtils.extractAuth0UserIdFromToken(token, auth0Domain);
                // Verify that the subject (Auth0 user id) is what we expect.
                assertEquals("auth0|user123", result);
            }
        }
    }

    @Test
    void testExtractAuth0UserId_TokenDecodingFailure() {
        // Prepare inputs which will force a decoding error.
        String token = "invalidToken";
        String auth0Domain = "https://test-auth0.com/";

        // Use static mocking so that JWT.decode(token) throws an exception.
        try (MockedStatic<JWT> jwtMock = Mockito.mockStatic(JWT.class)) {
            jwtMock.when(() -> JWT.decode(token)).thenThrow(new RuntimeException("Decoding failed"));
            // The utility should catch the exception and return null.
            String result = TokenUtils.extractAuth0UserIdFromToken(token, auth0Domain);
            assertNull(result);
        }
    }
}
