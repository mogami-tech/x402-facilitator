package tech.mogami.facilitator.web.html.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.mogami.facilitator.service.facilitator.SupportedService;
import tech.mogami.facilitator.web.html.util.BaseController;

import static tech.mogami.facilitator.web.html.pages.HomePage.HOME;
import static tech.mogami.facilitator.web.html.pages.HomePage.HOME_URL;
import static tech.mogami.facilitator.web.html.util.ModelAttribute.FACILITATOR_ADDRESS;
import static tech.mogami.facilitator.web.html.util.ModelAttribute.SUPPORTED_KINDS;

/**
 * Home controller is the controller for the home page of the Mogami Facilitator web application.
 */
@Controller
@RequiredArgsConstructor
public class HomeController extends BaseController {

    /** Facilitator address. */
    private final String facilitatorAddress;

    /** Supporter service. */
    private final SupportedService supportedService;

    /**
     * Page displaying home.
     *
     * @param model              model to add attributes to
     * @param request            request
     * @param redirectAttributes attributes for redirect
     * @param query              search query
     * @return page to display
     */
    @GetMapping(HOME_URL)
    @SuppressWarnings("SameReturnValue")
    public String home(final Model model,
                       final HttpServletRequest request,
                       final RedirectAttributes redirectAttributes,
                       @RequestParam(required = false) final String query) {
        // We add configuration information to display on the page.
        model.addAttribute(SUPPORTED_KINDS, supportedService.supported());
        model.addAttribute(FACILITATOR_ADDRESS, facilitatorAddress);

        return getPage(model, request, HOME);
    }

}
