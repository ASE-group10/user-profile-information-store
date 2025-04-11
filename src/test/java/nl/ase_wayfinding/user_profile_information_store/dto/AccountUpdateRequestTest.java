package nl.ase_wayfinding.user_profile_information_store.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class AccountUpdateRequestTest {

    @Test
    void testGettersAndSetters() {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setName("Alice");
        request.setPassword("secret");
        request.setPhoneNumber("1234567890");
        request.setPicture("http://example.com/picture.jpg");

        assertEquals("Alice", request.getName());
        assertEquals("secret", request.getPassword());
        assertEquals("1234567890", request.getPhoneNumber());
        assertEquals("http://example.com/picture.jpg", request.getPicture());
    }
}
