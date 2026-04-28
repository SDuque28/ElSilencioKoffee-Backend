package ElSilencioKoffee_Backend.production.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "production")
@Getter
@Setter
@NoArgsConstructor
public class Production {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_production")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_section", nullable = false)
    private ProductionSection section;

    @Column(name = "date_collection", nullable = false)
    private LocalDate collectionDate;

    @Column(name = "quantity_kg", precision = 10, scale = 2)
    private BigDecimal quantityKg;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_variety", nullable = false)
    private ProductionVariety variety;
}
