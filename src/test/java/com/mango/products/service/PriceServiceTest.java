package com.mango.products.service;

import com.mango.products.dto.PriceRequest;
import com.mango.products.dto.PriceResponse;
import com.mango.products.dto.PriceValueResponse;
import com.mango.products.model.Price;
import com.mango.products.model.Product;
import com.mango.products.repository.PriceRepository;
import com.mango.products.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PriceService priceService;

    @Test
    void createPrice() {
        Long productId = 1L;
        Product product = new Product("Camiseta blanca", "Camiseta basica");
        product.setId(productId);

        PriceRequest request = new PriceRequest(
                new BigDecimal("19.99"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );

        Price savedPrice = new Price(
                product,
                request.getValue(),
                request.getInitDate(),
                request.getEndDate()
        );
        savedPrice.setId(1L);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(priceRepository.save(any(Price.class))).thenReturn(savedPrice);

        PriceResponse response = priceService.createPrice(productId, request);

        assertNotNull(response);
        assertEquals(request.getValue(), response.getValue());
        assertEquals(request.getInitDate(), response.getInitDate());
        assertEquals(request.getEndDate(), response.getEndDate());
        verify(productRepository, times(1)).findById(productId);
        verify(priceRepository, times(1)).save(any(Price.class));
    }

    @Test
    void getPriceAtDate() {
        Long productId = 1L;
        LocalDate date = LocalDate.of(2026, 4, 15);
        Product product = new Product("Camiseta blanca", "Camiseta basica");
        product.setId(productId);

        Price price = new Price(
                product,
                new BigDecimal("19.99"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30)
        );

        when(priceRepository.findByProductIdAndDate(productId, date)).thenReturn(Optional.of(price));

        PriceValueResponse response = priceService.getPriceAtDate(productId, date);

        assertNotNull(response);
        assertEquals(price.getValue(), response.getValue());
        verify(priceRepository, times(1)).findByProductIdAndDate(productId, date);
    }
}
