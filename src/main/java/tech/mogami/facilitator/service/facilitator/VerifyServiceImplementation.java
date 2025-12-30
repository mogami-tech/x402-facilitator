package tech.mogami.facilitator.service.facilitator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
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
    public VerificationResult verify(final VerificationRequest verificationRequest) {
        // We run all verifiers in order, and return the first failure if any (Using @Order annotation).
        for (Verifier v : verifiers) {
            log.info("Running verification with {}: {}", v.type(), verificationRequest);
            VerificationResult result = v.verify(verificationRequest);
            if (!result.isValid()) {
                log.info("Verification error {} : {}", v.type(), result.errorMessage());
                return result;
            } else {
                log.debug("Verification successful with {}", v.type());
            }
        }

        // No error, so we return a valid response.
        log.info("All verifiers passed for request: {}", verificationRequest);
        return VerificationResult.ok();
    }

}
