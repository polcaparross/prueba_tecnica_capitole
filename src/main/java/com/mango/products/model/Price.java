package com.mango.products.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "price_value", nullable = false)
    private BigDecimal value;

    @Column(name = "init_date", nullable = false)
    private LocalDate initDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public Price(Product product, BigDecimal value, LocalDate initDate, LocalDate endDate) {
        this.product = product;
        this.value = value;
        this.initDate = initDate;
        this.endDate = endDate;
    }
}
