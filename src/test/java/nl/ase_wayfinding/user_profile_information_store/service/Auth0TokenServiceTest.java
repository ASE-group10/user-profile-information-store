package nl.ase_wayfinding.user_profile_information_store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Auth0TokenServiceTest {

    private Auth0TokenService tokenService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenService = new Auth0TokenService();

        // Inject mocked RestTemplate
        ReflectionTestUtils.setField(tokenService, "restTemplate", restTemplate);

        // Inject @Value fields
        ReflectionTestUtils.setField(tokenService, "auth0Domain", "https://test-auth0.com/");
        ReflectionTestUtils.setField(tokenService, "clientId", "testClientId");
        ReflectionTestUtils.setField(tokenService, "clientSecret", "testClientSecret");
        ReflectionTestUtils.setField(tokenService, "audience", "https://test-auth0.com/api/v2/");
    }

    @Test
    void testFetchesNewTokenWhenNoneCached() {
        Map<String, Object> responseBody = Map.of(
                "access_token", "mocked_token",
                "expires_in", 3600
        );
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(response);

        String token = tokenService.getManagementApiToken();

        assertEquals("mocked_token", token);
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testReturnsCachedTokenIfNotExpired() {
        ReflectionTestUtils.setField(tokenService, "managementApiToken", "cached_token");
        ReflectionTestUtils.setField(tokenService, "tokenExpirationTime", System.currentTimeMillis() + 10_000);

        String token = tokenService.getManagementApiToken();

        assertEquals("cached_token", token);
        verify(restTemplate, never()).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void testThrowsExceptionIfTokenFetchFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Service down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> tokenService.getManagementApiToken());
        assertTrue(ex.getMessage().contains("Failed to fetch management API token"));
    }
}
