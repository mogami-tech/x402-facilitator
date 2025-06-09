package tech.mogami.facilitator.verifier;

/**
 * VerificationStep is a placeholder for a step in the verification process.
 */
public enum VerificationStep {

    /** Verify that the request is not empty. */
    REQUEST_NOT_EMPTY,

    /** Verify the scheme. */
    SCHEME_EXISTS,

    /** Payment context verification step for the exact scheme. */
    PAYMENT_CONTEXT_FOR_EXACT_SCHEME,

    /** Signature verification step for the exact scheme. */
    SIGNATURE_FOR_EXACT_SCHEME;

}
