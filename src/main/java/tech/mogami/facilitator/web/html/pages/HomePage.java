package tech.mogami.facilitator.web.html.pages;

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

    /** Home page. */
    public static final Page HOME = Page.builder()
            .name("home")
            .url(HOME_URL).view("home")
            .title("home.title").description("home.description")
            .build();

}
