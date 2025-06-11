package tech.mogami.facilitator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.Verifier;

import java.util.List;

/**
 * {@link VerifyService} implementation.
 */
@Slf4j
@Service
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class VerifyServiceImplementation implements VerifyService {

    /** List of verifiers to be used for verification. */
    private final List<Verifier> verifiers;

    /**
     * Default constructor.
     *
     * @param newVerifiers the list of verifiers to use for verification
     */
    public VerifyServiceImplementation(final List<Verifier> newVerifiers) {
        verifiers = newVerifiers;
        log.info("Verify service initialized with {} verifiers", verifiers.size());
    }

    /**
     * Verifies a payment request.
     *
     * @param verifyRequest the request containing the payment details to verify
     * @return VerifyResponse containing the verification result
     */
    @Override
    public VerifyResponse verify(final VerifyRequest verifyRequest) {
        // We run all verifiers in order, and return the first failure if any (Using @Order annotation).
        for (Verifier v : verifiers) {
            VerificationResult result = v.verify(verifyRequest);
            if (!result.isValid()) {
                log.debug("Verification error {} : {}", v.type(), result.errorMessage());
                return VerifyResponse.builder()
                        .isValid(false)
                        .invalidReason(result.verificationError().getErrorCode())
                        .payer(getPayerFromVerifyRequest(verifyRequest))
                        .build();
            }
        }

        // No error, so we return a valid response.
        return VerifyResponse.builder()
                .isValid(true)
                .payer(getPayerFromVerifyRequest(verifyRequest))
                .build();
    }

    /**
     * Gets the payer from the verification request.
     * TODO optimize when all fields will be clearly checked.
     *
     * @param verificationRequest the verification request
     * @return the payer address or "PAYER_NOT_FOUND" if not found
     */
    private String getPayerFromVerifyRequest(final VerifyRequest verificationRequest) {
        if (verificationRequest == null || verificationRequest.paymentPayload() == null) {
            return "PAYER_NOT_FOUND";
        }
        ExactSchemePayload payload = (ExactSchemePayload) verificationRequest.paymentPayload().payload();
        if (payload != null && payload.authorization() != null && payload.authorization().from() != null) {
            return payload.authorization().from();
        } else {
            return "PAYER_NOT_FOUND";
        }
    }

}
