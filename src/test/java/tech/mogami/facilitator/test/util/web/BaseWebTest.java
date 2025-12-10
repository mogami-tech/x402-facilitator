package tech.mogami.facilitator.test.util.web;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static tech.mogami.facilitator.web.www.util.BaseController.HTMX_REQUEST;

/**
 * Utility classes for tests.
 */
public class BaseWebTest {

    /**
     * Method used by tests to test htmx and non htmx methods.
     *
     * @return headers to test
     */
    protected static Stream<Arguments> headers() {
        return Stream.of(
                // Normal call.
                Arguments.of(new HttpHeaders()),
                // HTMX call.
                Arguments.of(getHTMXHeaders())
        );
    }

    /**
     * Method used by tests to test htmx and non htmx methods.
     *
     * @return headers to test
     */
    protected static HttpHeaders getHTMXHeaders() {
        final HttpHeaders htmxHeaders = new HttpHeaders();
        htmxHeaders.add(HTMX_REQUEST, "true");
        return htmxHeaders;
    }

    /**
     * Asserts that the element with the given ID exists in the page.
     *
     * @param page      the page to check
     * @param elementId the ID of the element to check
     */
    public void assertElementExists(
            final Document page,
            final String elementId) {
        assertThat(page.getElementById(elementId))
                .as("checking that element with ID '%s' exists", elementId)
                .isNotNull();
    }

    /**
     * Asserts that the element with the given ID exists in the page and contains the expected value.
     *
     * @param page          the page to check
     * @param elementId     the ID of the element to check
     * @param expectedValue the expected value of the element
     */
    public void assertElementValue(
            final Document page,
            final String elementId,
            final String expectedValue) {
        assertThat(page.getElementById(elementId))
                .as("checking that element with ID '%s' exists with value '%s'", elementId, expectedValue)
                .isNotNull()
                .extracting(Element::text)
                .asString()
                .contains(expectedValue);
    }

    /**
     * Returns the message for the given key.
     *
     * @param messageSource message source
     * @param key           key
     * @return message translated in the current locale
     */
    protected String getMessage(MessageSource messageSource, String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

}

