package tech.mogami.facilitator.verifier;

import tech.mogami.commons.api.facilitator.verify.VerifyRequest;

/**
 * Verifier interface defines what a verifier should implement.
 */
public interface Verifier {

    /**
     * Verifies a request.
     *
     * @param verifyRequest the request to verify
     * @return the result of the verification
     */
    VerificationResult verify(VerifyRequest verifyRequest);

    /**
     * Returns the type of verification step this verifier handles.
     *
     * @return the type of verification step
     */
    VerificationStep type();

}
