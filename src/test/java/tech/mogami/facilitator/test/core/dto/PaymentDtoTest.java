package tech.mogami.facilitator.test.core.dto;


import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import tech.mogami.facilitator.dto.payment.PaymentDto;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentDto formated values tests")
public class PaymentDtoTest {

    @Test
    @DisplayName("Empty PaymentDto test")
    public void emptyPaymentDtoTest() {
        assertThat(PaymentDto.builder().paymentId("nonce001").build())
                .satisfies(paymentDto -> {
                    assertThat(paymentDto.paymentId()).isEqualTo("nonce001");
                    assertThat(paymentDto.formattedFrom()).isEqualTo("-");
                    assertThat(paymentDto.formattedTo()).isEqualTo("-");
                    assertThat(paymentDto.formattedAmount(Locale.FRENCH)).isEqualTo("-");
                    assertThat(paymentDto.formattedAssetContract()).isEqualTo("-");
                    assertThat(paymentDto.formattedNetwork()).isEqualTo("-");
                    assertThat(paymentDto.steps()).isNotNull();
                    assertThat(paymentDto.steps()).isEmpty();
                });
    }

}
