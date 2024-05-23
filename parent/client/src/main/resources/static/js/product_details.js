$(function() {
    initThumbnailCarousel();
})

function initThumbnailCarousel() {
    var main = new Splide('#main-carousel', {
        widthAuto: true,
        type: 'fade',
        rewind: true,
        pagination: false,
        arrows: false,
    });

    var thumbnails = new Splide('#thumbnail-carousel', {
        fixedWidth: 140,
        fixedHeight: 100,
        rewind: true,
        pagination: false,
        focus: 'center',
        arrows: false,
        isNavigation: true,
        breakpoints: {
            600: {
                fixedHeight: 44,
            },
        },
    });

    main.sync(thumbnails);
    main.mount();
    thumbnails.mount();

}