package tech.mogami.facilitator.provider.web3j;

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

}
