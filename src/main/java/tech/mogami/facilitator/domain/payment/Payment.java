package tech.mogami.facilitator.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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
import tech.mogami.commons.payment.PaymentStatus;
import tech.mogami.facilitator.domain.blockchain.Address;
import tech.mogami.facilitator.domain.util.base.BaseTenantEntity;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;
import static tech.mogami.commons.constant.BlockchainConstants.ATOMIC_AMOUNT_TYPE_PRECISION;
import static tech.mogami.commons.payment.PaymentStatus.PENDING;

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

    /** Asset amount (in atomic units). */
    @Column(name = "ASSET_AMOUNT", precision = ATOMIC_AMOUNT_TYPE_PRECISION, nullable = false)
    private BigInteger assetAmount;

    /** Asset contract. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_CONTRACT_ADDRESS_ID", nullable = false)
    private Address assetContract;

    /** Network name. */
    @Column(name = "NETWORK_NAME", nullable = false)
    private String networkName;

    /** Payment status. */
    @Builder.Default
    @Enumerated(STRING)
    @Column(name = "STATUS", nullable = false)
    private PaymentStatus status = PENDING;

    /** Payment steps associated with this payment. */
    @Builder.Default
    @OneToMany(mappedBy = "payment", fetch = EAGER)
    @OrderBy("createdAt ASC")
    private List<PaymentStep> steps = new LinkedList<>();

    /**
     * Adds a payment step to this payment.
     *
     * @param step the payment step to add
     */
    public void addStep(final PaymentStep step) {
        this.steps.add(step);
        step.setPayment(this);
    }

}
