package tech.mogami.facilitator.provider.web3j;

/**
 * Result of a contract call.
 *
 * @param success         whether the contract call was successful
 * @param status          status message
 * @param transactionHash hash of the transaction
 */
public record ContractCallResult(
        boolean success,
        String status,
        String transactionHash
) {

    /**
     * Factory for a successful contract call.
     */
    public static ContractCallResult success(final String status, final String transactionHash) {
        return new ContractCallResult(true, status, transactionHash);
    }

    /**
     * Factory for a failed contract call with an explicit status.
     */
    public static ContractCallResult failure(final String status) {
        return new ContractCallResult(false, status, null);
    }

    /**
     * Gets the error message if the call was not successful.
     *
     * @return the error message, or null if the call was successful
     */
    public String errorMessage() {
        if (!success) {
            return status;
        } else {
            return null;
        }
    }

}
