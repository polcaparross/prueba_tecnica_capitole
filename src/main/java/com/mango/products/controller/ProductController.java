package com.mango.products.controller;

import com.mango.products.dto.*;
import com.mango.products.service.PriceService;
import com.mango.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final PriceService priceService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/prices")
    public ResponseEntity<PriceResponse> createPrice(
            @PathVariable Long id,
            @Valid @RequestBody PriceRequest request) {
        return new ResponseEntity<>(priceService.createPrice(id, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/prices")
    public ResponseEntity<?> getPrices(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(priceService.getPriceAtDate(id, date));
        } else {
            return ResponseEntity.ok(priceService.getPriceHistory(id));
        }
    }
}
