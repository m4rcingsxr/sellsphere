package com.sellsphere.admin.product;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class ProductController {

    private final ProductService productService;

    public static final String DEFAULT_REDIRECT_URL = "redirect:/products/page/0?sortField=name" +
            "&sortDir=asc";

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
}
