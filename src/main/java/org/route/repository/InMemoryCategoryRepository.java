package org.route.repository;

import jakarta.annotation.PostConstruct;
import org.route.model.Category;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryCategoryRepository implements CategoryRepository {

    private final List<Category> categories = new CopyOnWriteArrayList<>();

    @PostConstruct
    void init() {
        categories.add(Category.builder().name("Electronics").slug("electronics").parentSlug("").displayOrder(1).active(true).build());
        categories.add(Category.builder().name("Mobile Phones").slug("mobile-phones").parentSlug("electronics").displayOrder(1).active(true).build());
        categories.add(Category.builder().name("Laptops").slug("laptops").parentSlug("electronics").displayOrder(2).active(true).build());
        categories.add(Category.builder().name("Accessories").slug("electronics-accessories").parentSlug("electronics").displayOrder(3).active(true).build());
        categories.add(Category.builder().name("Home & Kitchen").slug("home-kitchen").parentSlug("").displayOrder(2).active(true).build());
        categories.add(Category.builder().name("Cookware").slug("cookware").parentSlug("home-kitchen").displayOrder(1).active(true).build());
        categories.add(Category.builder().name("Lighting").slug("lighting").parentSlug("home-kitchen").displayOrder(2).active(true).build());
        categories.add(Category.builder().name("Fashion").slug("fashion").parentSlug("").displayOrder(3).active(false).build());
        categories.add(Category.builder().name("Men's Clothing").slug("mens-clothing").parentSlug("fashion").displayOrder(1).active(true).build());
    }

    @Override
    public List<Category> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(categories));
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        if (slug == null) {
            return Optional.empty();
        }
        return categories.stream()
                .filter(c -> c.getSlug().equalsIgnoreCase(slug.trim()))
                .findFirst();
    }

    @Override
    public List<Category> findByParentSlug(String parentSlug) {
        String normalized = (parentSlug == null || parentSlug.trim().isEmpty()) ? "" : parentSlug.trim();
        return categories.stream()
                .filter(c -> {
                    String p = (c.getParentSlug() == null || c.getParentSlug().trim().isEmpty()) ? "" : c.getParentSlug().trim();
                    return p.equalsIgnoreCase(normalized);
                })
                .toList();
    }

    @Override
    public void save(Category category) {
        categories.add(category);
    }
}

