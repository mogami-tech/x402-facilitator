package tech.mogami.facilitator.web.www.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.mogami.commons.util.NonceUtil;
import tech.mogami.facilitator.parameter.X402Parameters;
import tech.mogami.facilitator.web.www.util.BaseController;

import java.util.Optional;

import static tech.mogami.commons.web.GlobalModelAttributes.QUERY_ATTRIBUTE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_PAGE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_URL;

/**
 * Home controller is the controller for the home page of the Mogami Facilitator web application.
 */
@Controller
@RequiredArgsConstructor
public class HomeController extends BaseController {

    /** X402 Parameters. */
    private final X402Parameters x402Parameters;

    /**
     * Page displaying home.
     *
     * @param model              model to add attributes to
     * @param request            request
     * @param redirectAttributes attributes for redirect
     * @param query              search query
     * @return page to display
     */
    @SuppressWarnings("SameReturnValue")
    @GetMapping(HOME_URL)
    public String home(final Model model,
                       final HttpServletRequest request,
                       final RedirectAttributes redirectAttributes,
                       @RequestParam(required = false) final String query) {
        // Example nonce ===============================================================================================
        var exampleNonce = Optional.ofNullable(x402Parameters.examples())
                .map(X402Parameters.Examples::nonce)
                .orElse(null);
        model.addAttribute("exampleNonce", exampleNonce);
        model.addAttribute("shortenExampleNonce", NonceUtil.shortenNonce(exampleNonce));

        // We either retrieve the query from the request or from the redirect attributes ===============================
        final String queryValue = StringUtils.defaultIfBlank(query, (String) redirectAttributes.getFlashAttributes().get(QUERY_ATTRIBUTE));
        model.addAttribute(QUERY_ATTRIBUTE, StringUtils.trimToNull(queryValue));

        return getPage(model, request, HOME_PAGE);
    }

}
