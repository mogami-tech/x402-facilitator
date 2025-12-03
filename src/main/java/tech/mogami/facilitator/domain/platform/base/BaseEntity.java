package tech.mogami.facilitator.domain.platform.base;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Base entity class to be extended by other entities.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {

    /** Unique identifier. */
    @Id
    @Column(name = "ID", updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

}
