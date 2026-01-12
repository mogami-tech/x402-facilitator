package tech.mogami.facilitator.test.core.blockchain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.provider.web3j.GasService;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static tech.mogami.commons.constant.BlockchainConstants.DEFAULT_GAS_FEES;
import static tech.mogami.commons.constant.network.Networks.BASE_MAINNET;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;

@SpringBootTest
@DisplayName("Gas service tests")
public class GasServiceTest {

    @Autowired
    GasService gasService;

    @Test
    @DisplayName("Gas service fees update")
    void gasServiceFeesUpdate() {
        assertThat(gasService.getGasFees("UNKNOWN_NETWORK")).isEqualTo(DEFAULT_GAS_FEES);
        await().atMost(1, MINUTES).until(() -> gasService.getGasFees(BASE_SEPOLIA.name()) != DEFAULT_GAS_FEES);
        await().atMost(1, MINUTES).until(() -> gasService.getGasFees(BASE_MAINNET.name()) != DEFAULT_GAS_FEES);
    }

}
