package tech.mogami.facilitator.test.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.FlashMap;
import tech.mogami.facilitator.test.util.web.BaseWebTest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static tech.mogami.commons.web.GlobalModelAttributes.QUERY_ATTRIBUTE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_PAGE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_URL;
import static tech.mogami.facilitator.web.www.pages.HomePage.SEARCH_URL;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_URL;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Search controller tests")
public class SearchControllerTest extends BaseWebTest {

    @Autowired
    MessageSource messages;

    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @MethodSource("headers")
    @DisplayName("Search with existing result")
    void searchWithExistingResult(final HttpHeaders headers) throws Exception {

        mockMvc.perform(get(SEARCH_URL).headers(headers).queryParam(QUERY_ATTRIBUTE, "nonce001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PAYMENT_BY_NONCE_URL.replace("{nonce}", "nonce001")))
                .andReturn();

    }

    @ParameterizedTest
    @MethodSource("headers")
    @DisplayName("Search with query parameter and no result")
    void searchWithQueryParameterAndNoResult(final HttpHeaders headers) throws Exception {

        MvcResult result = mockMvc.perform(get(SEARCH_URL).headers(headers)
                        .queryParam(QUERY_ATTRIBUTE, "NON_EXISTING_NONCE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(HOME_URL))
                .andReturn();

        // Extract the flash attributes and session from the result
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        FlashMap flashMap = result.getFlashMap();
        assertNotNull(session);
        mockMvc.perform(get(HOME_URL).session(session).flashAttrs(flashMap))
                .andExpect(status().isOk())
                .andExpect(view().name(HOME_PAGE.view()))
                .andExpect(content().string(containsString("No payment found for &quot;NON_EXISTING_NONCE&quot;")))
                .andExpect(content().string(not(containsString(getMessage(messages, "search.error.noQuery")))));

    }

    @ParameterizedTest
    @MethodSource("headers")
    @DisplayName("Search without query parameter")
    void searchWithoutQueryParameter(final HttpHeaders headers) throws Exception {

        MvcResult result = mockMvc.perform(get(SEARCH_URL).headers(headers))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(HOME_URL))
                .andReturn();

        // Extract the flash attributes and session from the result
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        FlashMap flashMap = result.getFlashMap();
        assertNotNull(session);
        mockMvc.perform(get(HOME_URL).session(session).flashAttrs(flashMap))
                .andExpect(status().isOk())
                .andExpect(view().name(HOME_PAGE.view()))
                .andExpect(content().string(containsString(getMessage(messages, "search.error.noQuery"))))
                .andExpect(content().string(not(containsString(getMessage(messages, "search.error.noResult")))));

    }

}
