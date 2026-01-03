package tech.mogami.facilitator.test.core.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.web3j.crypto.Credentials;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
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
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_ENDPOINT;
import static tech.mogami.commons.constant.X402Error.INSUFFICIENT_FUNDS;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseMogamiTestData.EMPTY_WALLET_ADDRESS;
import static tech.mogami.commons.test.BaseMogamiTestData.EMPTY_WALLET_ADDRESS_PRIVATE_KEY;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_SERVER_WALLET_ADDRESS_1;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("/verify tests")
public class VerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Calling /verify with an error")
    void verifyWithError() throws Exception {
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("10000")
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
                Credentials.create(EMPTY_WALLET_ADDRESS_PRIVATE_KEY));

        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(JsonUtil.toJson(VerificationRequest.builder()
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.invalidReason").value(INSUFFICIENT_FUNDS.getCode()))
                .andExpect(jsonPath("$.payer").value(EMPTY_WALLET_ADDRESS.toLowerCase()));
    }

    @Test
    @DisplayName("Calling /verify without error")
    void verifyWithoutError() throws Exception {
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("10000")
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

        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(JsonUtil.toJson(VerificationRequest.builder()
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.invalidReason").isEmpty())
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1.toLowerCase()));
    }

}
