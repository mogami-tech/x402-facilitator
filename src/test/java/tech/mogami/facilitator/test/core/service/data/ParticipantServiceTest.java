package tech.mogami.facilitator.test.core.service.data;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.service.data.ParticipantService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@DisplayName("Participant service tests")
public class ParticipantServiceTest {

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    ParticipantService participantService;

    @Test
    @DisplayName("Test get or create address")
    public void testGetOrCreateAddress() {

        // Invalid addresses ===========================================================================================
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> participantService.getOrCreateAddress(null));
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> participantService.getOrCreateAddress("INVALID_ADDRESS"));

        // Valid address ===============================================================================================
        long numberOfAddressBeforeCreation = addressRepository.count();
        // We create the address.
        assertThat(participantService.getOrCreateAddress("0x1234567890abcdef1234567890abcdef12345678"))
                .satisfies(addressDto -> {
                    assertThat(addressDto.id()).isNotNull();
                    assertThat(addressDto.address()).isEqualTo("0x1234567890abcdef1234567890abcdef12345678");
                });
        // We get it again, so no new address is created.
        assertThat(participantService.getOrCreateAddress("0x1234567890abcdef1234567890abcdef12345678"))
                .satisfies(addressDto -> {
                    assertThat(addressDto.id()).isNotNull();
                    assertThat(addressDto.address()).isEqualTo("0x1234567890abcdef1234567890abcdef12345678");
                });
        // We check that only one address was created.
        assertThat(addressRepository.count()).isEqualTo(numberOfAddressBeforeCreation + 1);

        // We create another address.
        assertThat(participantService.getOrCreateAddress("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd"))
                .satisfies(addressDto -> {
                    assertThat(addressDto.id()).isNotNull();
                    assertThat(addressDto.address()).isEqualTo("0xabcdefabcdefabcdefabcdefabcdefabcdefabcd");
                });
        // We check that only one more address was created.
        assertThat(addressRepository.count()).isEqualTo(numberOfAddressBeforeCreation + 2);
    }

}
