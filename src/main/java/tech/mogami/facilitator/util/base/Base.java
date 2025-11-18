package tech.mogami.facilitator.util.base;

import org.mapstruct.factory.Mappers;
import tech.mogami.facilitator.util.mapper.AddressMapper;
import tech.mogami.facilitator.util.mapper.PaymentMapper;

/**
 * Global base class.
 */
public abstract class Base {

    /** Payment mapper. */
    protected static final PaymentMapper PAYMENT_MAPPER = Mappers.getMapper(PaymentMapper.class);

    /** Address mapper. */
    protected static final AddressMapper ADDRESS_MAPPER = Mappers.getMapper(AddressMapper.class);

}
