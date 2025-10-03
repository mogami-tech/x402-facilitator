package tech.mogami.facilitator.test.rest;

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
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.java.client.helper.X402PaymentHelper;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_ENDPOINT;
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
@DisplayName("/verify tests")
public class VerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Calling /verify with an error")
    void verifyWithError() throws Exception {
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("10000")
                .resource("http://localhost/weather")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        var paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .signature("0x7d9463e2c7c98e33c08747882521be88cc02443a8c46f3a1f5b51ae8d1bdd9581fa41ab35c1cebfe70a79471640a1bde9ffadd377e38d708b5ca6a38b30300f61b")
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("10000")
                                .validAfter("1748534647")
                                .validBefore("1748534767")
                                .nonce("0x9b750f5097972d82c02ac371278b83ecf3ca3be8387db59e664eb38c98f97a3d")
                                .build())
                        .build())
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(JsonUtil.toJson(VerifyRequest.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .paymentPayload(paymentPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.invalidReason").value("invalid_exact_evm_payload_authorization_valid_before"))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));
    }

    @Test
    @DisplayName("Calling /verify without error")
    void verifyWithoutError() throws Exception {
        var now = System.currentTimeMillis() / 1000;
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("10000")
                .resource("http://localhost/weather")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        var paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("10000")
                                .validAfter(String.valueOf(now))
                                .validBefore(String.valueOf(now + 10))
                                .nonce("0x9b750f5097972d82c02ac371278b83ecf3ca3be8387db59e664eb38c98f97a3d")
                                .build()
                        ).build()
                ).build();

        // We use Mogami client SDK to create a payment payload with insufficient funds.
        var signedPayload = X402PaymentHelper.getSignedPayload(
                Credentials.create(TEST_CLIENT_WALLET_ADDRESS_1_PRIVATE_KEY),
                paymentRequirements,
                paymentPayload);

        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_ENDPOINT)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(JsonUtil.toJson(VerifyRequest.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .paymentPayload(signedPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.invalidReason").isEmpty())
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));
    }

}
