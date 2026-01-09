package ELEC5619_Practical2_Group_5.bookstore.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleResponseStatusException_ReturnsBodyAndStatus_ForBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");

        ResponseEntity<Map<String, Object>> resp = handler.handleResponseStatusException(ex);

        assertEquals(400, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(400, resp.getBody().get("status"));
        assertEquals("Invalid payload", resp.getBody().get("error"));
    }

    @Test
    void handleResponseStatusException_AllowsNullReason() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT);

        ResponseEntity<Map<String, Object>> resp = handler.handleResponseStatusException(ex);

        assertEquals(409, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals(409, resp.getBody().get("status"));
        assertTrue(resp.getBody().containsKey("error"));
        assertNull(resp.getBody().get("error"));
    }

    @Test
    void advice_IsAppliedToControllers_AndBuildsExpectedJsonBody() throws Exception {
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new BoomController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mvc.perform(MockMvcRequestBuilders.get("/boom"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not here"));
    }

    @RestController
    static class BoomController {
        @GetMapping(value = "/boom", produces = MediaType.APPLICATION_JSON_VALUE)
        public String boom() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not here");
        }
    }
}
