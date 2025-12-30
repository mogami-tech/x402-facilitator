package tech.mogami.facilitator.dto.payment;

import lombok.Builder;
import lombok.Singular;
import tech.mogami.commons.constant.PaymentStatus;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.version.X402Version;
import tech.mogami.facilitator.dto.blockchain.AddressDto;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Payment data transfer object.
 *
 * @param paymentId     Globally unique payment identifier (usually the nonce)
 * @param version       X402 version used for this payment
 * @param fromAddress   Address from which the payment is sent
 * @param toAddress     Address to which the payment is sent
 * @param assetAmount   Amount of the asset being transferred
 * @param assetContract Contract address of the asset being transferred
 * @param network       Network on which the payment is made
 * @param status        Current status of the payment
 * @param steps         List of payment steps associated with this payment
 */
@Builder
public record PaymentDto(
        String paymentId,
        X402Version version,
        AddressDto fromAddress,
        AddressDto toAddress,
        BigInteger assetAmount,
        AddressDto assetContract,
        Network network,
        PaymentStatus status,
        @Singular List<PaymentStepDto> steps
) {

    /** Unknown value for any formated field. */
    public static final String UNKNOWN_FORMATTED_VALUE = "-";

    /**
     * Get the formatted version.
     *
     * @return the formatted version
     */
    public String formattedVersion() {
        return Optional.ofNullable(version)
                .map(v -> String.valueOf(v.version()))
                .orElse(UNKNOWN_FORMATTED_VALUE);
    }

    /**
     * Get the formatted "from".
     *
     * @return the formatted "from"
     */
    public String formattedFrom() {
        return Optional.ofNullable(fromAddress)
                .map(AddressDto::address)
                .orElse(UNKNOWN_FORMATTED_VALUE);
    }

    /**
     * Get the formatted to address.
     *
     * @return the formatted to address
     */
    public String formattedTo() {
        return Optional.ofNullable(toAddress)
                .map(AddressDto::address)
                .orElse(UNKNOWN_FORMATTED_VALUE);
    }

    /**
     * Get the formatted asset amount with symbol based on the default locale.
     *
     * @return the formatted asset amount with symbol
     */
    public String formattedAmount() {
        return formattedAmount(Locale.getDefault());
    }

    /**
     * Get the formatted asset amount with symbol based on the locale.
     *
     * @param locale the locale for formatting
     * @return the formatted asset amount with symbol
     */
    public String formattedAmount(final Locale locale) {
        if (assetAmount == null || assetContract == null || network == null) {
            return UNKNOWN_FORMATTED_VALUE;
        }

        // Manage number formatting based on locale if needed.
        final NumberFormat nf = DecimalFormat.getNumberInstance(locale);
        nf.setGroupingUsed(true);

        // We try to find if the asset contract used is registered in our network object.
        return network.findDeployedAsset(assetContract.address())
                .map(asset -> nf.format(asset.fromAtomic(assetAmount)) + " " + asset.asset().symbol())
                .orElseGet(() -> nf.format(assetAmount));
    }

    /**
     * Get the formatted asset contract address.
     *
     * @return the formatted asset contract address
     */
    public String formattedAssetContract() {
        return Optional.ofNullable(assetContract)
                .map(AddressDto::address)
                .orElse(UNKNOWN_FORMATTED_VALUE);
    }

    /**
     * Get the formatted network name.
     *
     * @return the formatted network name
     */
    public String formattedNetwork() {
        return Optional.ofNullable(network)
                .map(Network::displayName)
                .orElse(UNKNOWN_FORMATTED_VALUE);
    }

}
