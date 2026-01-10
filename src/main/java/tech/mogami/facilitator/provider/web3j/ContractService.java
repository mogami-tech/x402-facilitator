package tech.mogami.facilitator.provider.web3j;

import org.jspecify.annotations.NonNull;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;

/**
 * Service for interacting with smart contracts on the blockchain.
 */
public interface ContractService {

    /**
     * Executes a transfer with authorization on the blockchain.
     *
     * @param settlementRequest the settlement request containing transfer details
     * @return the result of the contract call
     */
    ContractCallResult transferWithAuthorization(@NonNull SettlementRequest settlementRequest);

}
