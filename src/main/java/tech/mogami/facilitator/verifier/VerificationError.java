package tech.mogami.facilitator.verifier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Verification errors.
 * TODO Maybe move this to commons.
 */
@RequiredArgsConstructor
public enum VerificationError {

    /** Undefined error. */
    UNDEFINED("undefined"),

    /** Unsupported scheme. */
    UNSUPPORTED_SCHEME("unsupported_scheme"),

    /** Invalid network. */
    INVALID_NETWORK("invalid_network"),

    /** Invalid signature. */
    INVALID_EXACT_SIGNATURE("invalid_exact_evm_payload_signature");

    /** Error code. */
    @Getter
    private final String errorCode;

}
