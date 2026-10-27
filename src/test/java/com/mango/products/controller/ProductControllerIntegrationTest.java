package com.mango.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mango.products.dto.PriceRequest;
import com.mango.products.dto.ProductRequest;
import com.mango.products.model.Product;
import com.mango.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
    }

    @Test
    void productHappyPathIntegrationTest() throws Exception {
        // 1. Create Product
        ProductRequest productRequest = new ProductRequest("Camiseta basica", "Camiseta blanca de algodon");
        String productResponseJson = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Camiseta basica"))
                .andReturn().getResponse().getContentAsString();

        Long productId = objectMapper.readTree(productResponseJson).get("id").asLong();

        // 2. Add Price
        PriceRequest priceRequest = new PriceRequest(
                new BigDecimal("19.99"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );

        mockMvc.perform(post("/products/" + productId + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(priceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value(19.99));

        // 3. Get Price at Date
        mockMvc.perform(get("/products/" + productId + "/prices")
                .param("date", "2026-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(19.99));

        // 4. Get History
        mockMvc.perform(get("/products/" + productId + "/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camiseta basica"))
                .andExpect(jsonPath("$.prices.length()").value(is(1)));
    }

    @Test
    void getPriceNotFound() throws Exception {
        mockMvc.perform(get("/products/999/prices")
                .param("date", "2026-03-15"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPriceOverlapError() throws Exception {
        Product product = productRepository.save(new Product("Camiseta basica", "Camiseta blanca"));

        PriceRequest p1 = new PriceRequest(new BigDecimal("19.99"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        mockMvc.perform(post("/products/" + product.getId() + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated());

        PriceRequest p2 = new PriceRequest(new BigDecimal("24.99"), LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1));
        mockMvc.perform(post("/products/" + product.getId() + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("New price overlaps with an existing price range"));
    }
}
