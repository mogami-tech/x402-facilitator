package tech.mogami.facilitator.test.rest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.web3j.crypto.Credentials;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.commons.util.NonceUtil;
import tech.mogami.java.client.helper.X402PaymentHelper;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.SETTLE_URL;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY;
import static tech.mogami.commons.test.BaseTestData.TEST_SERVER_WALLET_ADDRESS_1;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("/settle tests")
public class SettleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/settle with an error")
    void settleWithErrorTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_URL)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .content(JsonUtil.toJson(
                                VerifyRequest.builder()
                                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                        .paymentPayload(PaymentPayload.builder()
                                                .network(BASE_SEPOLIA.name())
                                                .scheme(EXACT_SCHEME.name())
                                                .payload(ExactSchemePayload.builder()
                                                        .authorization(ExactSchemePayload.Authorization.builder().build()))
                                                .build())
                                        .paymentRequirements(PaymentRequirements.builder()
                                                .network(BASE_SEPOLIA.name())
                                                .scheme("INVALID_SCHEME").build())
                                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.name()))
                .andExpect(jsonPath("$.errorReason").value("unsupported_scheme"))
                .andExpect(jsonPath("$.payer").value("PAYER_NOT_FOUND"));
    }

    @Test
    @Disabled("Disabled until we can mock the smart contract call")
    @DisplayName("/settle without error")
    void settleWithoutErrorTest() throws Exception {
        long now = System.currentTimeMillis() / 1000;
        PaymentRequirements paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("20000")
                .resource("http://localhost/weather")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        PaymentPayload paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("20000")
                                .validAfter(String.valueOf(now))
                                .validBefore(String.valueOf(now + 10))
                                .nonce(NonceUtil.generateNonce())
                                .build()
                        ).build()
                ).build();

        // We use Mogami client SDK to create a payment payload with insufficient funds.
        var signedPayload = X402PaymentHelper.getSignedPayload(
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY),
                paymentRequirements,
                paymentPayload);

        mockMvc.perform(MockMvcRequestBuilders.post(SETTLE_URL)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .content(JsonUtil.toJson(VerifyRequest.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.network").value(BASE_SEPOLIA.name()))
                .andExpect(jsonPath("$.errorReason").isEmpty())
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));
    }

}
