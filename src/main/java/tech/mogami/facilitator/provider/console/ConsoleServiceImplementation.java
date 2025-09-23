package tech.mogami.facilitator.provider.console;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tech.mogami.commons.api.console.v1.EventRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static tech.mogami.commons.api.console.ConsoleApiEndpoints.V1.EVENTS_URL;
import static tech.mogami.commons.api.console.ConsoleApiEndpoints.V1_PREFIX;

/**
 * {@link ConsoleService} implementation.
 */
@Slf4j
@Service
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class ConsoleServiceImplementation implements ConsoleService {

    /** Web client. */
    private final WebClient client;

    /**
     * Default constructor.
     */
    public ConsoleServiceImplementation() {
        this.client = WebClient.builder()
                .baseUrl(V1_PREFIX)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .build();
    }

    @Override
    public final void logEvent(final EventRequest event) {
        log.info("{}: Logging payment with nonce {}", event.type(), event.nonce());
        try {
            String eventId = client.post()
                    .uri(EVENTS_URL)
                    .contentType(APPLICATION_JSON)
                    .bodyValue(event)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("{}: Event logged successfully for event id {}", event.type(), eventId);
        } catch (Exception e) {
            log.error("{}: Failed to log event: {}", event.type(), e.getMessage(), e);
        }
    }

}
