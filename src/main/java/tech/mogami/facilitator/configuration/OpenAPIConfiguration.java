package tech.mogami.facilitator.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;

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
                        .title("Mogami x402 Facilitator API - X402 " + X402_SUPPORTED_VERSION_BY_MOGAMI.label())
                        .description("""
                                The Mogami Facilitator Server acts as your payment gateway for x402.
                                You can run it as a Docker image within your own infrastructure, or use our always-on hosted facilitator online.
                                It verifies, authorizes, and settles x402 transactions in real time, connecting your services to the blockchain securely while keeping private keys safe.
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
