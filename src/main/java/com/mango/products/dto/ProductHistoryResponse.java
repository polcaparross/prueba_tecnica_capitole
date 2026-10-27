package com.mango.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistoryResponse {

    private String name;
    private String description;
    private List<PriceResponse> prices;
}
