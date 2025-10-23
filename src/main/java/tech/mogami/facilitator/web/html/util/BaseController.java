package tech.mogami.facilitator.web.html.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.ui.Model;
import tech.mogami.commons.web.Page;

/**
 * Base controller.
 */
public class BaseController {

    /** HTMX request header. */
    public static final String HTMX_REQUEST = "HX-Request";

    /**
     * Get (and configure) the page to display.
     *
     * @param model   model
     * @param request request
     * @param page    page
     * @return page to display
     */
    protected final String getPage(@NonNull final Model model,
                                   @NonNull final HttpServletRequest request,
                                   @NonNull final Page page) {
        // Returns the view to display (page or page fragment).
        if (request.getHeader(HTMX_REQUEST) != null) {
            // HTMX_REQUEST header is present, return only the page fragment.
            return page.viewFragment();
        } else {
            return page.view();
        }
    }

}
