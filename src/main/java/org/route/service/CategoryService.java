package org.route.service;

import org.route.model.Category;
import org.route.model.CategoryItemView;
import org.route.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private static final Comparator<Category> DISPLAY_ORDER_COMPARATOR = Comparator
            .comparing(Category::getDisplayOrder, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(Category::getName, String.CASE_INSENSITIVE_ORDER);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }
        return categoryRepository.findBySlug(slug);
    }

    public boolean isTopLevel(Category category) {
        return category.getParentSlug() == null || category.getParentSlug().trim().isEmpty();
    }

    public boolean isSubCategory(Category category) {
        return !isTopLevel(category);
    }

    public List<Category> getTopLevelCategories() {
        return categoryRepository.findAll().stream()
                .filter(this::isTopLevel)
                .sorted(DISPLAY_ORDER_COMPARATOR)
                .toList();
    }

    public List<Category> getSubCategories(String parentSlug) {
        return categoryRepository.findByParentSlug(parentSlug).stream()
                .sorted(DISPLAY_ORDER_COMPARATOR)
                .toList();
    }

    public List<Category> getAllSubCategories() {
        return categoryRepository.findAll().stream()
                .filter(this::isSubCategory)
                .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<CategoryItemView> getCategoriesInHierarchy() {
        List<CategoryItemView> result = new ArrayList<>();

        List<Category> topLevel = getTopLevelCategories();
        for (Category top : topLevel) {
            result.add(CategoryItemView.builder()
                    .name(top.getName())
                    .slug(top.getSlug())
                    .parentName("—")
                    .displayOrder(top.getDisplayOrder())
                    .active(top.isActive())
                    .subCategory(false)
                    .build());

            List<Category> subCats = getSubCategories(top.getSlug());
            for (Category sub : subCats) {
                result.add(CategoryItemView.builder()
                        .name("— " + sub.getName())
                        .slug(sub.getSlug())
                        .parentName(top.getName())
                        .displayOrder(sub.getDisplayOrder())
                        .active(sub.isActive())
                        .subCategory(true)
                        .build());
            }
        }
        return result;
    }

    public String checkNameDuplicateUnderParent(String name, String parentSlug) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalizedParent = (parentSlug == null || parentSlug.trim().isEmpty()) ? "" : parentSlug.trim();
        for (Category c : categoryRepository.findAll()) {
            String p = (c.getParentSlug() == null || c.getParentSlug().trim().isEmpty()) ? "" : c.getParentSlug().trim();
            if (p.equalsIgnoreCase(normalizedParent) && c.getName().equalsIgnoreCase(name.trim())) {
                if (!normalizedParent.isEmpty()) {
                    Optional<Category> parent = categoryRepository.findBySlug(normalizedParent);
                    String parentName = parent.map(Category::getName).orElse(normalizedParent);
                    return "A category with this name already exists under " + parentName;
                } else {
                    return "A category with this name already exists";
                }
            }
        }
        return null;
    }

    public String validateParentCategory(String parentSlug) {
        if (parentSlug == null || parentSlug.trim().isEmpty()) {
            return null;
        }
        Optional<Category> parentOpt = categoryRepository.findBySlug(parentSlug.trim());
        if (parentOpt.isEmpty()) {
            return "Parent category does not exist";
        }
        Category parent = parentOpt.get();
        if (!isTopLevel(parent)) {
            return "Parent category must be a top-level category";
        }
        return null;
    }

    public boolean isSlugDuplicate(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        return categoryRepository.findBySlug(slug.trim()).isPresent();
    }

    public void validateCategory(Category category, Errors errors) {
        String parentError = validateParentCategory(category.getParentSlug());
        if (parentError != null) {
            errors.rejectValue("parentSlug", "invalid", parentError);
        }

        if (category.getName() != null && !category.getName().isBlank()) {
            String trimmedName = category.getName().trim();
            if (trimmedName.length() < 2 || trimmedName.length() > 50) {
                if (!errors.hasFieldErrors("name")) {
                    errors.rejectValue("name", "size", "Category name must be between 2 and 50 characters");
                }
            } else {
                String nameError = checkNameDuplicateUnderParent(trimmedName, category.getParentSlug());
                if (nameError != null) {
                    errors.rejectValue("name", "duplicate", nameError);
                }
            }
        }

        if (category.getSlug() != null && !category.getSlug().isBlank() && isSlugDuplicate(category.getSlug())) {
            errors.rejectValue("slug", "duplicate", "Slug already exists");
        }
    }

    public void saveCategory(Category category) {
        if (category.getName() != null) {
            category.setName(category.getName().trim());
        }
        if (category.getSlug() != null) {
            category.setSlug(category.getSlug().trim());
        }
        if (category.getParentSlug() == null || category.getParentSlug().trim().isEmpty()) {
            category.setParentSlug("");
        } else {
            category.setParentSlug(category.getParentSlug().trim());
        }
        categoryRepository.save(category);
    }
}
