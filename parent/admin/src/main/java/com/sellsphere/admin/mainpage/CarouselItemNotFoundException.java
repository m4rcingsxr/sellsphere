package com.sellsphere.admin.mainpage;

public class CarouselItemNotFoundException extends Throwable {

    public CarouselItemNotFoundException() {
        super("Carousel Item not found.");
    }

    public CarouselItemNotFoundException(String message) {
        super(message);
    }
}
