package tech.mogami.facilitator.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration.
 */
@Configuration
public class OpenAPIConfiguration {

    /**
     * Custom OpenAPI bean for the Mogami Facilitator API.
     *
     * @return OpenAPI instance with API information
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mogami x402 Facilitator API")
                        .description("""
                                Verify and settle x402 stablecoin payments instantly.
                                The Mogami Facilitator powers seamless monetization for APIs, apps, and AI agents —
                                enabling instant, frictionless payments over HTTP 402.
                                
                                Built on the open x402 protocol — no accounts, no cards, no subscriptions.
                                Just seamless value exchange for humans and machines.
                                """)
                        .license(new License()
                                .name("AGPL-3.0 license")
                                .url("https://github.com/mogami-tech/x402-facilitator?tab=AGPL-3.0-1-ov-file"))
                        .contact(new Contact()
                                .name("Mogami")
                                .url("https://www.mogami.tech")
                                .email("contact@mogami.tech")));
    }

}
