package tech.mogami.facilitator.provider.web3j;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DynamicEIP1559GasProvider;
import tech.mogami.commons.crypto.gas.GasFees;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tech.mogami.commons.constant.BlockchainConstants.DEFAULT_GAS_FEES;
import static tech.mogami.commons.constant.network.Networks.ALL_NETWORKS;

/**
 * {@link GasService} implementation.
 */
@Slf4j
@Service
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class GasServiceImplementation implements GasService {

    /** Refresh interval in milliseconds. */
    private static final String GAS_FEES_REFRESH_INTERVAL = "60000";

    /** Cache of gas fees per network. */
    private final Map<String, GasFees> cache = new ConcurrentHashMap<>();

    @Override
    public GasFees getGasFees(final String networkName) {
        return cache.getOrDefault(networkName, DEFAULT_GAS_FEES);
    }

    /**
     * Scheduled refresh of gas fees for all configured networks.
     * Runs every 60 seconds.
     */
    @Scheduled(fixedRateString = GAS_FEES_REFRESH_INTERVAL)
    public void refreshAll() {
        ALL_NETWORKS.forEach(network -> {
            try (Web3j web3j = Web3j.build(new HttpService(network.rpcUrl()))) {
                // Getting the latest block to fetch base fee ==================================================
                DynamicEIP1559GasProvider provider = new DynamicEIP1559GasProvider(web3j, network.chainId());
                GasFees newFees = new GasFees(provider.getMaxFeePerGas(), provider.getMaxPriorityFeePerGas());
                cache.put(network.name(), newFees);
                log.info("[GasService] Fetched gas fees for network {}: {}", network.name(), newFees);
            } catch (Exception e) {
                log.error("[GasService] Failed to fetch gas fees for network {}: {}", network.name(), e.getMessage());
            }
        });
    }

}
