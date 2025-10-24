package tech.mogami.facilitator.test.html;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tech.mogami.facilitator.test.util.web.BaseWebTest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static tech.mogami.facilitator.web.html.pages.HomePage.HOME;
import static tech.mogami.facilitator.web.html.pages.HomePage.HOME_URL;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Home controller tests")
public class HomeTest extends BaseWebTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("Display home page without query parameter and without htmx")
    public void homePageWithoutQueryParameter() throws Exception {

        mockMvc.perform(get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(HOME.view())))
                // Checking that we have a link to the API documentation.
                .andExpect(content().string(containsString("/swagger-ui/")))
                // Checking the search form is filled without value coming from parameter.
                .andExpect(content().string(containsString("value=\"\"")))
                // Checking that the supported networks are displayed.
                .andExpect(content().string(containsString("base-sepolia / exact")))
                .andExpect(content().string(containsString("base / exact")))
                // Checking that the facilitator address is displayed.
                .andExpect(content().string(containsString("0xb02166b97d37551cb8154c657d4c01b835404fc4")));

    }

}
