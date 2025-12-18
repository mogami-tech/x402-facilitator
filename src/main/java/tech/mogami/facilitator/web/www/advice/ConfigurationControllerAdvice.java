package tech.mogami.facilitator.web.www.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import tech.mogami.facilitator.service.facilitator.SupportedService;

import static tech.mogami.facilitator.web.www.util.ModelAttribute.FACILITATOR_ADDRESS_ATTRIBUTE;
import static tech.mogami.facilitator.web.www.util.ModelAttribute.SUPPORTED_KINDS_ATTRIBUTE;

/**
 * Controller advice used to distribute configuration to all controllers.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ConfigurationControllerAdvice {

    /** Facilitator address. */
    private final String facilitatorAddress;

    /** Supporter service. */
    private final SupportedService supportedService;

    @ModelAttribute
    public final void handleRequest(final Model model) {
        model.addAttribute(SUPPORTED_KINDS_ATTRIBUTE, supportedService.supported());
        model.addAttribute(FACILITATOR_ADDRESS_ATTRIBUTE, facilitatorAddress);
    }

}
