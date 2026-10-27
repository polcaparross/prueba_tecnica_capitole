package com.mango.products.service;

import com.mango.products.dto.PriceRequest;
import com.mango.products.dto.PriceResponse;
import com.mango.products.dto.PriceValueResponse;
import com.mango.products.dto.ProductHistoryResponse;
import com.mango.products.model.Price;
import com.mango.products.model.Product;
import com.mango.products.repository.PriceRepository;
import com.mango.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceRepository priceRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PriceResponse createPrice(Long productId, PriceRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Price price = new Price(
                product,
                request.getValue(),
                request.getInitDate(),
                request.getEndDate()
        );

        Price savedPrice = priceRepository.save(price);
        return mapToPriceResponse(savedPrice);
    }

    @Transactional(readOnly = true)
    public PriceValueResponse getPriceAtDate(Long productId, LocalDate date) {
        Price price = priceRepository.findByProductIdAndDate(productId, date)
                .orElseThrow(() -> new RuntimeException("Price not found for product id: " + productId + " at date: " + date));
        return new PriceValueResponse(price.getValue());
    }

    @Transactional(readOnly = true)
    public ProductHistoryResponse getPriceHistory(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        List<Price> prices = priceRepository.findByProductIdOrderByInitDateAsc(productId);

        List<PriceResponse> priceResponses = prices.stream()
                .map(this::mapToPriceResponse)
                .collect(Collectors.toList());

        return new ProductHistoryResponse(
                product.getName(),
                product.getDescription(),
                priceResponses
        );
    }

    private PriceResponse mapToPriceResponse(Price price) {
        return new PriceResponse(
                price.getValue(),
                price.getInitDate(),
                price.getEndDate()
        );
    }
}
