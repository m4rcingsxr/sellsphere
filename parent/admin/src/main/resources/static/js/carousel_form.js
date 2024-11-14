"use strict";

// URLs for different entity types
const urls = {
    ARTICLE: `${MODULE_URL}articles/titles`,
    PROMOTION: `${MODULE_URL}products/names`,
    IMAGE: `${MODULE_URL}carousel_images`,
};

// Document ready: Initialize event listeners and carousel setup
$(function () {
    handleCarouselTypeChange($("#type").val()); // Initial type handling
    initListeners();                           // Initialize event listeners
    initializeSwiper();                        // Initialize the Swiper for carousel functionality

    $("#type").on("change", function () {
        handleCarouselTypeChange(this.value);
    });
});

/**
 * Initializes the event listeners for different actions.
 */
function initListeners() {
    $("#promotionList").on("change", async function (event) {
        const selectedText = $(event.target).find(":selected").text();
        await loadPromotionProducts(selectedText); // Load products for selected promotion
    });

    // Event listener for image carousel actions (delete)
    $(document).on("click", "#slideContainer a[data-entity-id]", async function (event) {
        event.preventDefault();
        const entityId = $(this).data("entity-id");

        try {
            await deleteCarouselImage(entityId); // Delete the image
            const images = await fetchEntityByName([], "IMAGE");
            handleImageCarousel(images);
        } catch (error) {
            alert(error.message);
        }
    });

    $("#carouselImage").on("change", function (event) {
        addImageToSlider(event); // Add new image to slider
    });

    $("#articles").on("change", addArticleToSlider);   // Article selection
    $("#products").on("change", addProductToSlider);   // Product selection

    // Event listener for swapping carousel item positions
    $("#carouselItems").on("click", "a", function (event) {
        event.preventDefault();
        const $caret = $(event.currentTarget).find("i");

        if ($caret.hasClass("fa-caret-down")) {
            swapSliderInputs("down", $caret);
        } else if ($caret.hasClass("fa-caret-up")) {
            swapSliderInputs("up", $caret);
        }
    });
}

/**
 * Handles the change in carousel type and adjusts UI accordingly.
 * @param {string} type - The selected type (ARTICLE, PROMOTION, IMAGE)
 */
async function handleCarouselTypeChange(type) {
    $("#slideContainer, #imageSlideContainer, #carouselItems").empty(); // Clear current content

    switch (type) {
        case "PROMOTION":

            const selectedPromotionProducts = Array.from($("#products").find(":selected")).map(option => option.value);
            if(selectedPromotionProducts.length > 0) {
                const products = await fetchEntityByName(selectedPromotionProducts, "PROMOTION");
                handleProductCarousel(products);
            } else {
                await loadPromotionProducts($("#promotionList").find(":selected").text());
            }

            resetViewForPromotions();

            break;
        case "ARTICLE":
            const selectedArticles = Array.from($("#articles").find(":selected")).map(option => option.value); // Get selected article IDs
            if(selectedArticles.length > 0) {
                const articles = await fetchEntityByName(selectedArticles, "ARTICLE");
                handleArticleCarousel(articles);
            }
            resetViewForArticles();
            break;
        case "IMAGE":
            resetViewForImages();
            try {
                const images = await fetchEntityByName([], "IMAGE");
                handleImageCarousel(images);
            } catch (error) {
                alert(`Error loading images: ${error.message}`);
            }
            break;
    }
}

/**
 * Resets the UI for PROMOTION type selection.
 */
function resetViewForPromotions() {
    $("#order").val("");
    $(".product").removeClass("d-none");
    $(".images, .article, .promotions").addClass("d-none");
    $(".promotions").removeClass("d-none");
}

/**
 * Resets the UI for ARTICLE type selection.
 */
function resetViewForArticles() {
    $("#order").val("");
    $(".article").removeClass("d-none");
    $(".product, .offer, .category, .brand, .images, .promotions").addClass("d-none");
}

/**
 * Resets the UI for IMAGE type selection.
 */
function resetViewForImages() {
    $(".article, .product, .offer, .category, .brand, .promotions").addClass("d-none");
    $(".images").removeClass("d-none");
}

/**
 * Fetches the selected entity data by type and names.
 * @param {Array} selectedNames - The selected names.
 * @param {string} type - The type of entity (ARTICLE, PROMOTION, IMAGE).
 * @returns {Promise<Array>} - The fetched entities.
 */
async function fetchEntityByName(selectedNames, type) {
    const url = urls[type];
    if (!url) throw new Error(`Invalid type provided: ${type}`);

    try {
        const csrfToken = $('input[name="_csrf"]').val();
        let response;

        if (type === "PROMOTION" || type === "ARTICLE") {
            response = await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-Token": csrfToken,
                },
                body: JSON.stringify(selectedNames),
            });
        } else {
            response = await fetch(url);
        }

        if (!response.ok) throw new Error("Failed to fetch entities");

        return await response.json();
    } catch (error) {
        console.error("Error fetching entity by name:", error);
        throw error;
    }
}

/**
 * Handles the carousel rendering for IMAGE type entities.
 * @param {Array} carouselImageList - List of carousel images.
 */
function handleImageCarousel(carouselImageList) {
    if (carouselImageList) {
        $("#slideContainer").empty(); // Clear previous slides
        generateImageSlideMarkups(carouselImageList); // Generate new slides
        window.swiper.update();  // Update Swiper
        updateListEntityInput(carouselImageList);  // Update entity input
        $("#carouselImage").val("");  // Clear the image input
    }
}

/**
 * Generates markup for the image slides.
 * @param {Array} carouselImageList - List of carousel images.
 */
function generateImageSlideMarkups(carouselImageList) {
    const $slideContainer = $("#slideContainer");
    const entityOrder = getEntityOrder();

    if (entityOrder.length > 0) {
        carouselImageList.sort((a, b) => entityOrder.indexOf(a.id.toString()) - entityOrder.indexOf(b.id.toString()));
    }

    carouselImageList.forEach(image => {
        const markup = `
            <div class="swiper-slide" id="slide${image.id}">
                <div class="position-relative bg-light">
                    <img src="${image.imagePath}" alt="Image preview" style="width: 100%; height:120px; object-fit:contain;" />
                    <a href="#" data-entity-id="${image.id}" class="link-primary position-absolute top-0 end-0  d-block">
                        <i class="fa-solid fa-xmark fa-xl text-primary" style="color: #ffffff;"></i>
                    </a>
                </div>
                <div>
                    ${image.article.title}
                </div>
            </div>
        `;
        $slideContainer.append(markup);
    });

    window.swiper.update(); // Ensure Swiper is updated with new slides
}

/**
 * Deletes a carousel image and refreshes the image list.
 * @param {number} imageId - ID of the image to delete.
 */
async function deleteCarouselImage(imageId) {
    const csrfToken = $('input[name="_csrf"]').val();

    try {
        const response = await fetch(`${MODULE_URL}carousel_images/delete/${imageId}`, {
            method: "DELETE",
            headers: {
                "X-CSRF-TOKEN": csrfToken,
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            const errorMessage = await response.text();
            throw new Error(response.status === 404 ? `Image not found: ${errorMessage}` : `Error deleting image: ${errorMessage}`);
        }

        console.log("Image deleted successfully");
    } catch (error) {
        console.error("Failed to delete image:", error);
    }
}

/**
 * Loads products based on the selected promotion.
 * @param {string} selectedText - The selected promotion text.
 */
async function loadPromotionProducts(selectedText) {
    const url = `${MODULE_URL}promotions/${selectedText}`;

    try {
        const response = await fetch(url);
        const promotion = await response.json();

        const $productContainer = $("#products");
        $productContainer.empty();

        promotion.products.forEach(product => {
            const option = `<option value="${product.id}">${product.name}</option>`;
            $productContainer.append(option);
        });
    } catch (error) {
        console.error("Failed to load promotion products:", error);
    }
}

/**
 * Initializes the Swiper instance.
 */
function initializeSwiper() {
    window.swiper = new Swiper(".mySwiper", {
        slidesPerView: 1,
        spaceBetween: 10,
        pagination: {
            el: ".swiper-pagination",
            clickable: true,
        },
        breakpoints: {
            640: {
                slidesPerView: 2,
                spaceBetween: 20,
            },
            768: {
                slidesPerView: 4,
                spaceBetween: 40,
            },
            1024: {
                slidesPerView: 6,
                spaceBetween: 50,
            },
        }
    });
}

/**
 * Adds a new image to the carousel slider.
 * @param {Event} event - The event triggered by the file input.
 */
function addImageToSlider(event) {
    const files = event.target.files;
    if (files.length > 0) {
        const file = files[0];
        const articleId = $("#carouselImageArticle").val();

        if (file.type.startsWith("image/") && articleId) {
            uploadImage(file, articleId)
                .then(() => fetchEntityByName([], "IMAGE"))
                .then(handleImageCarousel)
                .catch((error) => {
                    console.error("Failed to upload image:", error);
                    alert(error.message);
                });
        } else {
            alert("Please select a valid image and ensure the article is attached.");
        }
    }
}

/**
 * Uploads the image to the server.
 * @param {File} file - The image file to upload.
 * @param {number} articleId - The associated article ID.
 * @returns {Promise<Object>} - Response from the server.
 */
async function uploadImage(file, articleId) {
    try {
    return await ajaxUtil.postBlob(`${MODULE_URL}carousel_images/upload?id=${articleId}`, {"file" : file});
    } catch(error) {
        console.error(error);
        showErrorModal(error.response);
    }
}

/**
 * Adds selected products to the carousel slider.
 * @param {Event} event - The change event triggered by selecting products.
 */
function addProductToSlider(event) {
    const selectedProducts = Array.from(event.target.selectedOptions).map(option => option.value);

    fetchEntityByName(selectedProducts, "PROMOTION")
        .then(handleProductCarousel)
        .catch((error) => {
            console.error("Error fetching products for carousel:", error);
            alert(error.message);
        });
}

/**
 * Handles product carousel rendering.
 * @param {Array} products - The list of products to be displayed in the carousel.
 */
function handleProductCarousel(products) {
    if (products) {
        generateProductSlideMarkups(products);
        updateListEntityInput(products);
        window.swiper.update(); // Update Swiper with new product slides
    }
}

/**
 * Generates the markup for product slides.
 * @param {Array} products - List of products to display.
 */
function generateProductSlideMarkups(products) {
    const $slideContainer = $("#slideContainer");
    $slideContainer.empty(); // Clear previous slides

    const entityOrder = getEntityOrder();

    // Sort products by entity order if present
    if (entityOrder.length > 0) {
        products.sort((a, b) => entityOrder.indexOf(a.id.toString()) - entityOrder.indexOf(b.id.toString()));
    }

    products.forEach(product => {
        const productCardHtml = generateProductCardMarkup(product);
        $slideContainer.append(productCardHtml);
    });
}

/**
 * Generates the HTML markup for a product card.
 * @param {Object} product - The product data to display.
 * @returns {string} - The generated HTML for the product card.
 */
function generateProductCardMarkup(product) {
    const discountBadge = product.discountPercent > 0
        ? `<span class="badge bg-danger position-absolute top-0 start-0 m-2">${product.discountPercent}% OFF</span>`
        : "";

    return `
    <div class="swiper-slide" id="slide${product.id}">
      <div class="card h-100">
        <div class="position-relative">
          <img src="${product.mainImagePath}" class="card-img-top" alt="${product.name}" style="object-fit: contain; height: 200px; padding: 10px;" />
          ${discountBadge}
        </div>
        <div class="card-body d-flex flex-column">
          <h5 class="card-title text-truncate" style="height: 3em;">${product.shortName}</h5>
          <h6 class="card-subtitle mb-2 text-muted">${product.brandName}</h6>
          <p class="card-text mt-auto">
            <strong>Price:</strong> $${product.price.toFixed(2)}<br>
            ${product.discountPercent > 0 ? `<strong>Discount Price:</strong> $${product.discountPrice.toFixed(2)}<br>` : ""}
          </p>
          <a href="#" class="btn btn-sm btn-primary mt-auto align-self-center">Add to Cart</a>
        </div>
      </div>
    </div>
  `;
}

/**
 * Adds selected articles to the carousel slider.
 * @param {Event} event - The change event triggered by selecting articles.
 */
function addArticleToSlider(event) {
    const selectedArticles = Array.from(event.target.selectedOptions).map(option => option.value);

    fetchEntityByName(selectedArticles, "ARTICLE")
        .then(handleArticleCarousel)
        .catch((error) => {
            console.error("Error fetching articles for carousel:", error);
            alert(error.message);
        });
}

/**
 * Handles article carousel rendering.
 * @param {Array} articles - List of articles to be displayed in the carousel.
 */
function handleArticleCarousel(articles) {
    if (articles) {
        generateArticleSlideMarkups(articles);
        updateListEntityInput(articles);
        window.swiper.update(); // Update Swiper with new article slides
    }
}

/**
 * Generates the markup for article slides.
 * @param {Array} articles - List of articles to display.
 */
function generateArticleSlideMarkups(articles) {
    const $slideContainer = $("#slideContainer");
    $slideContainer.empty(); // Clear previous slides

    const entityOrder = getEntityOrder();

    if (entityOrder.length > 0) {
        articles.sort((a, b) => entityOrder.indexOf(a.id.toString()) - entityOrder.indexOf(b.id.toString()));
    }

    articles.forEach(article => {
        const articleCardHtml = generateArticleCardMarkup(article);
        $slideContainer.append(articleCardHtml);
    });
}

/**
 * Generates the HTML markup for an article card.
 * @param {Object} article - The article data to display.
 * @returns {string} - The generated HTML for the article card.
 */
function generateArticleCardMarkup(article) {
    return `
    <div class="swiper-slide" id="slide${article.id}">
      <a>
          <div>
              <img src="${article.mainImagePath}" style="height: 150px; object-fit: contain;" >
              <div class="text-start text-light-emphasis fs-6">
                <span>${article.title}</span>
              </div>
          </div>
      </a>
    </div>
    `;
}

/**
 * Updates the entity input order list based on the current carousel entities.
 * @param {Array} entities - The entities (articles, products, images) to update the list with.
 */
function updateListEntityInput(entities) {
    const entityOrder = getEntityOrder();
    const $carouselItemsContainer = $("#carouselItems");
    $carouselItemsContainer.empty(); // Clear existing input list

    if (entityOrder.length > 0) {
        entityOrder.forEach((entityId, index) => {
            const entity = entities.find(e => e.id === entityId);
            if (entity) {
                $carouselItemsContainer.append(generateEntityInputMarkup(index, entity.id));
            }
        });

        entities.forEach(entity => {
            if (!entityOrder.includes(entity.id)) {
                $carouselItemsContainer.append(generateEntityInputMarkup($carouselItemsContainer.children().length, entity.id));
            }
        });
    } else {
        entities.forEach((entity, index) => {
            $carouselItemsContainer.append(generateEntityInputMarkup(index, entity.id));
        });
    }
}

/**
 * Generates the markup for the input controls for carousel entity ordering.
 * @param {number} index - The index of the entity.
 * @param {number} id - The entity ID.
 * @returns {string} - The generated HTML markup for the entity input controls.
 */
function generateEntityInputMarkup(index, id) {
    const upCaret = `<a href="#" class="link-primary"><i class="fa-solid fa-caret-up"></i></a>`;
    const downCaret = `<a href="#" class="link-primary"><i class="fa-solid fa-caret-down"></i></a>`;

    return `
    <div class="d-flex gap-4 align-items-center">
        <strong>${index}</strong>
        <input type="number" class="form-control mb-3" name="carouselItems[${index}].entityId" value="${id}"/>
        <input type="hidden" name="carouselItems[${index}].order" value="${index}" class="d-none"/>
        <div>
            ${upCaret}${downCaret}
        </div>
    </div>
    `;
}

/**
 * Retrieves the current entity order for the carousel items.
 * @returns {Array} - The current order of entities as an array of IDs.
 */
function getEntityOrder() {
    const entityOrderElement = document.getElementById("entityOrder");
    if (entityOrderElement) {
        return Array.from(entityOrderElement.options).map(option => option.value);
    }
    return [];
}

/**
 * Swaps the carousel item positions based on user interaction.
 * @param {string} direction - The direction of the swap ("up" or "down").
 * @param {jQuery} $caret - The jQuery object for the caret element.
 */
function swapSliderInputs(direction, $caret) {
    const $container = $caret.closest("div .d-flex");
    const $adjacentContainer = direction === "down" ? $container.next() : $container.prev();

    const $input = $container.find(".form-control");
    const $adjacentInput = $adjacentContainer.find(".form-control");

    const inputValue = $input.val();
    $input.val($adjacentInput.val());
    $adjacentInput.val(inputValue);

    swapSliderPositions($input.val(), $adjacentInput.val());
}

/**
 * Swaps the positions of two slides in the carousel.
 * @param {string} fromId - The ID of the first slide.
 * @param {string} toId - The ID of the second slide.
 */
function swapSliderPositions(fromId, toId) {
    const $fromElement = $(`#slide${fromId}`);
    const $toElement = $(`#slide${toId}`);

    if ($fromElement.length && $toElement.length) {
        const $fromChildren = $fromElement.children().detach();
        const $toChildren = $toElement.children().detach();

        $fromElement.append($toChildren);
        $toElement.append($fromChildren);

        const fromElementId = $fromElement.attr("id");
        const toElementId = $toElement.attr("id");

        $fromElement.attr("id", toElementId);
        $toElement.attr("id", fromElementId);
    }
}


