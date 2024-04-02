package com.sellsphere.admin.brand;

import com.sellsphere.admin.page.PagingAndSortingHelper;
import com.sellsphere.admin.page.PagingAndSortingParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class BrandController {

    public static final String DEFAULT_REDIRECT_URL = "redirect:/brand/page/0?sortField=name" +
            "&sortDir=asc";

    private final BrandService brandService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/brands")
    public String listFirstPage() {
        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/brands/page/{pageNum}")
    public String listPage(
            @PagingAndSortingParam(listName = "brandList", moduleURL =
                    "/brand") PagingAndSortingHelper helper,
            @PathVariable("pageNum") Integer pageNum
    ) {
        brandService.listPage(pageNum, helper);

        return "brand/brands";
    }

}
