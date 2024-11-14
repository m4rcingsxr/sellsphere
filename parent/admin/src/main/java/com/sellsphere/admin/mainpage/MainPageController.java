package com.sellsphere.admin.mainpage;

import com.sellsphere.admin.article.ArticleService;
import com.sellsphere.admin.article.PromotionService;
import com.sellsphere.admin.brand.BrandService;
import com.sellsphere.admin.category.CategoryService;
import com.sellsphere.common.entity.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class MainPageController {

    private final CarouselService carouselService;

    public static final String DEFAULT_REDIRECT_URL = "redirect:/main_page";
    private final ArticleService articleService;
    private final PromotionService promotionService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final CarouselImageService carouselImageService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(String.class,
                                        new StringTrimmerEditor(true)
        );
    }

    @GetMapping("/main_page")
    public String listAll(Model model) {
        List<Carousel> carouselList = carouselService.listAll();

        model.addAttribute(carouselList);

        return "mainpage/main_page";
    }

    @PostMapping("/main_page/save")
    public String saveCarousel(@ModelAttribute("carousel") @Valid Carousel carousel, BindingResult bindingResult,
                               @RequestParam(required = false) Integer promotionId, RedirectAttributes ra, Model model)
            throws PromotionNotFoundException {

        if (bindingResult.hasErrors()) {
            String pageTitle = "Update carousel" + (carousel.getId() != null ? " ID [" + carousel.getId() + "]" : "");

            Set<Integer> idsSet = carousel.getCarouselItems().stream()
                    .map(CarouselItem::getEntityId)
                    .collect(Collectors.toSet());
            prepareCarouselFormModel(model, idsSet, pageTitle, carousel);
            return "mainpage/carousel_form";
        }

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                             "Successfully " + (carousel.getId() != null ? "updated" : "saved") + " carousel."
        );

        carouselService.save(carousel, promotionId);

        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping({"/main_page/new", "/main_page/edit/{id}"})
    public String showCarouselForm(@PathVariable(required = false) Integer id, Model model)
            throws CarouselNotFoundException {

        String pageTitle = (id != null ? "Update " : "Create new ") + "carousel" + (id != null ? " ID [" + id + "]" :
                "");
        Carousel carousel;
        Set<Integer> idsSet;

        if (id != null) {
            carousel = carouselService.get(id);

            idsSet = carousel.getCarouselItems().stream()
                    .map(CarouselItem::getEntityId)
                    .collect(Collectors.toSet());

        } else {
            idsSet = Collections.emptySet();
            carousel = new Carousel();
        }


        prepareCarouselFormModel(model, idsSet, pageTitle, carousel);

        return "mainpage/carousel_form";
    }



    private void prepareCarouselFormModel(Model model, Set<Integer> idsSet, String pageTitle, Carousel carousel) {
        List<Article> articleList = articleService.findAllByArticleType(ArticleType.PROMOTION, "title",
                                                                        Sort.Direction.ASC
        );
        List<Brand> brandList = brandService.listAllBrands("name", Sort.Direction.ASC);
        List<Category> categoryList = categoryService.listAllRootCategoriesSorted("name", Sort.Direction.ASC);
        List<Promotion> promotionList = promotionService.listAll();

        List<CarouselImage> carouselImageList = carouselImageService.findAll();

        model.addAttribute("idsSet", idsSet);
        model.addAttribute("articleList", articleList);
        model.addAttribute("promotionList", promotionList);

        model.addAttribute("carouselImageList", carouselImageList);
        model.addAttribute("brandList", brandList);
        model.addAttribute("categoryList", categoryList);

        model.addAttribute("carouselTypeList", CarouselType.values());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("carousel", carousel);
    }

    @GetMapping("/main_page/change_order/{from}/{to}")
    public String changeCarouselOrder(@PathVariable("from") Integer fromId, @PathVariable("to") Integer toId,
                                      RedirectAttributes ra) throws CarouselNotFoundException {
        carouselService.changeCarouselOrder(fromId, toId);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE,
                             "Successfully changed order from id [" + fromId + "] " + "to id [" + toId + "]"
        );

        return DEFAULT_REDIRECT_URL;
    }

    @GetMapping("/main_page/delete/{carousel_id}")
    public String deleteCarousel(@PathVariable("carousel_id") Integer carouselId, RedirectAttributes ra)
            throws CarouselNotFoundException {
        carouselService.delete(carouselId);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Successfully removed carousel ID[" + carouselId + "]");
        return DEFAULT_REDIRECT_URL;
    }

}
