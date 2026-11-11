package org.route.repository;

import jakarta.annotation.PostConstruct;
import org.route.model.Brand;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryBrandRepository implements BrandRepository {

    private final List<Brand> brands = new CopyOnWriteArrayList<>();

    @PostConstruct
    void init() {
        brands.add(Brand.builder().name("Samsung").slug("samsung").active(true).build());
        brands.add(Brand.builder().name("Asus").slug("asus").active(true).build());
        brands.add(Brand.builder().name("Anker").slug("anker").active(true).build());
        brands.add(Brand.builder().name("Tefal").slug("tefal").active(true).build());
        brands.add(Brand.builder().name("Old Brand").slug("old-brand").active(false).build());
    }

    @Override
    public List<Brand> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(brands));
    }

    @Override
    public Optional<Brand> findBySlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        return brands.stream()
                .filter(b -> b.getSlug().equalsIgnoreCase(slug.trim()))
                .findFirst();
    }

    @Override
    public Optional<Brand> findByNameIgnoreCase(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return brands.stream()
                .filter(b -> b.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    @Override
    public void save(Brand brand) {
        brands.add(brand);
    }
}

