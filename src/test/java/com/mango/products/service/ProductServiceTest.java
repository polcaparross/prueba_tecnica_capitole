package com.mango.products.service;

import com.mango.products.dto.ProductRequest;
import com.mango.products.dto.ProductResponse;
import com.mango.products.model.Product;
import com.mango.products.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProductWithResponse() {
        ProductRequest request = new ProductRequest("Camiseta blanca", "Camiseta basica");
        Product savedProduct = new Product("Camiseta blanca", "Camiseta basica");
        savedProduct.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Camiseta blanca", response.getName());
        assertEquals("Camiseta basica", response.getDescription());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getProductWithResponse() {
        Long productId = 1L;
        Product product = new Product("Pantalon azul", "Pantalon vaquero");
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(productId);

        assertNotNull(response);
        assertEquals(productId, response.getId());
        assertEquals("Pantalon azul", response.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductExceptionNotExist() {
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProduct(productId));
        verify(productRepository, times(1)).findById(productId);
    }
}
