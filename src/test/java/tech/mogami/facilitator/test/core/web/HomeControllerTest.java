package tech.mogami.facilitator.test.core.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tech.mogami.facilitator.test.core.util.web.BaseWebTest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static tech.mogami.commons.web.GlobalModelAttributes.QUERY_ATTRIBUTE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_PAGE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_URL;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Home controller tests")
public class HomeControllerTest extends BaseWebTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Display home page without query parameter")
    public void homePageWithoutQueryParameter() throws Exception {

        mockMvc.perform(get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(HOME_PAGE.view())))
                // Checking that we have a link to the API documentation.
                .andExpect(content().string(containsString("/swagger-ui/")))
                // Checking the search form is filled without value coming from parameter.
                .andExpect(content().string(containsString("value=\"\"")))
                // Checking that the supported networks are displayed.
                // TODO Improve the display of supported kinds.
                .andExpect(content().string(containsString("x402:V2/eip155:84532/exact")))
                .andExpect(content().string(containsString("x402:V2/eip155:8453/exact")))
                // Checking that the facilitator address is displayed.
                .andExpect(content().string(containsString("0xb02166b97d37551cb8154c657d4c01b835404fc4")));

    }

    @ParameterizedTest
    @MethodSource("headers")
    @DisplayName("Display home page with query parameter")
    void homePageWithQueryParameter(final HttpHeaders headers) throws Exception {

        mockMvc.perform(get(HOME_URL).headers(headers).queryParam(QUERY_ATTRIBUTE, "MY_QUERY"))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(HOME_PAGE.view())))
                // Checking the search form is filled with the parameter.
                .andExpect(content().string(containsString("value=\"MY_QUERY\"")));

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/logo/mogami-x402-facilitator-logo-32x32.png"
    })
    @DisplayName("Home images are available")
    void homeImagesAvailable(String imagePath) throws Exception {
        mockMvc.perform(get("/images/" + imagePath)).andExpect(status().isOk());
    }

}
