package tech.mogami.facilitator.configuration;

import lombok.RequiredArgsConstructor;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.DAYS;

/**
 * Web configuration.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    /** Default cache duration. */
    public static final CacheControl DEFAULT_CACHE_DURATION = CacheControl.maxAge(1, DAYS);

    /**
     * Add Thymeleaf Layout Dialect support.
     *
     * @return Thymeleaf template engine
     */
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    /**
     * Message source for i18n.
     *
     * @return message source
     */
    @Bean
    @Primary
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/i18n/errors",
                "classpath:/i18n/html",
                "classpath:/i18n/generic",
                "classpath:/i18n/home",
                "classpath:/i18n/search",
                "classpath:/i18n/payment"
        );
        messageSource.setDefaultEncoding(UTF_8.name());
        return messageSource;
    }

    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/favicon/**")
                .addResourceLocations("classpath:/static/images/favicon/")
                .setCacheControl(DEFAULT_CACHE_DURATION);

        registry.addResourceHandler("/images/logo/**")
                .addResourceLocations("classpath:/static/images/logo/")
                .setCacheControl(DEFAULT_CACHE_DURATION);
    }

}
