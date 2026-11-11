package org.route;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.route.model.Brand;
import org.route.model.Category;
import org.route.model.CategoryItemView;
import org.route.model.Product;
import org.route.service.BrandService;
import org.route.service.CategoryService;
import org.route.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class NileMartApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Context loads successfully and root redirects to /products")
    void contextLoadsAndRootRedirects() throws Exception {
        assertNotNull(brandService);
        assertNotNull(categoryService);
        assertNotNull(productService);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @DisplayName("Every page has top menu with links to Products, Categories and Brands")
    void testTopMenuPresentOnAllPages() throws Exception {
        for (String url : List.of("/products", "/products/new", "/categories", "/categories/new", "/brands", "/brands/new")) {
            mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"/products\"")))
                    .andExpect(content().string(containsString("href=\"/categories\"")))
                    .andExpect(content().string(containsString("href=\"/brands\"")));
        }
    }

    // ==========================================
    // SEED DATA TESTS
    // ==========================================

    @Test
    @DisplayName("Seed Brands loaded correctly")
    void testBrandSeedData() {
        List<Brand> brands = brandService.getAllBrands();
        assertTrue(brands.stream().anyMatch(b -> b.getName().equals("Samsung") && b.getSlug().equals("samsung") && b.isActive()));
        assertTrue(brands.stream().anyMatch(b -> b.getName().equals("Asus") && b.getSlug().equals("asus") && b.isActive()));
        assertTrue(brands.stream().anyMatch(b -> b.getName().equals("Anker") && b.getSlug().equals("anker") && b.isActive()));
        assertTrue(brands.stream().anyMatch(b -> b.getName().equals("Tefal") && b.getSlug().equals("tefal") && b.isActive()));
        assertTrue(brands.stream().anyMatch(b -> b.getName().equals("Old Brand") && b.getSlug().equals("old-brand") && !b.isActive()));
    }

    @Test
    @DisplayName("Seed Categories loaded correctly with 2 levels (CAT-01)")
    void testCategorySeedData() {
        List<Category> categories = categoryService.getAllCategories();
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("electronics") && categoryService.isTopLevel(c) && c.getDisplayOrder() == 1 && c.isActive()));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("home-kitchen") && categoryService.isTopLevel(c) && c.getDisplayOrder() == 2 && c.isActive()));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("fashion") && categoryService.isTopLevel(c) && c.getDisplayOrder() == 3 && !c.isActive()));

        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("mobile-phones") && "electronics".equals(c.getParentSlug()) && c.getDisplayOrder() == 1));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("laptops") && "electronics".equals(c.getParentSlug()) && c.getDisplayOrder() == 2));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("electronics-accessories") && "electronics".equals(c.getParentSlug()) && c.getDisplayOrder() == 3));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("cookware") && "home-kitchen".equals(c.getParentSlug()) && c.getDisplayOrder() == 1));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("lighting") && "home-kitchen".equals(c.getParentSlug()) && c.getDisplayOrder() == 2));
        assertTrue(categories.stream().anyMatch(c -> c.getSlug().equals("mens-clothing") && "fashion".equals(c.getParentSlug()) && c.getDisplayOrder() == 1));
    }

    @Test
    @DisplayName("Seed Products loaded and updated correctly")
    void testProductSeedData() {
        List<Product> products = productService.getAllProducts();
        assertTrue(products.stream().anyMatch(p -> p.getSku().equals("P002") && "Desk Lamp".equals(p.getName()) && "lighting".equals(p.getCategory()) && p.getBrandSlug() == null && p.getInStock() == 10));
        assertTrue(products.stream().anyMatch(p -> p.getSku().equals("P003") && "ASUS TUF".equals(p.getName()) && "laptops".equals(p.getCategory()) && "asus".equals(p.getBrandSlug()) && p.getInStock() == 4));
        assertTrue(products.stream().anyMatch(p -> p.getSku().equals("PH-A55-128") && "Samsung Galaxy A55 128GB".equals(p.getName()) && "mobile-phones".equals(p.getCategory()) && "samsung".equals(p.getBrandSlug()) && p.getInStock() == 7));
    }

    // ==========================================
    // BRANDS WEB TESTS
    // ==========================================

    @Test
    @DisplayName("GET /brands returns table with all brands and Yes/No active status")
    void testGetBrands() throws Exception {
        mockMvc.perform(get("/brands"))
                .andExpect(status().isOk())
                .andExpect(view().name("brands"))
                .andExpect(model().attributeExists("brands"))
                .andExpect(content().string(containsString("Samsung")))
                .andExpect(content().string(containsString("Old Brand")))
                .andExpect(content().string(containsString("Yes")))
                .andExpect(content().string(containsString("No")));
    }

    @Test
    @DisplayName("GET /brands/new returns brand form with default active=true")
    void testGetBrandForm() throws Exception {
        mockMvc.perform(get("/brands/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("brandForm"))
                .andExpect(model().attribute("brand", hasProperty("active", is(true))));
    }

    @Test
    @DisplayName("POST /brands successfully saves brand and redirects")
    void testSaveBrandSuccess() throws Exception {
        mockMvc.perform(post("/brands")
                        .param("name", "Nile Cotton")
                        .param("slug", "nile-cotton")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/brands"));

        assertTrue(brandService.isSlugDuplicate("nile-cotton"));
    }

    @Test
    @DisplayName("POST /brands rejects duplicate name case-insensitively with exact message and preserves input")
    void testSaveBrandDuplicateName() throws Exception {
        mockMvc.perform(post("/brands")
                        .param("name", "SAMSUNG")
                        .param("slug", "samsung-new")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("brandForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("brand", "name", "duplicate"))
                .andExpect(content().string(containsString("Brand name already exists")))
                .andExpect(content().string(containsString("samsung-new")));
    }

    @Test
    @DisplayName("POST /brands rejects duplicate slug with exact message")
    void testSaveBrandDuplicateSlug() throws Exception {
        mockMvc.perform(post("/brands")
                        .param("name", "Unique Brand")
                        .param("slug", "samsung")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("brandForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("brand", "slug", "duplicate"))
                .andExpect(content().string(containsString("Slug already exists")));
    }

    @Test
    @DisplayName("POST /brands rejects all invalid slug formats mentioned in PDF")
    void testSaveBrandInvalidSlugs() throws Exception {
        for (String badSlug : List.of("Mobile Phones", "mobile_phones", "mobile phones!")) {
            mockMvc.perform(post("/brands")
                            .param("name", "Valid Brand Name")
                            .param("slug", badSlug)
                            .param("active", "true"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("brandForm"))
                    .andExpect(model().attributeHasFieldErrors("brand", "slug"));
        }
    }

    @Test
    @DisplayName("POST /brands rejects name shorter than 2 chars (including trimmed whitespace)")
    void testSaveBrandShortName() throws Exception {
        mockMvc.perform(post("/brands")
                        .param("name", " A ")
                        .param("slug", "brand-a")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("brandForm"))
                .andExpect(model().attributeHasFieldErrors("brand", "name"));
    }

    // ==========================================
    // CATEGORIES WEB TESTS
    // ==========================================

    @Test
    @DisplayName("GET /categories returns hierarchical categories sorted by displayOrder")
    void testGetCategories() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(content().string(containsString("Electronics")))
                .andExpect(content().string(containsString("— Mobile Phones")));

        List<CategoryItemView> views = categoryService.getCategoriesInHierarchy();
        assertEquals("Electronics", views.get(0).getName());
        assertEquals("— Mobile Phones", views.get(1).getName());
        assertEquals("Electronics", views.get(1).getParentName());
        assertEquals("— Laptops", views.get(2).getName());
        assertEquals("— Accessories", views.get(3).getName());
        assertEquals("Home & Kitchen", views.get(4).getName());
    }

    @Test
    @DisplayName("GET /categories/new returns category form with top-level dropdown only")
    void testGetCategoryForm() throws Exception {
        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("categoryForm"))
                .andExpect(model().attributeExists("category"))
                .andExpect(model().attributeExists("topLevelCategories"))
                .andExpect(content().string(containsString("None (top level)")))
                .andExpect(content().string(containsString("Electronics")))
                .andExpect(content().string(not(containsString("Mobile Phones"))));
    }

    @Test
    @DisplayName("POST /categories saves top-level category and redirects")
    void testSaveTopLevelCategorySuccess() throws Exception {
        mockMvc.perform(post("/categories")
                        .param("name", "Sports")
                        .param("slug", "sports")
                        .param("parentSlug", "")
                        .param("displayOrder", "4")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        assertTrue(categoryService.isSlugDuplicate("sports"));
    }

    @Test
    @DisplayName("POST /categories allows same name under different parent")
    void testSaveSameNameDifferentParentSuccess() throws Exception {
        // "Accessories" already exists under "electronics", so adding "Accessories" under "home-kitchen" must succeed!
        mockMvc.perform(post("/categories")
                        .param("name", "Accessories")
                        .param("slug", "kitchen-accessories")
                        .param("parentSlug", "home-kitchen")
                        .param("displayOrder", "3")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        assertTrue(categoryService.isSlugDuplicate("kitchen-accessories"));
    }

    @Test
    @DisplayName("POST /categories rejects sub-category under another sub-category (CAT-01)")
    void testSaveCategoryRejectThirdLevel() throws Exception {
        mockMvc.perform(post("/categories")
                        .param("name", "Samsung Phones")
                        .param("slug", "samsung-phones")
                        .param("parentSlug", "mobile-phones")
                        .param("displayOrder", "1")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("categoryForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("category", "parentSlug", "invalid"));
    }

    @Test
    @DisplayName("POST /categories rejects duplicate name within same parent with exact message")
    void testSaveCategoryDuplicateNameUnderSameParent() throws Exception {
        mockMvc.perform(post("/categories")
                        .param("name", "LAPTOPS")
                        .param("slug", "laptops-gaming")
                        .param("parentSlug", "electronics")
                        .param("displayOrder", "5")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("categoryForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("category", "name", "duplicate"))
                .andExpect(content().string(containsString("A category with this name already exists under Electronics")));
    }

    @Test
    @DisplayName("POST /categories rejects negative displayOrder")
    void testSaveCategoryNegativeDisplayOrder() throws Exception {
        mockMvc.perform(post("/categories")
                        .param("name", "Cameras")
                        .param("slug", "cameras")
                        .param("parentSlug", "electronics")
                        .param("displayOrder", "-1")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("categoryForm"))
                .andExpect(model().attributeHasFieldErrors("category", "displayOrder"));
    }

    // ==========================================
    // PRODUCTS WEB TESTS
    // ==========================================

    @Test
    @DisplayName("GET /products displays category name and brand name (and dash when no brand)")
    void testGetProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(content().string(containsString("Samsung Galaxy A55 128GB")))
                .andExpect(content().string(containsString("Mobile Phones")))
                .andExpect(content().string(containsString("Samsung")))
                .andExpect(content().string(containsString("Lighting")))
                .andExpect(content().string(containsString("—")));
    }

    @Test
    @DisplayName("GET /products/new displays form with sub-categories only and active brands only")
    void testGetProductForm() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("productForm"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attributeExists("subCategories"))
                .andExpect(model().attributeExists("activeBrands"))
                .andExpect(content().string(containsString("Mobile Phones")))
                .andExpect(content().string(containsString("No brand")))
                .andExpect(content().string(not(containsString("value=\"electronics\""))))
                .andExpect(content().string(not(containsString("Old Brand"))));
    }

    @Test
    @DisplayName("POST /products successfully saves product with and without brand and redirects")
    void testSaveProductSuccess() throws Exception {
        mockMvc.perform(post("/products")
                        .param("sku", "TEST-SKU-001")
                        .param("name", "Test Phone")
                        .param("category", "mobile-phones")
                        .param("brandSlug", "samsung")
                        .param("price", "999.99")
                        .param("inStock", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        assertTrue(productService.isProductExist("TEST-SKU-001"));

        mockMvc.perform(post("/products")
                        .param("sku", "TEST-SKU-NOBRAND")
                        .param("name", "Generic Pan")
                        .param("category", "cookware")
                        .param("brandSlug", "")
                        .param("price", "250.00")
                        .param("inStock", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        assertTrue(productService.isProductExist("TEST-SKU-NOBRAND"));
    }

    @Test
    @DisplayName("POST /products rejects category that is top-level (CAT-01)")
    void testSaveProductRejectTopLevelCategory() throws Exception {
        mockMvc.perform(post("/products")
                        .param("sku", "TEST-SKU-002")
                        .param("name", "Invalid Category Product")
                        .param("category", "electronics")
                        .param("brandSlug", "")
                        .param("price", "100")
                        .param("inStock", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("productForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("product", "category", "invalid"));
    }

    @Test
    @DisplayName("POST /products rejects blank category with single validation error")
    void testSaveProductBlankCategory() throws Exception {
        mockMvc.perform(post("/products")
                        .param("sku", "TEST-SKU-BLANKCAT")
                        .param("name", "Missing Category Product")
                        .param("category", "")
                        .param("brandSlug", "")
                        .param("price", "100")
                        .param("inStock", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("productForm"))
                .andExpect(model().attributeHasFieldErrors("product", "category"))
                .andExpect(model().attribute("product", hasProperty("name", is("Missing Category Product"))));
    }

    @Test
    @DisplayName("POST /products rejects inactive or non-existent brand")
    void testSaveProductRejectInactiveOrMissingBrand() throws Exception {
        mockMvc.perform(post("/products")
                        .param("sku", "TEST-SKU-003")
                        .param("name", "Invalid Brand Product")
                        .param("category", "mobile-phones")
                        .param("brandSlug", "old-brand")
                        .param("price", "100")
                        .param("inStock", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("productForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("product", "brandSlug", "invalid"));

        mockMvc.perform(post("/products")
                        .param("sku", "TEST-SKU-004")
                        .param("name", "Missing Brand Product")
                        .param("category", "mobile-phones")
                        .param("brandSlug", "does-not-exist")
                        .param("price", "100")
                        .param("inStock", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("productForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("product", "brandSlug", "invalid"));
    }

    @Test
    @DisplayName("POST /products rejects duplicate SKU")
    void testSaveProductRejectDuplicateSku() throws Exception {
        mockMvc.perform(post("/products")
                        .param("sku", "P002")
                        .param("name", "Duplicate SKU Product")
                        .param("category", "lighting")
                        .param("brandSlug", "")
                        .param("price", "200")
                        .param("inStock", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("productForm"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("product", "sku", "duplicate"));
    }
}
