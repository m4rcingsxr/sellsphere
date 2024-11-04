package com.sellsphere.client;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.mainpage.CarouselService;
import com.sellsphere.common.entity.Carousel;
import com.sellsphere.common.entity.CarouselImage;
import com.sellsphere.common.entity.Customer;
import com.sellsphere.common.entity.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.glassfish.jersey.internal.inject.Custom;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final CarouselService carouselService;
    private final CustomerService customerService;

    @GetMapping({"/", "/loggedOut"})
    public String viewHomePage(Model model, Principal principal) throws CustomerNotFoundException {
        Customer customer = null;
        if(principal != null) {
            customer = getAuthenticatedCustomer(principal);
        }

        Carousel mainCarousel = carouselService.getMainCarousel();
        List<CarouselImage> carouselImages = carouselService.getCarouselImages(mainCarousel);
        Map<Carousel, List<Object>> carouselMap = carouselService.getAllCarousels(customer);

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

    private Customer getAuthenticatedCustomer(Principal principal) throws CustomerNotFoundException {
        return customerService.getByEmail(principal.getName());
    }
}
