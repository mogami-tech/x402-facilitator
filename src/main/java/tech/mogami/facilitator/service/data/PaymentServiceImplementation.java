package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.util.base.BaseService;

import java.util.Optional;

/**
 * {@link PaymentService} implementation.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class PaymentServiceImplementation extends BaseService implements PaymentService {

    /** Address repository. */
    private final AddressRepository addressRepository;

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    /** Participant service. */
    private final ParticipantService participantService;

    /** Participant step service. */
    private final PaymentStepServiceImplementation paymentStepService;

    @Override
    public Optional<PaymentDto> searchPaymentById(final String paymentId) {
        log.debug("Retrieving payment by paymentId: {}", paymentId);
        return Optional.ofNullable(StringUtils.trimToNull(paymentId))
                .flatMap(paymentRepository::findByPaymentId)
                .map(PAYMENT_MAPPER::toDto);
    }

}
