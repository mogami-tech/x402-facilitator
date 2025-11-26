package tech.mogami.facilitator.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(nullValuePropertyMappingStrategy = IGNORE, uses = {AddressMapper.class})
public interface PaymentMapper {

    @Mapping(target = "fromAddress", source = "from")
    PaymentDto toDto(Payment payment);

    Payment toEntity(PaymentDto dto);

    PaymentStepDto toDto(PaymentStep step);

    PaymentStep toEntity(PaymentStepDto dtoStep);

}
