package tech.mogami.facilitator.test.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.web3j.crypto.Credentials;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;
import tech.mogami.commons.payment.PaymentRequired;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.PaymentResource;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.java.client.X402V2Client;

import java.util.List;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SETTLE_ENDPOINT;
import static tech.mogami.commons.constant.X402Error.INSUFFICIENT_FUNDS;
import static tech.mogami.commons.constant.X402Error.INVALID_TRANSACTION_STATE;
import static tech.mogami.commons.constant.network.Networks.BASE_MAINNET;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_2;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mockedBlockchain")
@DisplayName("/settle tests")
public class SettleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Calling /settle with an error")
    void settleWithError() throws Exception {
        // Error during the verification inside the settle process =====================================================
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_MAINNET.networkId())
                .amount("10000")
                .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
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

        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .content(JsonUtil.toJson(SettlementRequest.builder()
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.network").value(BASE_MAINNET.networkId()))
                .andExpect(jsonPath("$.errorReason").value(INSUFFICIENT_FUNDS.getCode()))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));

        // Error during settle =========================================================================================
        paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("10000")
                .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
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

        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .content(JsonUtil.toJson(SettlementRequest.builder()
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.networkId()))
                .andExpect(jsonPath("$.errorReason").value(INVALID_TRANSACTION_STATE.getCode()))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));
    }

    @Test
    @DisplayName("/settle without error")
    void settleWithoutError() throws Exception {
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("10000")
                .payTo(TEST_CLIENT_WALLET_ADDRESS_2)
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

        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .content(JsonUtil.toJson(SettlementRequest.builder()
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.networkId()))
                .andExpect(jsonPath("$.errorReason").isEmpty())
                .andExpect(jsonPath("$.transaction").value("0xMOCKEDTRANSACTIONHASH"))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));
    }

}
