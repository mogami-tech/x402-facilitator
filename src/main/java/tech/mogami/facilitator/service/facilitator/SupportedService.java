package tech.mogami.facilitator.service.facilitator;

import tech.mogami.commons.api.facilitator.supported.SupportedResponse;

/**
 * Supported service interface.
 */
public interface SupportedService {

    /**
     * Returns supported payment schemes and networks.
     *
     * @return SupportedResponse
     */
    SupportedResponse supported();

}
