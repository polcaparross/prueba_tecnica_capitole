package com.mango.products.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRequest {

    @NotNull(message = "Price value is required")
    @Positive(message = "Price value must be greater than zero")
    private BigDecimal value;

    @NotNull(message = "initDate is required")
    private LocalDate initDate;

    private LocalDate endDate;
}
