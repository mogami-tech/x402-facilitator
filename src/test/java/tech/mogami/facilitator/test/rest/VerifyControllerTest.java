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
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.VERIFY_URL;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_SERVER_WALLET_ADDRESS_1;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("/verify tests")
public class VerifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/verify with an error")
    void verifyWithErrorTest() throws Exception {
        PaymentRequirements paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("10000")
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
                        .signature("0xde533856d81c76984a8dbc8d563bbb6d6d4ca36ce6c4d6e8cf315de3bfc14ab26d6bcdc37549aeed78bf92e39d5180268f8f399a4ffb816cfbf500823882b6001c")
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

        mockMvc.perform(MockMvcRequestBuilders.post(VERIFY_URL)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON, TEXT_PLAIN, MediaType.ALL)
                        .header("User-Agent", "axios/1.8.4")
                        .header("Accept-Encoding", "identity")
                        .content(JsonUtil.toJson(VerifyRequest.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .paymentPayload(paymentPayload)
                                .paymentRequirements(paymentRequirements)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(status().isOk()) // Ou HttpStatus.BAD_REQUEST si c'est une erreur client
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.invalidReason").value("invalid_exact_evm_payload_authorization_valid_before"))
                .andExpect(jsonPath("$.payer").value(TEST_CLIENT_WALLET_ADDRESS_1));
    }

    @Test
    @DisplayName("/verify without error")
    void verifyWithoutErrorTest() throws Exception {
        // TODO Make a live test with Mogami client SDK.
    }


}
