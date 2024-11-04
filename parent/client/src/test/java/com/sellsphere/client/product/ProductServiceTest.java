package com.sellsphere.client.product;

import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Category;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void givenProductPageRequest_whenListProducts_thenShouldReturnProductPageResponse() {
        Product product1 = createProduct(1, "Product1", "Brand1", BigDecimal.valueOf(200), 10, "Red", "M");
        Product product2 = createProduct(1, "Product1", "Brand1", BigDecimal.valueOf(300), 10, "Red", "M");
        List<Product> productList = Arrays.asList(product1, product2);

        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 9), 2);

        // Mock the repository calls
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(productPage);

        // Create the product page request
        ProductPageRequest productPageRequest = new ProductPageRequest();
        productPageRequest.setCategoryId(1);
        productPageRequest.setKeyword("keyword");
        productPageRequest.setFilter(new String[]{"Color,Red", "Size,M"});
        productPageRequest.setSortBy("LOWEST");
        productPageRequest.setPageNum(0);
        productPageRequest.setMinPrice(BigDecimal.valueOf(100));
        productPageRequest.setMaxPrice(BigDecimal.valueOf(1000));

        // Call the method to test
        ProductPageResponse response = productService.listProducts(productPageRequest, null);

        // Assert the response
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(BigDecimal.valueOf(100), response.getMinPrice());
        assertEquals(BigDecimal.valueOf(1000), response.getMaxPrice());

        // Verify that the repository methods were called with correct parameters
        verify(productRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void givenFilterCriteria_whenCalculateAvailableFilterCounts_thenShouldReturnCorrectCounts() {
        // Creating products
        Product product1 = createProduct(1, "Product1", "Brand1", BigDecimal.valueOf(200), 10, "Red", "M");
        Product product5 = createProduct(5, "Product5", "Brand1", BigDecimal.valueOf(400), 30, "Red", "M");

        // Products that match the filter criteria
        List<Product> filteredProducts = Arrays.asList(product1, product5);

        // Mock the repository call to return the filtered products
        when(productRepository.findAll(any(Specification.class))).thenReturn(filteredProducts);

        // Define the filter criteria
        String keyword = "keyword";
        Integer categoryId = 1;
        String[] filter = new String[]{"Color,Red", "Size,M"};
        BigDecimal minPrice = BigDecimal.valueOf(100);
        BigDecimal maxPrice = BigDecimal.valueOf(1000);

        // Call the method to test
        Map<String, Map<String, Long>> counts = productService.calculateAvailableFilterCounts(keyword, categoryId, filter, minPrice, maxPrice);

        // Assert the counts
        assertNotNull(counts);

        // Verify counts for the filtered products
        assertEquals(2, counts.get("Brand").get("Brand1"));
        assertNull(counts.get("Brand").get("Brand2"));
        assertEquals(2, counts.get("Color").get("Red"));
        assertNull(counts.get("Color").get("Blue"));
        assertEquals(2, counts.get("Size").get("M"));
        assertNull(counts.get("Size").get("L"));
        assertNull(counts.get("Size").get("S"));
        assertNull(counts.get("Size").get("XL"));
        assertNull(counts.get("Color").get("Green"));
    }


    @Test
    void givenFilterMapRequest_whenCalculateAllFiltersCounts_thenShouldReturnExpectedMapWithCorrectOrder() {
        Product product1 = createProduct(1, "Product1", "Brand1", BigDecimal.valueOf(200), 10, "Red", "M");
        Product product2 = createProduct(2, "Product2", "Brand1", BigDecimal.valueOf(300), 20, "Blue", "L");
        Product product3 = createProduct(3, "Product3", "Brand2", BigDecimal.valueOf(400), 30, "Red", "S");
        Product product4 = createProduct(4, "Product4", "Brand2", BigDecimal.valueOf(400), 30, "Green", "XL");
        Product product5 = createProduct(5, "Product5", "Brand1", BigDecimal.valueOf(400), 30, "Red", "M");

        List<Product> filteredProducts = Arrays.asList(product1, product5);
        List<Product> allProducts = Arrays.asList(product1, product2, product3, product4, product5);

        doReturn(filteredProducts).doReturn(allProducts).when(productRepository).findAll(any(Specification.class));

        FilterMapCountRequest mapRequest = new FilterMapCountRequest();
        mapRequest.setCategoryId(1);
        mapRequest.setKeyword("keyword");
        mapRequest.setFilter(new String[]{"Color,Red", "Size,M"});
        mapRequest.setMinPrice(BigDecimal.valueOf(100));
        mapRequest.setMaxPrice(BigDecimal.valueOf(1000));

        Map<String, Map<String, Long>> counts = productService.calculateAllFilterCounts(mapRequest);

        assertNotNull(counts);

        // Verify counts for existing filters
        assertEquals(2, counts.get("Brand").get("Brand1"));
        assertEquals(0, counts.get("Brand").get("Brand2"));
        assertEquals(2, counts.get("Color").get("Red"));
        assertEquals(0, counts.get("Color").get("Blue"));
        assertEquals(2, counts.get("Size").get("M"));
        assertEquals(0, counts.get("Size").get("L"));
        assertEquals(0, counts.get("Size").get("S"));

        assertEquals(0, counts.get("Color").get("Green"));
        assertEquals(0, counts.get("Size").get("XL"));

        // Verify sorting
        List<Map.Entry<String, Map<String, Long>>> sortedEntries = new ArrayList<>(counts.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        // Check that entries are sorted by name
        assertEquals("Brand", sortedEntries.get(0).getKey());
        assertEquals("Color", sortedEntries.get(1).getKey());
        assertEquals("Size", sortedEntries.get(2).getKey());

        // Check that values under each product detail name are sorted
        Map<String, Long> sortedBrandValues = new LinkedHashMap<>(counts.get("Brand"));
        List<String> sortedBrandKeys = new ArrayList<>(sortedBrandValues.keySet());
        assertEquals("Brand1", sortedBrandKeys.get(0));
        assertEquals("Brand2", sortedBrandKeys.get(1));

        Map<String, Long> sortedColorValues = new LinkedHashMap<>(counts.get("Color"));
        List<String> sortedColorKeys = new ArrayList<>(sortedColorValues.keySet());
        assertEquals("Red", sortedColorKeys.get(0));
        assertEquals("Blue", sortedColorKeys.get(1));
        assertEquals("Green", sortedColorKeys.get(2));

        Map<String, Long> sortedSizeValues = new LinkedHashMap<>(counts.get("Size"));
        List<String> sortedSizeKeys = new ArrayList<>(sortedSizeValues.keySet());
        assertEquals("M", sortedSizeKeys.get(0));
        assertEquals("L", sortedSizeKeys.get(1));
        assertEquals("S", sortedSizeKeys.get(2));
        assertEquals("XL", sortedSizeKeys.get(3));
    }

    private Product createProduct(int id, String name, String brandName, BigDecimal price, int discountPercent, String color, String size) {
        Brand brand = new Brand();
        brand.setName(brandName);

        Category category = new Category();
        category.setName("laptops");
        category.setId(1);

        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setDiscountPercent(BigDecimal.valueOf(discountPercent));

        ProductDetail detailColor = new ProductDetail();
        detailColor.setName("Color");
        detailColor.setValue(color);
        detailColor.setProduct(product);

        ProductDetail detailSize = new ProductDetail();
        detailSize.setName("Size");
        detailSize.setValue(size);
        detailSize.setProduct(product);

        product.setDetails(Arrays.asList(detailColor, detailSize));

        product.setCategory(category);
        return product;
    }

}
