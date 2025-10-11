package tech.mogami.facilitator.verifier;

import tech.mogami.commons.constant.X402Error;

/**
 * VerificationResult is a placeholder for the result of a verification process.
 *
 * @param isValid           indicates if the verification was successful
 * @param verificationError reason why the verification failed (Respects x402 specs)
 * @param errorMessage      field added by Mogami to explain exactly why the verification failed
 */
public record VerificationResult(
        boolean isValid,
        X402Error verificationError,
        String errorMessage
) {

    /**
     * Creates a successful verification result.
     *
     * @return a VerificationResult indicating success
     */
    public static VerificationResult ok() {
        return new VerificationResult(true, null, null);
    }

    /**
     * Creates a failed verification result.
     *
     * @param verificationError the error that caused the failure
     * @return a VerificationResult indicating failure with the specified error
     */
    public static VerificationResult fail(final X402Error verificationError, final String errorMessage) {
        return new VerificationResult(false, verificationError, errorMessage);
    }

}
