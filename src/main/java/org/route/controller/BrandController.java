package org.route.controller;

import jakarta.validation.Valid;
import org.route.model.Brand;
import org.route.service.BrandService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping("/brands")
    public String getBrands(Model model) {
        List<Brand> brands = brandService.getAllBrands();
        model.addAttribute("brands", brands);
        return "brands";
    }

    @GetMapping("/brands/new")
    public String showBrandForm(Model model) {
        model.addAttribute("brand", Brand.builder().active(true).build());
        return "brandForm";
    }

    @PostMapping("/brands")
    public String addBrand(@Valid @ModelAttribute("brand") Brand brand,
                           BindingResult result) {
        brandService.validateBrand(brand, result);
        if (result.hasErrors()) {
            return "brandForm";
        }
        brandService.saveBrand(brand);
        return "redirect:/brands";
    }
}
