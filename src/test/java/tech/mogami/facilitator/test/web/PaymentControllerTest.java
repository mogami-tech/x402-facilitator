package tech.mogami.facilitator.test.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tech.mogami.facilitator.test.util.web.BaseWebTest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_PAGE;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_URL;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Payment controller tests")
public class PaymentControllerTest extends BaseWebTest {

    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @MethodSource("headers")
    @DisplayName("Simple payment without step")
    public void simplePaymentWithoutStep(final HttpHeaders headers) throws Exception {

        MvcResult result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", "nonce001")).headers(headers))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
                .andReturn();
        Document page = Jsoup.parse(result.getResponse().getContentAsString());

        // Payment header.
        assertElementValue(page, "payment-nonce", "nonce001");
        assertElementValue(page, "payment-status", "Pending");
        assertElementValue(page, "payment-network-name", "-");
        assertElementValue(page, "payment-from-address", "-");
        assertElementValue(page, "payment-to-address", "-");
        assertElementValue(page, "payment-amount", "-");
    }

}
