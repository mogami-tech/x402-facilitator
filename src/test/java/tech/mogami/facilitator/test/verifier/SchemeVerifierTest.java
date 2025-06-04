package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.verifier.general.SchemeVerifier;

@SpringBootTest
@DisplayName("Scheme Verifier Tests")
public class SchemeVerifierTest {

    @Autowired
    private SchemeVerifier schemeVerifier;

    @Test
    @DisplayName("Scheme not set")
    public void testSchemeNotSet() {

    }


}
