package com.sellsphere.client;

import com.sellsphere.client.mainpage.CarouselService;
import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final CarouselService carouselService;

    @GetMapping({"/", "/loggedOut"})
    public String viewHomePage(Model model) {
        Carousel mainCarousel = carouselService.getMainCarousel();
        List<CarouselImage> carouselImages = carouselService.getCarouselImages(mainCarousel);
        Map<Carousel, List<Object>> carouselMap = carouselService.getAllCarousels();

        model.addAttribute("mainCarousel", mainCarousel);
        model.addAttribute("carouselMap", carouselMap);
        model.addAttribute("carouselImages", carouselImages);
        model.addAttribute("isMainPage", true);

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/keycloak";
    }

}
