package tech.mogami.facilitator.parameter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * X402 Parameters.
 *
 * @param facilitator facilitator parameters
 * @param examples    non-functional example values used for documentation and UI
 */
@Validated
@SuppressWarnings("unused")
@ConfigurationProperties(prefix = "x402")
public record X402Parameters(

        @Valid
        @NotNull
        Facilitator facilitator,

        Examples examples

) {

    /**
     * Facilitator parameters.
     *
     * @param privateKey the private key of the facilitator
     */
    public record Facilitator(

            @NotEmpty
            String privateKey

    ) {
    }

    /**
     * Non-functional example values used for documentation and UI.
     *
     * @param nonce an existing payment nonce
     */
    public record Examples(

            String nonce

    ) {
    }

}