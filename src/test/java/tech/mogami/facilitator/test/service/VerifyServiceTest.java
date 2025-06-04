package tech.mogami.facilitator.test.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.service.VerifyService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Verify Service Tests")
public class VerifyServiceTest {

    @Autowired
    private VerifyService verifyService;

    @Test
    @DisplayName("Empty request")
    public void testEmptyRequest() {
        assertThat(verifyService.verify(null))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("undefined");
                    assertThat(result.payer()).isNull();
                });
    }

}
