package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.repository.PaymentStepRepository;

import java.util.UUID;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * {@link PaymentStepService} implementation.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class PaymentStepServiceImplementation implements PaymentStepService {

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    /** Payment step repository. */
    private final PaymentStepRepository paymentStepRepository;

    @Override
    @Async("paymentLogExecutor")
    @Transactional(propagation = REQUIRES_NEW)
    public void addPaymentStep(final String paymentId, final PaymentStepDto paymentStep) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment " + paymentId + " not found"));

        // Save the step.
        final String paymentStepId = UUID.randomUUID().toString();
        paymentStepRepository.save(PaymentStep.builder()
                .paymentStepId(paymentStepId)
                .payment(payment)
                .paymentStepType(paymentStep.paymentStepType())
                .requestPayload(paymentStep.requestPayload())
                .responsePayload(paymentStep.responsePayload())
                .errorCode(paymentStep.errorCode())
                .errorMessage(paymentStep.errorMessage())
                .build());
        log.info("Payment step persisted for payment wit paymentStepId {}", paymentStepId);
    }

}
