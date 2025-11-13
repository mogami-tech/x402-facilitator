package tech.mogami.facilitator.dto.blockchain;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import tech.mogami.commons.validator.BlockchainAddress;

import static tech.mogami.commons.constant.BlockchainConstants.BLOCKCHAIN_ADDRESS_LENGTH;
import static tech.mogami.commons.constant.BlockchainConstants.BLOCKCHAIN_ADDRESS_SHORTEN_PREFIX_LENGTH;
import static tech.mogami.commons.constant.BlockchainConstants.BLOCKCHAIN_ADDRESS_SHORTEN_SUFFIX_LENGTH;


/**
 * Address on blockchain.
 *
 * @param id      Unique identifier.
 * @param address Blockchain address.
 */
@Builder
public record AddressDto(
        Long id,
        @BlockchainAddress String address
) {

    /**
     * Returns a shortened address.
     */
    public String shortenAddress() {
        if (StringUtils.length(address) == BLOCKCHAIN_ADDRESS_LENGTH) {
            return String.format("%s...%s",
                    StringUtils.left(address, BLOCKCHAIN_ADDRESS_SHORTEN_PREFIX_LENGTH),
                    StringUtils.right(address, BLOCKCHAIN_ADDRESS_SHORTEN_SUFFIX_LENGTH));
        } else {
            return address;
        }
    }

}
