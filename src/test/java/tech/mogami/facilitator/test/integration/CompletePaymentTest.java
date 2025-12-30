package tech.mogami.facilitator.test.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.PaymentPayload;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.commons.util.NonceUtil;
import tech.mogami.facilitator.provider.outbox.repository.OutboxEventRepository;
import tech.mogami.facilitator.test.util.web.BaseWebTest;

import java.util.Locale;

import static org.assertj.core.api.Fail.fail;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_ENDPOINT;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_SERVER_WALLET_ADDRESS_1;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_PAGE;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_URL;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Complete payment tests")
public class CompletePaymentTest extends BaseWebTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void forceLocale() {
        Locale.setDefault(Locale.FRANCE);
    }

    @Test
    @DisplayName("Complete payment integration test")
    void completePaymentIntegrationTest() throws Exception {
        var nonce = NonceUtil.generateNonce();
        System.out.println("Using nonce: " + nonce);

        // Calling /verify with an error ===============================================================================
        // Data.
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .amount("10000")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        var paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                .scheme(EXACT_SCHEME.name())
//                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .signature("0x7d9463e2c7c98e33c08747882521be88cc02443a8c46f3a1f5b51ae8d1bdd9581fa41ab35c1cebfe70a79471640a1bde9ffadd377e38d708b5ca6a38b30300f61b")
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("20000")
                                .validAfter("1748534647")
                                .validBefore("1748534767")
                                .nonce(nonce)
                                .build())
                        .build())
                .build();

        // Calling the service.
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(JsonUtil.toJson(VerificationRequest.builder()
//                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .paymentPayload(paymentPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.invalidReason").value("invalid_exact_evm_payload_signature"))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));

        // Checking the payment page.
        await().until(this::allEventsAreTreated);
        MvcResult result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
                .andReturn();
        Document page = Jsoup.parse(result.getResponse().getContentAsString());

        // Payment header.
        assertElementValue(page, "payment-nonce", nonce);
        assertElementValue(page, "payment-status", "Pending");
        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-amount", "0,02 USDC");
        assertElementValue(page, "payment-version", "1");

        // First step (error).
        assertElementExists(page, "payment-step-0");
        assertElementValue(page, "payment-step-0-type", "VERIFY");
        assertElementValue(page, "payment-step-0-status", "Failed");
        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");
        assertElementNotExists(page, "payment-step-1");

        // Calling /verify without error ===============================================================================
        var now = System.currentTimeMillis() / 1000;
        paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .amount("10000")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                .scheme(EXACT_SCHEME.name())
//                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("20000")
                                .validAfter(String.valueOf(now))
                                .validBefore(String.valueOf(now + 10))
                                .nonce(nonce)
                                .build()
                        ).build()
                ).build();
        fail("Remove this line when fixing the test");
//        var signedPayload = X402PaymentHelper.getSignedPayload(
//                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY),
//                paymentRequirements,
//                paymentPayload);
//
//        // Calling the service.
//        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
//                        .contentType(APPLICATION_JSON)
//                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
//                        .header("User-Agent", "axios/1.8.4")
//                        .header("Accept-Encoding", "identity")
//                        .content(JsonUtil.toJson(VerificationRequest.builder()
//                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                                .paymentPayload(signedPayload)
//                                .paymentRequirements(paymentRequirements)
//                                .build())))
//                .andExpect(status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(APPLICATION_JSON))
//                .andExpect(jsonPath("$.isValid").value(true))
//                .andExpect(jsonPath("$.invalidReason").isEmpty())
//                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));

        // Checking the payment page.
//        await().until(this::allEventsAreTreated);
//        result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
//                .andExpect(status().isOk())
//                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
//                .andReturn();
//        page = Jsoup.parse(result.getResponse().getContentAsString());
//
//        // Payment header.
//        assertElementValue(page, "payment-nonce", nonce);
//        assertElementValue(page, "payment-status", "Pending");
//        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
//        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
//        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
//        assertElementValue(page, "payment-amount", "0,02 USDC");
//        assertElementValue(page, "payment-version", "1");
//
//        // First verify step (error).
//        assertElementExists(page, "payment-step-0");
//        assertElementValue(page, "payment-step-0-type", "VERIFY");
//        assertElementValue(page, "payment-step-0-status", "Failed");
//        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");
//
//        // Second verify step (success).
//        assertElementExists(page, "payment-step-1");
//        assertElementValue(page, "payment-step-1-type", "VERIFY");
//        assertElementValue(page, "payment-step-1-status", "Succeed");
//        assertElementNotExists(page, "payment-step-1-error");
//        assertElementNotExists(page, "payment-step-2");

        // Calling /settle with error (no signature) ===================================================================
//        now = System.currentTimeMillis() / 1000;
//        paymentRequirements = PaymentRequirements.builder()
//                .scheme(EXACT_SCHEME.name())
//                .network(BASE_SEPOLIA.name())
//                .maxAmountRequired("200")
//                .resource("http://localhost/weather")
//                .maxTimeoutSeconds(60)
//                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
//                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
//                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
//                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
//                .build();
//        paymentPayload = PaymentPayload.builder()
//                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                .scheme(EXACT_SCHEME.name())
//                .network(BASE_SEPOLIA.name())
//                .payload(ExactSchemePayload.builder()
//                        .authorization(ExactSchemePayload.Authorization.builder()
//                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
//                                .to(TEST_SERVER_WALLET_ADDRESS_1)
//                                .value("20000")
//                                .validAfter(String.valueOf(now))
//                                .validBefore(String.valueOf(now + 10))
//                                .nonce(nonce)
//                                .build())
//                        .build())
//                .build();
//
//        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
//                        .contentType(APPLICATION_JSON)
//                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
//                        .header("User-Agent", "axios/1.8.4")
//                        .header("Accept-Encoding", "identity")
//                        .content(JsonUtil.toJson(SettleRequest.builder()
//                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                                .paymentPayload(paymentPayload)
//                                .paymentRequirements(paymentRequirements)
//                                .build())))
//                .andExpect(status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(APPLICATION_JSON))
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.name()))
//                .andExpect(jsonPath("$.errorReason").value(INVALID_PAYLOAD.getCode()))
//                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));
//
//        // Checking the payment page.
//        await().until(this::allEventsAreTreated);
//        result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
//                .andExpect(status().isOk())
//                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
//                .andReturn();
//        page = Jsoup.parse(result.getResponse().getContentAsString());
//
//        // Payment header.
//        assertElementValue(page, "payment-nonce", nonce);
//        assertElementValue(page, "payment-status", "Failed");
//        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
//        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
//        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
//        assertElementValue(page, "payment-amount", "0,02 USDC");
//        assertElementValue(page, "payment-version", "1");
//
//        // First verify step (error).
//        assertElementExists(page, "payment-step-0");
//        assertElementValue(page, "payment-step-0-type", "VERIFY");
//        assertElementValue(page, "payment-step-0-status", "Failed");
//        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");
//
//        // Second verify step (success).
//        assertElementExists(page, "payment-step-1");
//        assertElementValue(page, "payment-step-1-type", "VERIFY");
//        assertElementValue(page, "payment-step-1-status", "Succeed");
//        assertElementNotExists(page, "payment-step-1-error");
//
//        // First settle step (error).
//        assertElementExists(page, "payment-step-2");
//        assertElementValue(page, "payment-step-2-type", "SETTLE");
//        assertElementValue(page, "payment-step-2-status", "Failed");
//        assertElementValue(page, "payment-step-2-error", "Error: invalid_payload (Details: Signature in exact scheme payload is required)");
//        assertElementNotExists(page, "payment-step-3");
//
//        // Calling /settle without error (no signature) ================================================================
//        now = System.currentTimeMillis() / 1000;
//        paymentRequirements = PaymentRequirements.builder()
//                .scheme(EXACT_SCHEME.name())
//                .network(BASE_SEPOLIA.name())
//                .maxAmountRequired("20000")
//                .resource("http://localhost/weather")
//                .maxTimeoutSeconds(60)
//                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
//                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
//                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
//                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
//                .build();
//        paymentPayload = PaymentPayload.builder()
//                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                .scheme(EXACT_SCHEME.name())
//                .network(BASE_SEPOLIA.name())
//                .payload(ExactSchemePayload.builder()
//                        .authorization(ExactSchemePayload.Authorization.builder()
//                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
//                                .to(TEST_SERVER_WALLET_ADDRESS_1)
//                                .value("20000")
//                                .validAfter(String.valueOf(now))
//                                .validBefore(String.valueOf(now + 10))
//                                .nonce(nonce)
//                                .build())
//                        .build())
//                .build();
//        signedPayload = X402PaymentHelper.getSignedPayload(
//                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY),
//                paymentRequirements,
//                paymentPayload);
//
//        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
//                        .contentType(APPLICATION_JSON)
//                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
//                        .content(JsonUtil.toJson(SettleRequest.builder()
//                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
//                                .paymentPayload(signedPayload)
//                                .paymentRequirements(paymentRequirements)
//                                .build())))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.name()))
//                .andExpect(jsonPath("$.errorReason").isEmpty())
//                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));
//
//        // Checking the payment page.
//        await().until(this::allEventsAreTreated);
//        result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
//                .andExpect(status().isOk())
//                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
//                .andReturn();
//        page = Jsoup.parse(result.getResponse().getContentAsString());
//
//        // Payment header.
//        assertElementValue(page, "payment-nonce", nonce);
//        assertElementValue(page, "payment-status", "Completed");
//        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
//        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
//        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
//        assertElementValue(page, "payment-amount", "0,02 USDC");
//        assertElementValue(page, "payment-version", "1");
//
//        // First verify step (error).
//        assertElementExists(page, "payment-step-0");
//        assertElementValue(page, "payment-step-0-type", "VERIFY");
//        assertElementValue(page, "payment-step-0-status", "Failed");
//        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");
//
//        // Second verify step (success).
//        assertElementExists(page, "payment-step-1");
//        assertElementValue(page, "payment-step-1-type", "VERIFY");
//        assertElementValue(page, "payment-step-1-status", "Succeed");
//        assertElementNotExists(page, "payment-step-1-error");
//
//        // First settle step (error).
//        assertElementExists(page, "payment-step-2");
//        assertElementValue(page, "payment-step-2-type", "SETTLE");
//        assertElementValue(page, "payment-step-2-status", "Failed");
//        assertElementValue(page, "payment-step-2-error", "Error: invalid_payload (Details: Signature in exact scheme payload is required)");
//
//
//        // Second settle step (success).
//        assertElementExists(page, "payment-step-3");
//        assertElementValue(page, "payment-step-3-type", "SETTLE");
//        assertElementValue(page, "payment-step-3-status", "Succeed");
//        assertElementNotExists(page, "payment-step-4-error");
    }

    /**
     * Checks if all outbox events are treated (either DONE or ERROR).
     *
     * @return true if all events are treated, false otherwise
     */
    private boolean allEventsAreTreated() {
        return outboxEventRepository.findAll()
                .stream()
                .allMatch(event -> event.getStatus().isFinal());
    }

}
