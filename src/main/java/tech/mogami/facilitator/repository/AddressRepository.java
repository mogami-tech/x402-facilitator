package tech.mogami.facilitator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.mogami.facilitator.domain.blockchain.Address;

import java.util.Optional;

/**
 * Repository interface for managing {@link Address} entities.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Finds an address by its unique address string.
     *
     * @param address the unique address string
     * @return an Optional containing the found Address, or empty if not found
     */
    Optional<Address> findByAddress(String address);

}
