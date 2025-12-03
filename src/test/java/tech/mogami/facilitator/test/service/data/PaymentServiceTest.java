package tech.mogami.facilitator.test.service.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.service.data.PaymentService;
import tech.mogami.facilitator.test.util.BaseTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16:///explorer",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@ActiveProfiles({"scheduler-disabled"})
@DisplayName("Payment service tests")
public class PaymentServiceTest extends BaseTest {

    /** Complete payment. */
    public static final String COMPLETE_PAYMENT_NONCE = "NONCE_00001";

    /** Uncompleted payment. */
    public static final String UNCOMPLETED_PAYMENT = "NONCE_00002";

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OutboxService outboxService;

    @Test
    @DisplayName("Test payment step log creation")
    public void testCreatePaymentStepLog() throws InterruptedException {
        final long countBeforeCallingServices = paymentRepository.count();

        // Invalid payment step should be refused ======================================================================
//        assertThatExceptionOfType(ConstraintViolationException.class)
//                .isThrownBy(() -> outboxService.publish(NewPaymentStepMessage.builder().build()));
//        assertThatExceptionOfType(ConstraintViolationException.class)
//                .isThrownBy(() -> paymentQueueService.addPaymentStep(PaymentStepDto.builder().build()));
//        assertThatExceptionOfType(ConstraintViolationException.class)
//                .isThrownBy(() -> paymentQueueService.addPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).build()));
//        assertThatExceptionOfType(ConstraintViolationException.class)
//                .isThrownBy(() -> paymentQueueService.addPaymentStep(PaymentStepDto.builder().nonce("RANDOM_NONCE").build()));

        // Calling the service several times with the same nonce =======================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .errorMessage("Invalid Json")
//                .build());
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).nonce(UNCOMPLETED_PAYMENT).build());
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .errorCode("invalid_json")
//                .build());
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        assertThat(paymentRepository.count()).isEqualTo(countBeforeCallingServices + 2);

        // Testing what we have created ================================================================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(2);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress()).isNull();
//                    assertThat(payment.toAddress()).isNull();
//                    assertThat(payment.assetAmount()).isNull();
//                    assertThat(payment.assetContract()).isNull();
//                    assertThat(payment.network()).isNull();
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isNull();
//                    assertThat(payment.formattedTo()).isNull();
//                    assertThat(payment.formattedAmount(ENGLISH)).isNull();
//                    assertThat(payment.formattedAmount(FRENCH)).isNull();
//                });
//        assertThat(paymentService.searchPaymentById(UNCOMPLETED_PAYMENT)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(UNCOMPLETED_PAYMENT);
//                    assertThat(payment.steps()).hasSize(1);
//                });

        // We add a failed verify step without signature ===============================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getVerifyRequest(
//                        BASE_SEPOLIA,
//                        TEST_CLIENT_WALLET_ADDRESS_1,
//                        TEST_CLIENT_WALLET_ADDRESS_2,
//                        BASE_SEPOLIA_USDC_CONTRACT,
//                        "1500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getVerifyResponse(
//                        false,
//                        INVALID_EXACT_EVM_PAYLOAD_SIGNATURE,
//                        TEST_CLIENT_WALLET_ADDRESS_1
//                ))
//                .errorCode(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode())
//                .errorMessage(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getDefaultMessage())
//                .build());

        // We add a failed verify step  ================================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .errorCode("invalid_json")
//                .errorMessage("Invalid Json")
//                .build());

        // We now check the value we can get from the payment step =====================================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(4);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress()).isNotNull();
//                    assertThat(payment.fromAddress().address()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_1);
//                    assertThat(payment.toAddress().address()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_2);
//                    assertThat(payment.assetAmount()).isEqualTo("1500500000");
//                    assertThat(payment.assetContract().address()).isEqualTo(BASE_SEPOLIA_USDC_CONTRACT);
//                    assertThat(payment.network().name()).isEqualTo(BASE_SEPOLIA.name());
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_1);
//                    assertThat(payment.formattedTo()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_2);
//                    assertThat(payment.formattedAmount(ENGLISH)).isEqualTo("1,500.5 USDC");
//                    assertThat(normalizeSpaces(payment.formattedAmount(FRENCH))).isEqualTo("1 500,5 USDC");
//                });

        // We now add a failed verify step with different values =======================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getVerifyRequest(
//                        BASE_MAINNET,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD22222",
//                        "0xCC6f005718945b59cfC5aF1981BF93904A822222",
//                        "0x036CbD53842c5426634e7929541eC2318f322222",
//                        "2500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getVerifyResponse(
//                        false,
//                        INVALID_EXACT_EVM_PAYLOAD_SIGNATURE,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD22222"
//                ))
//                .errorCode(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode())
//                .errorMessage(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getDefaultMessage())
//                .build());

        // The payment values must have been updated ===================================================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(5);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress().address()).isEqualTo("0xf6b42050A71Ca13f842eDa53C7d31B7C1BD22222");
//                    assertThat(payment.toAddress().address()).isEqualTo("0xCC6f005718945b59cfC5aF1981BF93904A822222");
//                    assertThat(payment.assetAmount()).isEqualTo("2500500000");
//                    assertThat(payment.assetContract().address()).isEqualTo("0x036CbD53842c5426634e7929541eC2318f322222");
//                    assertThat(payment.network().name()).isEqualTo(BASE_MAINNET.name());
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isEqualTo("0xf6b42050A71Ca13f842eDa53C7d31B7C1BD22222");
//                    assertThat(payment.formattedTo()).isEqualTo("0xCC6f005718945b59cfC5aF1981BF93904A822222");
//                    assertThat(payment.formattedAmount(ENGLISH)).isEqualTo("2,500,500,000");
//                    assertThat(normalizeSpaces(payment.formattedAmount(FRENCH))).isEqualTo("2 500 500 000");
//                });

        // We add a failed settle step  ================================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(SETTLE)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getSettleRequest(
//                        BASE_SEPOLIA,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD33333",
//                        "0xCC6f005718945b59cfC5aF1981BF93904A833333",
//                        "0x036CbD53842c5426634e7929541eC2318f333333",
//                        "3500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getSettleResponse(
//                        false,
//                        UNEXPECTED_SETTLE_ERROR,
//                        "0xcf7d269daf58b2bb3939b878aabe624c6b5e1c31329d9325e2a60eae1af33333",
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD33333"
//                ))
//                .errorCode(UNEXPECTED_SETTLE_ERROR.getCode())
//                .errorMessage(UNEXPECTED_SETTLE_ERROR.getDefaultMessage())
//                .build());

        // The payment values must have beed updated ===================================================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(6);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress().address()).isEqualTo("0xf6b42050A71Ca13f842eDa53C7d31B7C1BD33333");
//                    assertThat(payment.toAddress().address()).isEqualTo("0xCC6f005718945b59cfC5aF1981BF93904A833333");
//                    assertThat(payment.assetAmount()).isEqualTo("3500500000");
//                    assertThat(payment.assetContract().address()).isEqualTo("0x036CbD53842c5426634e7929541eC2318f333333");
//                    assertThat(payment.network().name()).isEqualTo(BASE_SEPOLIA.name());
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isEqualTo("0xf6b42050A71Ca13f842eDa53C7d31B7C1BD33333");
//                    assertThat(payment.formattedTo()).isEqualTo("0xCC6f005718945b59cfC5aF1981BF93904A833333");
//                    assertThat(payment.formattedAmount(ENGLISH)).isEqualTo("3,500,500,000");
//                    assertThat(normalizeSpaces(payment.formattedAmount(FRENCH))).isEqualTo("3 500 500 000");
//                });

        // A failed verify step is added ===============================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getVerifyRequest(
//                        BASE_MAINNET,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD44444",
//                        "0xCC6f005718945b59cfC5aF1981BF93904A844444",
//                        "0x036CbD53842c5426634e7929541eC2318f344444",
//                        "4500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getVerifyResponse(
//                        false,
//                        INVALID_EXACT_EVM_PAYLOAD_SIGNATURE,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD44444"
//                ))
//                .errorCode(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode())
//                .errorMessage(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getDefaultMessage())
//                .build());

        // The payment values must NOT be updated ======================================================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(7);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress().address()).isEqualTo("0xf6b42050A71Ca13f842eDa53C7d31B7C1BD33333");
//                    assertThat(payment.toAddress().address()).isEqualTo("0xCC6f005718945b59cfC5aF1981BF93904A833333");
//                    assertThat(payment.assetAmount()).isEqualTo("3500500000");
//                    assertThat(payment.assetContract().address()).isEqualTo("0x036CbD53842c5426634e7929541eC2318f333333");
//                    assertThat(payment.network().name()).isEqualTo(BASE_SEPOLIA.name());
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isEqualTo("0xf6b42050A71Ca13f842eDa53C7d31B7C1BD33333");
//                    assertThat(payment.formattedTo()).isEqualTo("0xCC6f005718945b59cfC5aF1981BF93904A833333");
//                    assertThat(payment.formattedAmount(ENGLISH)).isEqualTo("3,500,500,000");
//                    assertThat(normalizeSpaces(payment.formattedAmount(FRENCH))).isEqualTo("3 500 500 000");
//                });

        // A successful settle step is added ===========================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(SETTLE)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getSettleRequest(
//                        BASE_MAINNET,
//                        TEST_CLIENT_WALLET_ADDRESS_1,
//                        TEST_CLIENT_WALLET_ADDRESS_2,
//                        BASE_MAINNET_USDC_CONTRACT,
//                        "4500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getSettleResponse(
//                        false,
//                        UNEXPECTED_SETTLE_ERROR,
//                        "0xcf7d269daf58b2bb3939b878aabe624c6b5e1c31329d9325e2a60eae1af33333",
//                        TEST_CLIENT_WALLET_ADDRESS_1
//                ))
//                .build());

        // The payment values must be updated ==========================================================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(8);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress().address()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_1);
//                    assertThat(payment.toAddress().address()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_2);
//                    assertThat(payment.assetAmount()).isEqualTo("4500500000");
//                    assertThat(payment.assetContract().address()).isEqualTo(BASE_MAINNET_USDC_CONTRACT);
//                    assertThat(payment.network().name()).isEqualTo(BASE_MAINNET.name());
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_1);
//                    assertThat(payment.formattedTo()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_2);
//                    assertThat(payment.formattedAmount(ENGLISH)).isEqualTo("4,500.5 USDC");
//                    assertThat(normalizeSpaces(payment.formattedAmount(FRENCH))).isEqualTo("4 500,5 USDC");
//                });

        // A failed verify step is added ===============================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getVerifyRequest(
//                        BASE_MAINNET,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD55555",
//                        "0xCC6f005718945b59cfC5aF1981BF93904A855555",
//                        "0x036CbD53842c5426634e7929541eC2318f355555",
//                        "5500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getVerifyResponse(
//                        false,
//                        INVALID_EXACT_EVM_PAYLOAD_SIGNATURE,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD55555"
//                ))
//                .errorCode(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode())
//                .errorMessage(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getDefaultMessage())
//                .build());

        // A successful verify step is added ===========================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(VERIFY)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getVerifyRequest(
//                        BASE_MAINNET,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD66666",
//                        "0xCC6f005718945b59cfC5aF1981BF93904A866666",
//                        "0x036CbD53842c5426634e7929541eC2318f366666",
//                        "5500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getVerifyResponse(
//                        false,
//                        INVALID_EXACT_EVM_PAYLOAD_SIGNATURE,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD66666"
//                ))
//                .build());

        // A failed settle step is added ===============================================================================
//        paymentQueueService.addPaymentStep(PaymentStepDto.builder()
//                .paymentStepType(SETTLE)
//                .nonce(COMPLETE_PAYMENT_NONCE)
//                .requestPayload(getSettleRequest(
//                        BASE_SEPOLIA,
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD77777",
//                        "0xCC6f005718945b59cfC5aF1981BF93904A877777",
//                        "0x036CbD53842c5426634e7929541eC2318f77777",
//                        "7500500000",
//                        COMPLETE_PAYMENT_NONCE))
//                .responsePayload(getSettleResponse(
//                        false,
//                        UNEXPECTED_SETTLE_ERROR,
//                        "0xcf7d269daf58b2bb3939b878aabe624c6b5e1c31329d9325e2a60eae1af33333",
//                        "0xf6b42050A71Ca13f842eDa53C7d31B7C1BD77777"
//                ))
//                .errorCode(UNEXPECTED_SETTLE_ERROR.getCode())
//                .errorMessage(UNEXPECTED_SETTLE_ERROR.getDefaultMessage())
//                .build());
//        assertThat(paymentRepository.count()).isEqualTo(countBeforeCallingServices + 2);

        // The payment values must not have changed - Still the successful step ========================================
//        Thread.sleep(Duration.ofSeconds(3).toMillis());
//        paymentBatch.updatePayments();
//        assertThat(paymentService.searchPaymentById(COMPLETE_PAYMENT_NONCE)).isPresent().get()
//                .satisfies(payment -> {
//                    assertThat(payment.paymentId()).isEqualTo(COMPLETE_PAYMENT_NONCE);
//                    assertThat(payment.steps()).hasSize(11);
//
//                    // Raw values.
//                    assertThat(payment.paymentId()).isNotNull();
//                    assertThat(payment.fromAddress().address()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_1);
//                    assertThat(payment.toAddress().address()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_2);
//                    assertThat(payment.assetAmount()).isEqualTo("4500500000");
//                    assertThat(payment.assetContract().address()).isEqualTo(BASE_MAINNET_USDC_CONTRACT);
//                    assertThat(payment.network().name()).isEqualTo(BASE_MAINNET.name());
//
//                    // Formatted values.
//                    assertThat(payment.formattedFrom()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_1);
//                    assertThat(payment.formattedTo()).isEqualTo(TEST_CLIENT_WALLET_ADDRESS_2);
//                    assertThat(payment.formattedAmount(ENGLISH)).isEqualTo("4,500.5 USDC");
//                    assertThat(normalizeSpaces(payment.formattedAmount(FRENCH))).isEqualTo("4 500,5 USDC");
//                });

    }


}
