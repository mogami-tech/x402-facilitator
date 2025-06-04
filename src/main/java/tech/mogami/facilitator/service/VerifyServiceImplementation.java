package tech.mogami.facilitator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
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
     * @param verificationRequest the request containing the payment details to verify
     * @return VerifyResponse containing the verification result
     */
    @Override
    public VerifyResponse verify(final VerifyRequest verificationRequest) {

        // We run all verifiers in order, and return the first failure if any (Using @Order annotation).
        for (Verifier v : verifiers) {
            VerificationResult result = v.verify(verificationRequest);
            if (!result.isValid()) {
                log.debug("Verification error {} : {}", v.type(), result.errorMessage());
                return VerifyResponse.builder()
                        .isValid(false)
                        .invalidReason(result.verificationError().getErrorCode())
                        // TODO Add buyer.
                        .build();
            }
        }
        // No error, so we return a valid response.
        return VerifyResponse.builder()
                .isValid(true)
                // TODO Add buyer.
                .build();
    }

}
