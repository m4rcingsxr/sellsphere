document.addEventListener('DOMContentLoaded', function() {

    const mainSwiper = new Swiper('#mainCarousel', {
        scrollbar: {
            el: '.swiper-scrollbar',
            hide: true,
        },
        autoplay: {
            delay:  Math.floor(Math.random() * 3000) + 2000,
            disableOnInteraction: false,
            loop: true,
        },
    });

    document.querySelectorAll('.product-carousels').forEach((carousel) => {
        new Swiper(carousel, {
            slidesPerView: 2,
            spaceBetween: 20,
            pagination: {
                el: ".swiper-pagination",
                type: 'bullets',
                clickable: true,
            },
            breakpoints: {
                640: {
                    slidesPerView: 4,
                    spaceBetween: 20,
                },
                1024: {
                    slidesPerView: 6,
                    spaceBetween: 10,
                },
            },
            autoplay: {
                delay: Math.floor(Math.random() * 3000) + 2000,
                disableOnInteraction: false,
            },
        });
    });


    const articleSwiper = new Swiper(".article-carousels", {
        slidesPerView: 2,
        spaceBetween: 30,
        pagination: {
            el: ".swiper-pagination",
            type: 'bullets',
            clickable: true,
        },
        breakpoints: {
            640: {
                slidesPerView: 4,
                spaceBetween: 20,
            },
            1024: {
                slidesPerView: 6,
                spaceBetween: 10,
            },
        },
        autoplay: {
            delay: Math.floor(Math.random() * 3000) + 2000,
            disableOnInteraction: false,

        },
    });


});