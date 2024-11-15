/**
 * jQuery Lazy Loading Script
 *
 * This script lazy loads images when they are about to enter the viewport.
 * It uses the Intersection Observer API for modern browsers and falls back
 * to a scroll, resize, and orientation change event listener for older browsers.
 *
 * Required HTML structure:
 * - Each image to be lazy-loaded must have the class 'lazy'.
 * - Each image must have a 'data-src' attribute containing the image URL.
 * - Each image should have a sibling element with the class 'spinner' to indicate loading.
 *
 * - Example:
 *   <div class="entity-image-container">
 *     <img data-src="path-to-your-image1.jpg" alt="Description" class="entity-image lazy">
 *     <div class="spinner"></div>
 *   </div>
 */
$(document).ready(async function() {

    // Function to handle lazy loading of images
    const handleLazyLoad = async (lazyImage) => {
        return new Promise((resolve) => {
            lazyImage.attr("src", lazyImage.data("src"));
            lazyImage.on("load", function() {
                lazyImage.addClass("loaded");
                lazyImage.siblings(".spinner").hide(); // Hide spinner
                resolve();
            });
            lazyImage.on("error", function() {
                lazyImage.siblings(".spinner").hide(); // Hide spinner on error
                resolve();
            });
        });
    };

    // Select all images with the class 'lazy'.
    let lazyImages = $("img.lazy");

    // Check if Intersection Observer is supported by the browser.
    if ("IntersectionObserver" in window) {
        // Create an Intersection Observer instance.
        let lazyImageObserver = new IntersectionObserver(async function(entries, observer) {
            for (const entry of entries) {
                if (entry.isIntersecting) {
                    // Get the lazy image element.
                    let lazyImage = $(entry.target);
                    // Handle lazy loading.
                    await handleLazyLoad(lazyImage);
                    lazyImageObserver.unobserve(entry.target);
                }
            }
        });

        // Observe each lazy image.
        lazyImages.each(function() {
            lazyImageObserver.observe(this);
        });
    } else {
        // Fallback for browsers without IntersectionObserver support

        // Lazy load function to load images in view.
        const lazyLoad = async () => {
            for (const img of lazyImages) {
                let lazyImage = $(img);
                if (lazyImage.offset().top < $(window).scrollTop() + $(window).height() &&
                    lazyImage.offset().bottom > $(window).scrollTop() &&
                    lazyImage.css("display") !== "none") {
                    await handleLazyLoad(lazyImage);
                    lazyImage.removeClass("lazy").addClass("loaded");
                }
            }

            // Remove event listeners if all images are loaded.
            if (lazyImages.length === 0) {
                $(document).off("scroll", lazyLoad);
                $(window).off("resize", lazyLoad);
                $(window).off("orientationchange", lazyLoad);
            }
        };

        // Attach event listeners for scroll, resize, and orientation change.
        $(document).on("scroll", lazyLoad);
        $(window).on("resize", lazyLoad);
        $(window).on("orientationchange", lazyLoad);
        await lazyLoad(); // Initial call to load images that are in view on page load
    }
});