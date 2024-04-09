package com.sellsphere.admin.product;

import com.sellsphere.admin.brand.BrandService;
import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import com.sellsphere.common.entity.Brand;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Product;
import com.sellsphere.common.entity.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ProductController {

    private static final String PRODUCT_FORM = "product/product_form";
    public static final String DEFAULT_REDIRECT_URL =
            "redirect:/products/page/0?sortField=name" + "&sortDir=asc";

    private final ProductService productService;
    private final BrandService brandService;

    @GetMapping("/products")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/products/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "productList", moduleURL = "/products") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum) {
        productService.listPage(pageNum, helper);

        return "product/products";
    }

    @GetMapping({"/products/new", "/products/edit/{id}"})
    public String showProductForm(@PathVariable(required = false) Integer id, Model model)
            throws ProductNotFoundException {
        Product product = (id != null) ? productService.get(id) : new Product();

        prepareModelForProductForm(product, model);
        return PRODUCT_FORM;
    }

    private void prepareModelForProductForm(Product product, Model model) {
        String pageTitle = (product.getId() != null) ? "Edit Product [ID: " +
                product.getId() + "]" : "Create New Product";

        List<Brand> brandList = brandService.listAll("name", Constants.SORT_ASCENDING);

        model.addAttribute("brandList", brandList);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("product", product);
    }

}
