package com.mango.products.controller;

import com.mango.products.dto.*;
import com.mango.products.service.PriceService;
import com.mango.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Products", description = "Operations related to products and their historical prices")
public class ProductController {

    private final ProductService productService;
    private final PriceService priceService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/prices")
    @Operation(summary = "Add a price to a product")
    public ResponseEntity<PriceResponse> createPrice(
            @PathVariable Long id,
            @Valid @RequestBody PriceRequest request) {
        return new ResponseEntity<>(priceService.createPrice(id, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/prices")
    @Operation(summary = "Get current price or price history",
               description = "If 'date' is provided, returns the price for that date. Otherwise, returns the full history.")
    @ApiResponse(responseCode = "200", description = "Success",
                 content = { @Content(mediaType = "application/json",
                 schema = @Schema(oneOf = {PriceValueResponse.class, ProductHistoryResponse.class})) })
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
