package tech.mogami.facilitator.test.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.mogami.facilitator.dto.blockchain.AddressDto;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;

@DisplayName("AddressDTO tests")
public class AddressDTOTest {

    @Test
    @DisplayName("Shorten address")
    public void shortenAddress() {
        // Test with null.
        assertThat(AddressDto.builder().build())
                .satisfies(address -> assertThat(address.shortenAddress()).isNull());

        // Normal behavior.
        assertThat(AddressDto.builder().address(TEST_CLIENT_WALLET_ADDRESS_1).build())
                .satisfies(address -> assertThat(address.shortenAddress()).isEqualTo("0xf6b...F6E"));
    }

}
