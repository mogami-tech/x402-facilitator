package tech.mogami.facilitator.test.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.parameter.X402Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Parameters tests")
public class ParametersTest {

    @Autowired
    private X402Parameters x402Parameters;

    @Test
    @DisplayName("Facilitator parameters")
    void facilitatorParameters() {
        assertThat(x402Parameters.facilitator().privateKey())
                .isEqualTo("0xc1f97668293dcaecb72bfc6fba31a39d34a1a5d1d3d36e30f237a9cbcb3077e9");
    }

}
