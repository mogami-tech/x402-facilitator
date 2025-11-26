package tech.mogami.facilitator.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.mogami.facilitator.domain.blockchain.Address;
import tech.mogami.facilitator.dto.blockchain.AddressDto;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(nullValuePropertyMappingStrategy = IGNORE)
public interface AddressMapper {

    AddressDto toDto(Address address);

    //@Mapping(target = "id", source = "id")
    @Mapping(target = "participant", ignore = true)
    Address toEntity(AddressDto dto);

}
