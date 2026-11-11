package org.route.repository;

import jakarta.annotation.PostConstruct;
import org.route.model.Product;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final List<Product> myProducts = new CopyOnWriteArrayList<>();

    @PostConstruct
    void init() {
        myProducts.add(Product.builder()
                .sku("P002")
                .name("Desk Lamp")
                .price(BigDecimal.valueOf(500))
                .inStock(10)
                .category("lighting")
                .brandSlug(null)
                .build());

        myProducts.add(Product.builder()
                .sku("P003")
                .name("ASUS TUF")
                .price(BigDecimal.valueOf(24000))
                .inStock(4)
                .category("laptops")
                .brandSlug("asus")
                .build());

        myProducts.add(Product.builder()
                .sku("PH-A55-128")
                .name("Samsung Galaxy A55 128GB")
                .price(new BigDecimal("18500.00"))
                .inStock(7)
                .category("mobile-phones")
                .brandSlug("samsung")
                .build());
    }

    @Override
    public List<Product> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(myProducts));
    }

    @Override
    public Product findBySku(String sku) {
        if (sku == null) {
            return null;
        }
        for (Product product : myProducts) {
            if (product.getSku().equalsIgnoreCase(sku.trim())) {
                return product;
            }
        }
        return null;
    }

    @Override
    public void save(Product product) {
        myProducts.add(product);
    }
}
