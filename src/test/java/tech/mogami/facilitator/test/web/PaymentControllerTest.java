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

    @ParameterizedTest
    @MethodSource("headers")
    @DisplayName("Complete payment with steps")
    public void completePaymentWithSteps(final HttpHeaders headers) throws Exception {
        MvcResult result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", "nonce002")).headers(headers))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
                .andReturn();
        Document page = Jsoup.parse(result.getResponse().getContentAsString());

        // Payment header.
        assertElementValue(page, "payment-nonce", "nonce002");
        assertElementValue(page, "payment-status", "Completed");
        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
        assertElementValue(page, "payment-from-address", "0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73");
        assertElementValue(page, "payment-to-address", "0x7553F6FA4Fb62986b64f79aEFa1fB93ea64A22b1");
        assertElementValue(page, "payment-amount", "0,01 USDC");

        // First step (error).
        assertElementExists(page, "payment-step-0");
        assertElementValue(page, "payment-step-0-type", "VERIFY");
        assertElementValue(page, "payment-step-0-status", "Failed");
        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature is empty)");

        // Second step (successful).
        assertElementExists(page, "payment-step-1");
        assertElementValue(page, "payment-step-1-type", "VERIFY");
        assertElementValue(page, "payment-step-1-status", "Succeed");
        assertElementNotExists(page, "payment-step-1-error-code");
        assertElementNotExists(page, "payment-step-1-error-message");

        // Third step (error).
        assertElementExists(page, "payment-step-2");
        assertElementValue(page, "payment-step-2-type", "SETTLE");
        assertElementValue(page, "payment-step-2-status", "Failed");
        assertElementValue(page, "payment-step-2-error", "Error: unexpected_settle_error (Details: Node unreachable)");

        // Fourth step (successful).
        assertElementExists(page, "payment-step-3");
        assertElementValue(page, "payment-step-3-type", "SETTLE");
        assertElementValue(page, "payment-step-3-status", "Succeeded");
        assertElementNotExists(page, "payment-step-3-error-code");
        assertElementNotExists(page, "payment-step-3-error-message");

        assertElementNotExists(page, "payment-step-4");
    }

}
