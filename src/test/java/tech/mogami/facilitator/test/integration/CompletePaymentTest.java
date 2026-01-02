package tech.mogami.facilitator.test.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
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
import org.web3j.crypto.Credentials;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.PaymentRequired;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.PaymentResource;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.provider.outbox.repository.OutboxEventRepository;
import tech.mogami.facilitator.test.core.util.web.BaseWebTest;
import tech.mogami.java.client.X402V2Client;

import java.util.List;
import java.util.Locale;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SETTLE_ENDPOINT;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_ENDPOINT;
import static tech.mogami.commons.constant.X402Error.INVALID_EXACT_EVM_PAYLOAD_SIGNATURE;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY;
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
        // We start by generating the valid payment to use later =======================================================
        var validPaymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("20000")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        var validPaymentRequired = PaymentRequired.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .resource(PaymentResource.builder()
                        .url("https://example.com/resource/12345")
                        .build())
                .accepts(List.of(validPaymentRequirements))
                .build();
        var validSignedPayload = X402V2Client.buildPaymentPayload(
                validPaymentRequired,
                validPaymentRequirements,
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY));
        var nonce = ((ExactSchemePayload) validSignedPayload.getTypedPayload()).getNonce().orElseThrow();
        System.out.println("=> Using nonce: " + nonce);

        // Calling /verify with an error ===============================================================================
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("20000")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        var paymentRequired = PaymentRequired.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .resource(PaymentResource.builder()
                        .url("https://example.com/resource/12345")
                        .build())
                .accepts(List.of(paymentRequirements))
                .build();
        var signedPayload = X402V2Client.buildPaymentPayload(
                paymentRequired,
                paymentRequirements,
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY));

        // Calling the service.
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(getVerificationRequestAsJson(
                                VerificationRequest.builder()
                                        .paymentPayload(signedPayload)
                                        .paymentRequirements(paymentRequirements)
                                        .build(),
                                nonce)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.invalidReason").value(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode()))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));

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
        assertElementValue(page, "payment-version", X402_SUPPORTED_VERSION_BY_MOGAMI.canonical());

        // First step (error).
        assertElementExists(page, "payment-step-0");
        assertElementValue(page, "payment-step-0-type", "VERIFY");
        assertElementValue(page, "payment-step-0-status", "Failed");
        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");
        assertElementNotExists(page, "payment-step-1");

        // Calling /verify without error ===============================================================================
        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(getVerificationRequestAsJson(
                                VerificationRequest.builder()
                                        .paymentPayload(validSignedPayload)
                                        .paymentRequirements(validPaymentRequirements)
                                        .build(),
                                nonce)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.invalidReason").isEmpty())
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));

        // Checking the payment page.
        await().until(this::allEventsAreTreated);
        result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
                .andReturn();
        page = Jsoup.parse(result.getResponse().getContentAsString());

        // Payment header.
        assertElementValue(page, "payment-nonce", nonce);
        assertElementValue(page, "payment-status", "Pending");
        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-amount", "0,02 USDC");
        assertElementValue(page, "payment-version", X402_SUPPORTED_VERSION_BY_MOGAMI.canonical());

        // First verify step (error).
        assertElementExists(page, "payment-step-0");
        assertElementValue(page, "payment-step-0-type", "VERIFY");
        assertElementValue(page, "payment-step-0-status", "Failed");
        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");

        // Second verify step (success).
        assertElementExists(page, "payment-step-1");
        assertElementValue(page, "payment-step-1-type", "VERIFY");
        assertElementValue(page, "payment-step-1-status", "Succeed");
        assertElementNotExists(page, "payment-step-1-error");
        assertElementNotExists(page, "payment-step-2");

        // Calling /settle with error (bad signature) ==================================================================
        paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("200")
                .maxTimeoutSeconds(60)
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        paymentRequired = PaymentRequired.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .resource(PaymentResource.builder()
                        .url("https://example.com/resource/12345")
                        .build())
                .accepts(List.of(paymentRequirements))
                .build();

        signedPayload = X402V2Client.buildPaymentPayload(
                paymentRequired,
                paymentRequirements,
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY));
        // We tamper the payload to produce an invalid signature.
        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(getSettlementRequestAsJson(
                                SettlementRequest.builder()
                                        .paymentPayload(signedPayload)
                                        .paymentRequirements(paymentRequirements)
                                        .build(),
                                nonce)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.networkId()))
                .andExpect(jsonPath("$.errorReason").value(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode()))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));

        // Checking the payment page.
        await().until(this::allEventsAreTreated);
        result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
                .andReturn();
        page = Jsoup.parse(result.getResponse().getContentAsString());

        // Payment header.
        assertElementValue(page, "payment-nonce", nonce);
        assertElementValue(page, "payment-status", "Failed");
        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
        // TODO Fix the error in here.
        // assertElementValue(page, "payment-amount", "0,02 USDC");
        assertElementValue(page, "payment-version", X402_SUPPORTED_VERSION_BY_MOGAMI.canonical());

        // First verify step (error).
        assertElementExists(page, "payment-step-0");
        assertElementValue(page, "payment-step-0-type", "VERIFY");
        assertElementValue(page, "payment-step-0-status", "Failed");
        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");

        // Second verify step (success).
        assertElementExists(page, "payment-step-1");
        assertElementValue(page, "payment-step-1-type", "VERIFY");
        assertElementValue(page, "payment-step-1-status", "Succeed");
        assertElementNotExists(page, "payment-step-1-error");

        // First settle step (error).
        assertElementExists(page, "payment-step-2");
        assertElementValue(page, "payment-step-2-type", "SETTLE");
        assertElementValue(page, "payment-step-2-status", "Failed");
        assertElementValue(page, "payment-step-2-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");
        assertElementNotExists(page, "payment-step-3");

        // Calling /settle without error (no signature) ================================================================
        var mockMvcResult = mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .content(getSettlementRequestAsJson(
                                SettlementRequest.builder()
                                        .paymentPayload(validSignedPayload)
                                        .paymentRequirements(validPaymentRequirements)
                                        .build(),
                                nonce)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.networkId()))
                .andExpect(jsonPath("$.errorReason").isEmpty())
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()))
                .andReturn();
        String responseBody = mockMvcResult.getResponse().getContentAsString();
        String transaction = JsonPath.read(responseBody, "$.transaction");
        System.out.println("==> Transaction = " + transaction);

        // Checking the payment page.
        await().until(this::allEventsAreTreated);
        result = mockMvc.perform(get(PAYMENT_BY_NONCE_URL.replace("{nonce}", nonce)))
                .andExpect(status().isOk())
                .andExpect(view().name(containsString(PAYMENT_BY_NONCE_PAGE.view())))
                .andReturn();
        page = Jsoup.parse(result.getResponse().getContentAsString());

        // Payment header.
        assertElementValue(page, "payment-nonce", nonce);
        assertElementValue(page, "payment-status", "Completed");
        assertElementValue(page, "payment-network-name", "Base Sepolia Testnet");
        assertElementValue(page, "payment-from-address", TEST_CLIENT_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-to-address", TEST_SERVER_WALLET_ADDRESS_1);
        assertElementValue(page, "payment-amount", "0,02 USDC");
        assertElementValue(page, "payment-version", X402_SUPPORTED_VERSION_BY_MOGAMI.canonical());

        // First verify step (error).
        assertElementExists(page, "payment-step-0");
        assertElementValue(page, "payment-step-0-type", "VERIFY");
        assertElementValue(page, "payment-step-0-status", "Failed");
        assertElementValue(page, "payment-step-0-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");

        // Second verify step (success).
        assertElementExists(page, "payment-step-1");
        assertElementValue(page, "payment-step-1-type", "VERIFY");
        assertElementValue(page, "payment-step-1-status", "Succeed");
        assertElementNotExists(page, "payment-step-1-error");

        // First settle step (error).
        assertElementExists(page, "payment-step-2");
        assertElementValue(page, "payment-step-2-type", "SETTLE");
        assertElementValue(page, "payment-step-2-status", "Failed");
        assertElementValue(page, "payment-step-2-error", "Error: invalid_exact_evm_payload_signature (Details: Signature verification failed for exact scheme)");

        // Second settle step (success).
        assertElementExists(page, "payment-step-3");
        assertElementValue(page, "payment-step-3-type", "SETTLE");
        assertElementValue(page, "payment-step-3-status", "Succeed");
        assertElementNotExists(page, "payment-step-4-error");
    }

    /**
     * Returns the JSON representation of the settlement request with the given nonce.
     *
     * @param settlementRequest the settlement request
     * @param nonce             the nonce to set
     * @return the JSON representation of the settlement request
     * @throws JsonProcessingException if an error occurs during JSON processing
     */
    private String getSettlementRequestAsJson(SettlementRequest settlementRequest, String nonce) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(JsonUtil.toPrettyJson(settlementRequest));

        ObjectNode authorization = root
                .withObject("paymentPayload")
                .withObject("payload")
                .withObject("authorization");
        authorization.put("nonce", nonce);

        return mapper.writeValueAsString(root);
    }

    /**
     * Returns the JSON representation of the verification request with the given nonce.
     *
     * @param verificationRequest the verification request
     * @param nonce               the nonce to set
     * @return the JSON representation of the verification request
     * @throws JsonProcessingException if an error occurs during JSON processing
     */
    private String getVerificationRequestAsJson(VerificationRequest verificationRequest, String nonce) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(JsonUtil.toPrettyJson(verificationRequest));

        ObjectNode authorization = root
                .withObject("paymentPayload")
                .withObject("payload")
                .withObject("authorization");
        authorization.put("nonce", nonce);

        return mapper.writeValueAsString(root);
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
