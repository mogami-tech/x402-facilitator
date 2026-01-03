package tech.mogami.facilitator.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.network.Networks;
import tech.mogami.commons.constant.version.X402Version;
import tech.mogami.commons.constant.version.X402Versions;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(nullValuePropertyMappingStrategy = IGNORE, uses = {AddressMapper.class})
public interface PaymentMapper {

    @Named("networkFromName")
    static Network mapNetworkFromName(final String name) {
        return Networks.findByName(name).orElse(null);
    }

    @Named("x402VersionFromString")
    static X402Version mapX402VersionFromString(final String version) {
        return X402Versions.findByVersion(version).orElse(null);
    }

    @Mapping(target = "version", source = "x402Version", qualifiedByName = "x402VersionFromString")
    @Mapping(target = "fromAddress", source = "from")
    @Mapping(target = "toAddress", source = "to")
    @Mapping(target = "network", source = "networkName", qualifiedByName = "networkFromName")
    @Mapping(target = "steps", source = "steps")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "createdAt", source = "createdAt")
    PaymentStepDto toDto(PaymentStep step);

}
