package tech.mogami.facilitator.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.network.Networks;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(nullValuePropertyMappingStrategy = IGNORE, uses = {AddressMapper.class})
public interface PaymentMapper {

    @Named("networkFromName")
    static Network mapNetworkFromName(String name) {
        return Networks.findByName(name).orElse(null);
    }

    @Mapping(target = "fromAddress", source = "from")
    @Mapping(target = "toAddress", source = "to")
    @Mapping(target = "network", source = "networkName", qualifiedByName = "networkFromName")
    PaymentDto toDto(Payment payment);

    PaymentStepDto toDto(PaymentStep step);

    PaymentStep toEntity(PaymentStepDto dtoStep);

}
