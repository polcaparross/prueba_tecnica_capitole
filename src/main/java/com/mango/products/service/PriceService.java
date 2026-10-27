package com.mango.products.service;

import com.mango.products.dto.PriceRequest;
import com.mango.products.dto.PriceResponse;
import com.mango.products.dto.PriceValueResponse;
import com.mango.products.dto.ProductHistoryResponse;
import com.mango.products.exception.ResourceNotFoundException;
import com.mango.products.exception.ValidationException;
import com.mango.products.model.Price;
import com.mango.products.model.Product;
import com.mango.products.repository.PriceRepository;
import com.mango.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
        if (request.getEndDate() != null && !request.getInitDate().isBefore(request.getEndDate())) {
            throw new ValidationException("initDate must be before endDate");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (priceRepository.existsOverlappingPrice(productId, request.getInitDate(), request.getEndDate())) {
            throw new ValidationException("New price overlaps with an existing price range");
        }

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
        List<Price> prices = priceRepository.findValidPrice(productId, date, PageRequest.of(0, 1));
        if (prices.isEmpty()) {
            throw new ResourceNotFoundException("Price not found for product id: " + productId + " at date: " + date);
        }
        return new PriceValueResponse(prices.get(0).getValue());
    }

    @Transactional(readOnly = true)
    public ProductHistoryResponse getPriceHistory(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

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
