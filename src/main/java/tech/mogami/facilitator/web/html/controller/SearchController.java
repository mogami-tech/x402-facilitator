package tech.mogami.facilitator.web.html.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import tech.mogami.facilitator.service.data.PaymentService;
import tech.mogami.facilitator.web.html.util.BaseController;

import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tech.mogami.commons.web.GlobalModelAttributes.ERROR_MESSAGE_ATTRIBUTE;
import static tech.mogami.commons.web.GlobalModelAttributes.QUERY_ATTRIBUTE;
import static tech.mogami.facilitator.web.html.pages.HomePage.HOME_REDIRECT;
import static tech.mogami.facilitator.web.html.pages.PaymentPage.PAYMENT_BY_NONCE_URL;

/**
 * Search controller.
 */
@Controller
@RequiredArgsConstructor
public class SearchController extends BaseController {

    /** Message source. */
    private final MessageSource messageSource;

    /** Payment service. */
    private final PaymentService paymentService;

    /**
     * Search for a payment.
     *
     * @param model              model to add attributes to
     * @param locale             locale
     * @param redirectAttributes attributes for redirect
     * @param query              search query
     * @return page to display
     */
    @SuppressWarnings("SameReturnValue")
    public String search(final Model model,
                         final Locale locale,
                         final RedirectAttributes redirectAttributes,
                         @RequestParam final String query) {
        final String effectiveQuery = StringUtils.trimToNull(query);
        if (effectiveQuery == null) {
            // If no query is provided, we set an error message ========================================================
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTRIBUTE, messageSource.getMessage("search.error.noQuery", null, locale));
        } else {
            // We search for the payment ===============================================================================
            model.addAttribute(QUERY_ATTRIBUTE, effectiveQuery);
            if (!paymentService.existsByPaymentId(effectiveQuery)) {
                // No payment found, we set an error message ===========================================================
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTRIBUTE, messageSource.getMessage(
                        "search.error.noResult",
                        new Object[]{effectiveQuery},
                        locale
                ));
            } else {
                // We found a payment, we redirect to its page =========================================================
                return "redirect:" + PAYMENT_BY_NONCE_URL.replace("{nonce}", UriUtils.encodePathSegment(effectiveQuery, UTF_8));
            }
        }
        return HOME_REDIRECT;
    }

}
