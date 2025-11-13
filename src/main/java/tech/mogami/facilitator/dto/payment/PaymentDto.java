package tech.mogami.facilitator.dto.payment;

import lombok.Singular;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.payment.PaymentStatus;
import tech.mogami.facilitator.dto.blockchain.AddressDto;

import java.math.BigInteger;
import java.util.List;

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
}
