package com.mango.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {

    private BigDecimal value;
    private LocalDate initDate;
    private LocalDate endDate;
}
