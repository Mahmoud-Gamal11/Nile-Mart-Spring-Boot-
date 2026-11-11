package org.route.controller;

import jakarta.validation.Valid;
import org.route.model.Category;
import org.route.model.CategoryItemView;
import org.route.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String getCategories(Model model) {
        List<CategoryItemView> categories = categoryService.getCategoriesInHierarchy();
        model.addAttribute("categories", categories);
        return "categories";
    }

    @GetMapping("/categories/new")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", Category.builder().displayOrder(0).active(true).build());
        model.addAttribute("topLevelCategories", categoryService.getTopLevelCategories());
        return "categoryForm";
    }

    @PostMapping("/categories")
    public String addCategory(@Valid @ModelAttribute("category") Category category,
                              BindingResult result,
                              Model model) {
        categoryService.validateCategory(category, result);
        if (result.hasErrors()) {
            model.addAttribute("topLevelCategories", categoryService.getTopLevelCategories());
            return "categoryForm";
        }
        categoryService.saveCategory(category);
        return "redirect:/categories";
    }
}
