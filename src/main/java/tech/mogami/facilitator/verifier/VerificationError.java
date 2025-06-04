package tech.mogami.facilitator.verifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Verification errors.
 */
@RequiredArgsConstructor
public enum VerificationError {

    /** Undefined error. */
    UNDEFINED("undefined"),

    /** Unsupported x402 scheme. */
    UNSUPPORTED_X402_SCHEME("unsupported_scheme");

    /** Error code. */
    @Getter
    private final String errorCode;

}
