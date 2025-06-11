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
    @DisplayName("X402Parameters values")
    void x402ParametersValuesTest() {
        assertThat(x402Parameters.facilitator().privateKey())
                .isEqualTo("0xf4f7e165433421377856179c698aa387bd8f872657977bd8fa6d62604f41773c");
    }

}
