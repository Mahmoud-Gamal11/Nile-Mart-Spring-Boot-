package org.route.service;

import org.route.model.Brand;
import org.route.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.List;
import java.util.Optional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public List<Brand> getActiveBrands() {
        return brandRepository.findAll().stream()
                .filter(Brand::isActive)
                .toList();
    }

    public Optional<Brand> getBrandBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return brandRepository.findBySlug(slug);
    }

    public boolean isNameDuplicate(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return brandRepository.findByNameIgnoreCase(name).isPresent();
    }

    public boolean isSlugDuplicate(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        return brandRepository.findBySlug(slug).isPresent();
    }

    public void validateBrand(Brand brand, Errors errors) {
        if (brand.getName() != null && !brand.getName().isBlank()) {
            String trimmedName = brand.getName().trim();
            if (trimmedName.length() < 2 || trimmedName.length() > 50) {
                if (!errors.hasFieldErrors("name")) {
                    errors.rejectValue("name", "size", "Brand name must be between 2 and 50 characters");
                }
            } else if (isNameDuplicate(trimmedName)) {
                errors.rejectValue("name", "duplicate", "Brand name already exists");
            }
        }
        if (brand.getSlug() != null && !brand.getSlug().isBlank() && isSlugDuplicate(brand.getSlug())) {
            errors.rejectValue("slug", "duplicate", "Slug already exists");
        }
    }

    public void saveBrand(Brand brand) {
        if (brand.getName() != null) {
            brand.setName(brand.getName().trim());
        }
        if (brand.getSlug() != null) {
            brand.setSlug(brand.getSlug().trim());
        }
        brandRepository.save(brand);
    }
}
