package tech.mogami.facilitator.service.facilitator;

import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.facilitator.verifier.VerificationResult;

/**
 * Verify service provides methods to verify payment requests.
 */
public interface VerifyService {

    /**
     * Verifies a payment request.
     *
     * @param verifyRequest the request containing the payment details to verify
     * @return verification result
     */
    VerificationResult verify(VerificationRequest verifyRequest);

}
