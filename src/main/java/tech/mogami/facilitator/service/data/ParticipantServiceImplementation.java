package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tech.mogami.facilitator.domain.blockchain.Address;
import tech.mogami.facilitator.dto.blockchain.AddressDto;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.util.base.Base;

/**
 * {@link ParticipantService} implementation.
 */
@Slf4j
@Service
@Validated
@Transactional
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class ParticipantServiceImplementation extends Base implements ParticipantService {

    /** Address repository. */
    private final AddressRepository addressRepository;

    @Override
    public AddressDto getOrCreateAddress(final String address) {
        log.info("Creating or retrieving address: {}", address);
        // We try to find the address.
        Address addressRetrieved = addressRepository.findByAddress(address)
                // If not found, we create it.
                .orElseGet(() -> {
                    try {
                        return addressRepository.save(Address.builder()
                                .address(address)
                                .build());
                    } catch (DataIntegrityViolationException e) {
                        // Seems someone else created it in between, so we try again to find it.
                        return addressRepository.findByAddress(address)
                                .orElseThrow(() -> new IllegalStateException("Address should exist"));
                    }
                });
        return ADDRESS_MAPPER.toDto(addressRetrieved);
    }

}
