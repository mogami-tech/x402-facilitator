package tech.mogami.facilitator.service;

import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;

/**
 * Verify service provides methods to verify payment requests.
 * ✅ verify request is here
 * TODO Why there is no x402Version verification?
 * ✅ verify payload and payment schemes
 * ✅ verify payment context
 */
public interface VerifyService {

    /**
     * Verifies a payment request.
     *
     * @param verificationRequest the request containing the payment details to verify
     * @return VerifyResponse containing the verification result
     */
    VerifyResponse verify(VerifyRequest verificationRequest);

}
