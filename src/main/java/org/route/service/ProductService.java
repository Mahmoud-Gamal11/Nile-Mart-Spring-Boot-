package org.route.service;

import org.route.model.Brand;
import org.route.model.Category;
import org.route.model.Product;
import org.route.model.ProductItemView;
import org.route.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          BrandService brandService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getAvailableProducts() {
        List<Product> available = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p.getInStock() != null && p.getInStock() > 0) {
                available.add(p);
            }
        }
        return available;
    }

    public List<ProductItemView> getAvailableProductViews() {
        List<ProductItemView> views = new ArrayList<>();
        for (Product p : getAvailableProducts()) {
            String catName = categoryService.getCategoryBySlug(p.getCategory())
                    .map(Category::getName)
                    .orElse(p.getCategory());

            String brandName = "—";
            if (p.getBrandSlug() != null && !p.getBrandSlug().isBlank()) {
                brandName = brandService.getBrandBySlug(p.getBrandSlug())
                        .map(Brand::getName)
                        .orElse("—");
            }

            views.add(ProductItemView.builder()
                    .sku(p.getSku())
                    .name(p.getName())
                    .price(p.getPrice())
                    .inStock(p.getInStock())
                    .categorySlug(p.getCategory())
                    .categoryName(catName)
                    .brandSlug(p.getBrandSlug())
                    .brandName(brandName)
                    .build());
        }
        return views;
    }

    public boolean isProductExist(String sku) {
        if (sku == null || sku.isBlank()) {
            return false;
        }
        return productRepository.findBySku(sku) != null;
    }

    public String validateProductCategory(String categorySlug) {
        if (categorySlug == null || categorySlug.isBlank()) {
            return null; // Handled by Bean Validation @NotBlank
        }
        Optional<Category> catOpt = categoryService.getCategoryBySlug(categorySlug);
        if (catOpt.isEmpty()) {
            return "Selected category does not exist.";
        }
        Category cat = catOpt.get();
        if (!categoryService.isSubCategory(cat)) {
            return "Category must be a sub-category (CAT-01).";
        }
        return null;
    }

    public String validateProductBrand(String brandSlug) {
        if (brandSlug == null || brandSlug.isBlank()) {
            return null; // brand is optional
        }
        Optional<Brand> brandOpt = brandService.getBrandBySlug(brandSlug);
        if (brandOpt.isEmpty() || !brandOpt.get().isActive()) {
            return "Brand does not exist or is inactive.";
        }
        return null;
    }

    public void validateProduct(Product product, Errors errors) {
        if (product.getSku() != null && !product.getSku().isBlank() && isProductExist(product.getSku())) {
            errors.rejectValue("sku", "duplicate", "sku is already exist");
        }

        String categoryError = validateProductCategory(product.getCategory());
        if (categoryError != null) {
            errors.rejectValue("category", "invalid", categoryError);
        }

        String brandError = validateProductBrand(product.getBrandSlug());
        if (brandError != null) {
            errors.rejectValue("brandSlug", "invalid", brandError);
        }
    }

    public void saveProduct(Product product) {
        if (product.getSku() != null) {
            product.setSku(product.getSku().trim());
        }
        if (product.getName() != null) {
            product.setName(product.getName().trim());
        }
        if (product.getCategory() != null) {
            product.setCategory(product.getCategory().trim());
        }
        if (product.getBrandSlug() != null && product.getBrandSlug().isBlank()) {
            product.setBrandSlug(null);
        } else if (product.getBrandSlug() != null) {
            product.setBrandSlug(product.getBrandSlug().trim());
        }
        productRepository.save(product);
    }
}
