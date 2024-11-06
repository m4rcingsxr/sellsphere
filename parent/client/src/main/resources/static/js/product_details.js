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
        gap: 0,  // Set gap to 0 to remove spacing
        rewind: true,
        pagination: false,
        focus: 'center',
        arrows: false,
        isNavigation: true,

    });

    main.sync(thumbnails);
    main.mount();
    thumbnails.mount();

}