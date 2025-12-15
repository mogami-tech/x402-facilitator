package tech.mogami.facilitator.web.www.pages;

import lombok.experimental.UtilityClass;
import tech.mogami.commons.web.Page;

/**
 * Home page definition.
 */
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class HomePage {

    /** Home page URL. */
    public static final String HOME_URL = "/";

    /** Search URL. */
    public static final String SEARCH_URL = "/search";

    /** Home page redirect URL. */
    public static final String HOME_REDIRECT = "redirect:/";

    /** Home page. */
    public static final Page HOME_PAGE = Page.builder()
            .name("home")
            .url(HOME_URL).view("home")
            .title("home.title").description("home.description")
            .build();

}
