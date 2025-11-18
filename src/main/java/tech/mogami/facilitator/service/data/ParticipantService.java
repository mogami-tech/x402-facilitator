package tech.mogami.facilitator.service.data;

import jakarta.validation.constraints.NotNull;
import tech.mogami.commons.validator.BlockchainAddress;
import tech.mogami.facilitator.dto.blockchain.AddressDto;

/**
 * Service for managing participants.
 */
public interface ParticipantService {

    /**
     * Retrieves an existing address or creates a new one if it does not exist.
     *
     * @param address the address string
     * @return the Address entity
     */
    AddressDto getOrCreateAddress(@NotNull @BlockchainAddress String address);

}
