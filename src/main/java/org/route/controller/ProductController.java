package org.route.controller;

import jakarta.validation.Valid;
import org.route.model.Product;
import org.route.model.ProductItemView;
import org.route.service.BrandService;
import org.route.service.CategoryService;
import org.route.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    @GetMapping("/products")
    public String getProducts(Model model) {
        List<ProductItemView> products = productService.getAvailableProductViews();
        model.addAttribute("products", products);
        model.addAttribute("availableProducts", products);
        return "products";
    }

    @GetMapping({"/new", "/products/new"})
    public String showForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("subCategories", categoryService.getAllSubCategories());
        model.addAttribute("activeBrands", brandService.getActiveBrands());
        return "productForm";
    }

    @PostMapping("/products")
    public String addProduct(@Valid @ModelAttribute("product") Product product,
                             BindingResult result,
                             Model model) {
        productService.validateProduct(product, result);
        if (result.hasErrors()) {
            model.addAttribute("subCategories", categoryService.getAllSubCategories());
            model.addAttribute("activeBrands", brandService.getActiveBrands());
            return "productForm";
        }
        productService.saveProduct(product);
        return "redirect:/products";
    }
}
