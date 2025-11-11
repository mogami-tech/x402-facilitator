package tech.mogami.facilitator.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.mogami.facilitator.domain.blockchain.Address;
import tech.mogami.facilitator.domain.util.BaseTenantEntity;

import java.util.List;

import static jakarta.persistence.FetchType.EAGER;

/**
 * Represents a payment.
 */
@Entity
@Table(name = "PAYMENT")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseTenantEntity {

    /** Unique payment id (usually the nonce). */
    @Column(name = "PAYMENT_ID", nullable = false, unique = true, updatable = false)
    private String paymentId;

    /** The address from which the payment is made. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_FROM_ADDRESS_ID", nullable = false)
    private Address from;

    /** The address to which the payment is made. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_TO_ADDRESS_ID", nullable = false)
    private Address to;

    /** Payment steps associated with this payment. */
    @OneToMany(mappedBy = "payment", fetch = EAGER)
    @OrderBy("timestamp ASC")
    private List<PaymentStep> steps;


}
