package nl.ase_wayfinding.user_profile_information_store.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(CustomErrorController.class)
public class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHandleError() throws Exception {
        // Simulate error mapping by hitting /error endpoint.
        mockMvc.perform(get("/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").exists());
    }
}
