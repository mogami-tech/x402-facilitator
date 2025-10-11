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
            log.info("Running verification with {}: {}", v.type(), verifyRequest);
            VerificationResult result = v.verify(verifyRequest);
            if (!result.isValid()) {
                log.info("Verification error {} : {}", v.type(), result.errorMessage());
                return VerifyResponse.builder()
                        .isValid(false)
                        .invalidReason(result.verificationError().getCode())
                        .payer(getPayerFromVerifyRequest(verifyRequest))
                        .build();
            } else {
                log.debug("Verification successful with {}", v.type());
            }
        }

        // No error, so we return a valid response.
        log.info("All verifiers passed for request: {}", verifyRequest);
        return VerifyResponse.builder()
                .isValid(true)
                .payer(getPayerFromVerifyRequest(verifyRequest))
                .build();
    }

    /**
     * Gets the payer from the verification request.
     *
     * @param verificationRequest the verification request
     * @return the payer address or "PAYER_NOT_FOUND" if not found
     */
    private String getPayerFromVerifyRequest(final VerifyRequest verificationRequest) {
        if (verificationRequest == null || verificationRequest.paymentPayload() == null) {
            return "PAYER_NOT_FOUND";
        }
        if (verificationRequest.paymentPayload().payload() instanceof ExactSchemePayload exact
                && exact.authorization() != null
                && exact.authorization().from() != null) {
            return exact.authorization().from();
        }
        return "PAYER_NOT_FOUND";
    }

}
