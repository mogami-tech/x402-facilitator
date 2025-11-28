package tech.mogami.facilitator.dto.payment;

import lombok.Builder;
import lombok.Singular;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.payment.PaymentStatus;
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
        AddressDto fromAddress,
        AddressDto toAddress,
        BigInteger assetAmount,
        AddressDto assetContract,
        Network network,
        PaymentStatus status,
        @Singular List<PaymentStepDto> steps
) {

    /**
     * Get the formatted "from".
     *
     * @return the formatted "from"
     */
    public String formattedFrom() {
        return Optional.ofNullable(fromAddress)
                .map(AddressDto::address)
                .orElse(null);
    }

    /**
     * Get the formatted to address.
     *
     * @return the formatted to address
     */
    public String formattedTo() {
        return Optional.ofNullable(toAddress)
                .map(AddressDto::address)
                .orElse(null);
    }

    /**
     * Get the formatted asset amount with symbol based on the locale.
     *
     * @param locale the locale for formatting
     * @return the formatted asset amount with symbol
     */
    public String formattedAmount(final Locale locale) {
        if (assetAmount == null || assetContract == null || network == null) {
            return null;
        }

        // Manage number formatting based on locale if needed.
        final NumberFormat nf = DecimalFormat.getNumberInstance(locale);
        nf.setGroupingUsed(true);

        // We try to find if the asset contract used is registered in our network object.
        return network.findDeployedAsset(assetContract.address())
                .map(asset -> nf.format(asset.fromAtomic(assetAmount)) + " " + asset.asset().symbol())
                .orElseGet(() -> nf.format(assetAmount));
    }

}
