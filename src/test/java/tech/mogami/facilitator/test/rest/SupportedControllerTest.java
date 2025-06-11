package tech.mogami.facilitator.test.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.mogami.facilitator.web.rest.SupportedController;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.SUPPORTED_URL;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("/supported tests")
public class SupportedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private SupportedController supportedService;

    @Test
    @DisplayName("/supported test")
    void supportedTest() throws Exception {
        verify(supportedService, times(0)).supported();

        // First call without cache.
        mockMvc.perform(get(SUPPORTED_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.kinds").isArray())
                .andExpect(jsonPath("$.kinds", hasSize(1)))
                // Checking base networks ==============================================================================
                .andExpect(jsonPath("$.kinds[0].x402Version").value("1"))
                .andExpect(jsonPath("$.kinds[0].scheme").value("exact"))
                .andExpect(jsonPath("$.kinds[0].network").value("base-sepolia"));
        verify(supportedService, times(1)).supported();

        // Second call with cache (REST service should only have been called once).
        mockMvc.perform(get(SUPPORTED_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.kinds").isArray())
                .andExpect(jsonPath("$.kinds", hasSize(1)));
        verify(supportedService, times(1)).supported();
    }

}
