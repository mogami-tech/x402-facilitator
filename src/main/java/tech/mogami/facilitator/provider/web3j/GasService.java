package tech.mogami.facilitator.provider.web3j;

import org.web3j.tx.gas.ContractGasProvider;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.crypto.gas.GasFees;

/**
 * Centralized gas-fee service.
 * Refreshes all network gas prices on a fixed schedule (every X seconds)
 * and provides fast, cached access to current values.
 */
public interface GasService {

    /**
     * Get the current gas fees for a given network.
     *
     * @param networkName the network name to get the gas fee for
     * @return the current gas fee for the given network
     */
    GasFees getGasFees(String networkName);

    /**
     * Get the current gas provider for a given network.
     *
     * @param network the network to get the gas provider for
     * @return the current gas provider for the given network
     */
    ContractGasProvider getGasProvider(Network network);

}
