package org.route.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemView {
    private String sku;
    private String name;
    private BigDecimal price;
    private Integer inStock;
    private String categorySlug;
    private String categoryName;
    private String brandSlug;
    private String brandName;
}

