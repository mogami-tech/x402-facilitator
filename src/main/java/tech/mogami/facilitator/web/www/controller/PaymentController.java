package tech.mogami.facilitator.web.www.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.mogami.facilitator.service.data.PaymentService;
import tech.mogami.facilitator.web.www.util.BaseController;

import java.util.Locale;

import static tech.mogami.commons.web.GlobalModelAttributes.ERROR_MESSAGE_ATTRIBUTE;
import static tech.mogami.commons.web.GlobalModelAttributes.QUERY_ATTRIBUTE;
import static tech.mogami.commons.web.GlobalModelAttributes.RESULT_ATTRIBUTE;
import static tech.mogami.facilitator.web.www.pages.HomePage.HOME_REDIRECT;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_PAGE;
import static tech.mogami.facilitator.web.www.pages.PaymentPage.PAYMENT_BY_NONCE_URL;

/**
 * Payment controller.
 */
@Controller
@RequiredArgsConstructor
public class PaymentController extends BaseController {

    /** Message source. */
    private final MessageSource messageSource;

    /** Payment service. */
    private final PaymentService paymentService;

    /**
     * Page displaying payment by nonce.
     *
     * @param model              model to add attributes to
     * @param locale             locale to use for messages
     * @param request            request
     * @param redirectAttributes redirect attributes
     * @param nonce              payment nonce
     * @return page to display
     */
    @SuppressWarnings("SameReturnValue")
    @GetMapping(value = {PAYMENT_BY_NONCE_URL, PAYMENT_BY_NONCE_URL + "/"})
    public String getPaymentByNonce(final Model model,
                                    final Locale locale,
                                    final HttpServletRequest request,
                                    final RedirectAttributes redirectAttributes,
                                    @PathVariable final String nonce) {
        final String effectiveNonce = StringUtils.trimToNull(nonce);
        return paymentService.searchByPaymentId(effectiveNonce)
                // Payment found =======================================================================================
                .map(payment -> {
                    // We now add the payment and each event type as a separate attribute.
                    model.addAttribute(RESULT_ATTRIBUTE, payment);
                    return getPage(model, request, PAYMENT_BY_NONCE_PAGE);
                })
                // Payment not found ===================================================================================
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(QUERY_ATTRIBUTE, effectiveNonce);
                    redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTRIBUTE, messageSource.getMessage(
                            "payment.error.notFound",
                            new Object[]{effectiveNonce},
                            locale
                    ));
                    return HOME_REDIRECT;
                });
    }

}
