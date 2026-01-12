package tech.mogami.facilitator.provider.web3j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;

import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;

/**
 * Mocked {@link ContractService} implementation.
 */
@Slf4j
@Service
@Profile("mockedBlockchain")
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class MockedContractServiceImplementation implements ContractService {

    @Override
    public ContractCallResult transferWithAuthorization(final @NonNull SettlementRequest settlementRequest) {
        final String to = settlementRequest.getTo().orElseThrow(() -> new IllegalArgumentException("To address is required"));
        if (TEST_CLIENT_WALLET_ADDRESS_1.equalsIgnoreCase(to)) {
            return ContractCallResult.failure("Mocked transaction failed: impossible to connect to the network");
        } else {
            return ContractCallResult.success("Mocked transaction successful", "0xMOCKEDTRANSACTIONHASH");
        }
    }

}
