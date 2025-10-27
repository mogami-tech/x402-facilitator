package tech.mogami.facilitator.configuration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Error configuration for handling different HTTP errors.
 * This class defines the error pages for various HTTP status codes.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ErrorConfiguration implements ErrorController {

    /** Generic error page. */
    private static final String ERROR_GENERIC_VIEW = "util/errors/error";

    /** Error 403 page. */
    private static final String ERROR_403_VIEW = "util/errors/error-403";

    /** Error 404 page. */
    private static final String ERROR_404_VIEW = "util/errors/error-404";

    /** Error 500 page. */
    private static final String ERROR_500_VIEW = "util/errors/error-500";

    /**
     * Handle errors.
     *
     * @param request request
     * @return page to display
     */
    @RequestMapping("/error")
    public String handleError(final HttpServletRequest request) {
        Object status = request.getAttribute(ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = NumberUtils.toInt(status.toString(), INTERNAL_SERVER_ERROR.value());
            if (statusCode == FORBIDDEN.value()) {
                log.error("Error 403: Forbidden access to {}", request.getRequestURI());
                return ERROR_403_VIEW;
            }
            if (statusCode == NOT_FOUND.value()) {
                log.error("Error 404: Page not found");
                return ERROR_404_VIEW;
            }
            if (statusCode == INTERNAL_SERVER_ERROR.value()) {
                log.error("Error 500: Server error when accessing {}", request.getRequestURI());
                return ERROR_500_VIEW;
            }
        }
        log.error("An unexpected error occurred with url {}", request.getRequestURI());
        return ERROR_GENERIC_VIEW;
    }

}