package tech.mogami.facilitator.web.html.pages;

import lombok.experimental.UtilityClass;
import tech.mogami.commons.web.Page;

/**
 * Payment page definition.
 */
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class PaymentPage {

    /** Payment by nonce page URL. */
    public static final String PAYMENT_BY_NONCE_URL = "/payments/by-nonce/{nonce}";

    /** Payment page. */
    public static final Page PAYMENT_BY_NONCE_PAGE = Page.builder()
            .name("payment")
            .url(PAYMENT_BY_NONCE_URL).view("payment")
            .title("payment.title").description("payment.description")
            .build();

}
