package com.sellsphere.admin.mainpage;

public class CarouselNotFoundException extends Throwable{

    public CarouselNotFoundException() {
        super("Carousel not found.");
    }

    public CarouselNotFoundException(String message) {
        super(message);
    }
}
