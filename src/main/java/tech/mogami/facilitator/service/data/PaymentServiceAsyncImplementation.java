package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;

import java.util.UUID;

/**
 * Payment service async.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class PaymentServiceAsyncImplementation {

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    /**
     * Logs a payment step asynchronously.
     *
     * @param paymentId the ID of the payment
     * @param dto       the payment step data transfer object
     */
    @Async("paymentLogExecutor")
    @Transactional
    public void logPaymentStep(final String paymentId, final PaymentStepDto dto) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment " + paymentId + " not found"));

        // Add a step.
        payment.addStep(PaymentStep.builder()
                .paymentStepId(UUID.randomUUID().toString())
                .payment(payment)
                .paymentStepType(dto.paymentStepType())
                .requestPayload(dto.requestPayload())
                .responsePayload(dto.responsePayload())
                .errorCode(dto.errorCode())
                .errorMessage(dto.errorMessage())
                .build()
        );

        // Save it.
        paymentRepository.save(payment);
        log.info("Payment step persisted for payment {}", paymentId);
    }

}
