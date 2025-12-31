package tech.mogami.facilitator.test.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SUPPORTED_ENDPOINT;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("/supported tests")
public class SupportedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Calling /supported")
    void supported() throws Exception {
        mockMvc.perform(get(SUPPORTED_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))

                // kinds ===============================================================================================
                .andExpect(jsonPath("$.kinds").isArray())
                .andExpect(jsonPath("$.kinds", hasSize(2)))

                .andExpect(jsonPath("$.kinds[0].x402Version").value("2"))
                .andExpect(jsonPath("$.kinds[0].scheme").value("exact"))
                .andExpect(jsonPath("$.kinds[0].network").value("eip155:84532"))

                .andExpect(jsonPath("$.kinds[1].x402Version").value("2"))
                .andExpect(jsonPath("$.kinds[1].scheme").value("exact"))
                .andExpect(jsonPath("$.kinds[1].network").value("eip155:8453"))

                // extensions =============================================================================
                .andExpect(jsonPath("$.extensions").isArray())
                .andExpect(jsonPath("$.extensions", hasSize(0)))

                // signers =============================================================================
                .andExpect(jsonPath("$.signers").isMap())

                .andExpect(jsonPath("$.signers['eip155:*']").isArray())
                .andExpect(jsonPath("$.signers['eip155:*']", hasSize(1)))
                .andExpect(jsonPath("$.signers['eip155:*'][0]").value("0xb02166b97d37551cb8154c657d4c01b835404fc4"));
    }

}
