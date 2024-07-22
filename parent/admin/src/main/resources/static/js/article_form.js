$(function () {
    init();
});

/**
 * Initializes the form and event listeners.
 */
async function init() {
    try {
        const articleType = $("#articleType").val();
        showArticleForm(articleType);

        await handleChangePromotionArticleType($("#save-promotion-article-type").val());
        await handleChangePromotionProductType($("#promotion-type").val());
        await handleChangeOfExistingPromotions($("#existing-promotions-input").val());

        initListeners();
    } catch (error) {
        handleInitializationError(error);
    }
}

/**
 * Initializes all event listeners for the form elements.
 */
function initListeners() {
    $("#articleType").on("change", function () {
        showArticleForm(this.value);
    });

    $("#navigation").on("click", ".nav-link", handleSelectNavigationItemNumber);
    $("#footer").on("click", ".nav-link", handleSelectFooterItemNumber);

    $("#promotion-type").on("change", async function () {
        try {
            await handleChangePromotionProductType(this.value);
        } catch (error) {
            handleError(error);
        }
    });

    $("#save-promotion-article-type").on("change", async function () {
        try {
            await handleChangePromotionArticleType(this.value);
            togglePromotionSections(this.value);
        } catch (error) {
            handleError(error);
        }
    });

    $("#categories").on("change", async function () {
        try {
            await loadProductsForSelectedCategory(this.value);
        } catch (error) {
            handleError(error);
        }
    });

    $("#brands").on("change", async function () {
        try {
            await loadProductsForSelectedBrand(this.value);
        } catch (error) {
            handleError(error);
        }
    });

    $("#keyword").on("change", async function () {
        try {
            await loadProductsForKeyword();
        } catch (error) {
            handleError(error);
        }
    });

    $("#select-products").on("change", loadSelectedProducts);
    $("#selected-products").on("change", removeUnselectedOptions);

    $("#existing-promotions-input").on("change", async function () {
        try {
            await handleChangeOfExistingPromotions(this.value);
        } catch (error) {
            handleError(error);
        }
    });
}

/**
 * Displays the appropriate article form based on the selected article type.
 * @param {string} articleType - The selected article type.
 */
function showArticleForm(articleType) {
    hideAllArticleForms();

    switch (articleType) {
        case "NAVIGATION":
            showArticleNavigationForm();
            break;
        case "PROMOTION":
            showArticlePromotionForm();
            break;
        case "FOOTER":
            showArticleFooterForm();
            break;
        default:
            break;
    }
}

/**
 * Hides all article forms.
 */
function hideAllArticleForms() {
    $("#navigation, #save-promotion-type, #footer, #existing-promotions, #new-product, #selected-products-container").addClass("d-none");
}

/**
 * Displays the navigation article form.
 */
function showArticleNavigationForm() {
    $("#navigation").removeClass("d-none");
}

/**
 * Displays the promotion article form.
 */
async function showArticlePromotionForm() {
    $("#save-promotion-type, #selected-products-container").removeClass("d-none");
    try {
        await handleChangePromotionArticleType($("#save-promotion-article-type").val());
    } catch (error) {
        handleError(error);
    }
}

/**
 * Displays the footer article form.
 */
function showArticleFooterForm() {
    $("#footer").removeClass("d-none");
}

/**
 * Handles the selection of a navigation item.
 * @param {Event} event - The event object.
 */
function handleSelectNavigationItemNumber(event) {
    event.preventDefault();
    if ($(this).attr("disabled")) return;

    const currentIndex = this.dataset.index;
    const selectedOrder = $("#navigation-order").val();

    if (currentIndex === selectedOrder) return;

    if ($("#id").val() && selectedOrder) {
        $(`a[data-index="${selectedOrder}"]`).removeClass("selected").addClass("fw-lighter").text("empty");
    } else {
        $(".selected").addClass("fw-lighter").text("empty");
    }

    $("#navigation-order").val(currentIndex);
    $(this).removeClass("fw-lighter").addClass("selected").text("Current Article");
}

/**
 * Handles the selection of a footer item.
 * @param {Event} event - The event object.
 */
function handleSelectFooterItemNumber(event) {
    event.preventDefault();
    if ($(this).attr("disabled")) return;

    const currentItemNumber = this.dataset.itemNumber;
    const currentSectionNumber = this.dataset.sectionNumber;
    const selectedOrder = $("#navigation-order").val();
    const selectedSection = $("#section-order").val();

    if (currentItemNumber === selectedOrder && currentSectionNumber === selectedSection) return;

    if ($("#id").val() && selectedOrder && selectedSection) {
        $(`a[data-item-number="${selectedOrder}"][data-section-number="${selectedSection}"]`).removeClass("selected").addClass("fw-lighter").text("empty");
    } else {
        $(".selected").addClass("fw-lighter").text("empty");
    }

    $("#navigation-order").val(currentItemNumber);
    $("#section-order").val(currentSectionNumber);
    $(this).removeClass("fw-lighter").addClass("selected").text("Current Article");
}

/**
 * Handles the change of promotion product type.
 * @param {string} type - The selected promotion product type.
 */
async function handleChangePromotionProductType(type) {
    try {
        switch (type) {
            case "BRAND":
                await populateDropdown("#brands", fetchAllBrands);
                await loadProductsForSelectedBrand($("#brands").val());
                togglePromotionFilters(["#brands"], ["#keyword", "#categories"]);
                break;
            case "CATEGORY":
                await populateDropdown("#categories", fetchAllCategories);
                await loadProductsForSelectedCategory($("#categories").val());
                togglePromotionFilters(["#categories"], ["#keyword", "#brands"]);
                break;
            case "KEYWORD":
                togglePromotionFilters(["#keyword"], ["#brands", "#categories"]);
                $("#select-products").empty();
                break;
        }
    } catch (error) {
        handleError(error);
    }
}

/**
 * Populates a dropdown element with data from an async function.
 * @param {string} selector - The dropdown selector.
 * @param {function} fetchData - The async function to fetch data.
 */
async function populateDropdown(selector, fetchData) {
    try {
        const container = $(selector);
        container.empty();
        const data = await fetchData();
        data.forEach(item => {
            container.append(`<option value="${item.id}">${item.name}</option>`);
        });
    } catch (error) {
        handleError(error);
    }
}

/**
 * Toggles the visibility of promotion filters.
 * @param {Array<string>} showSelectors - Array of selectors to show.
 * @param {Array<string>} hideSelectors - Array of selectors to hide.
 */
function togglePromotionFilters(showSelectors, hideSelectors) {
    showSelectors.forEach(selector => $(selector).removeClass("d-none"));
    hideSelectors.forEach(selector => $(selector).addClass("d-none"));
}

/**
 * Toggles the visibility of promotion sections based on the selected type.
 * @param {string} type - The selected promotion article type.
 */
function togglePromotionSections(type) {
    if (type === 'EXISTING') {
        $("#existing-promotions").removeClass("d-none");
        $("#new-product").addClass("d-none");
    } else {
        $("#existing-promotions").addClass("d-none");
        $("#new-product").removeClass("d-none");
    }
}

/**
 * Fetches all brands.
 * @returns {Promise<Array<Object>>} - A promise that resolves to an array of brands.
 */
async function fetchAllBrands() {
    return ajaxUtil.get(`${MODULE_URL}brands/fetch-all`);
}

/**
 * Fetches all categories.
 * @returns {Promise<Array<Object>>} - A promise that resolves to an array of categories.
 */
async function fetchAllCategories() {
    return ajaxUtil.get(`${MODULE_URL}categories/fetch-all`);
}

/**
 * Loads products for the selected brand.
 * @param {string} brandId - The selected brand ID.
 */
async function loadProductsForSelectedBrand(brandId) {
    await loadProducts(`${MODULE_URL}products/brand/${brandId}`);
}

/**
 * Loads products for the selected category.
 * @param {string} categoryId - The selected category ID.
 */
async function loadProductsForSelectedCategory(categoryId) {
    await loadProducts(`${MODULE_URL}products/category/${categoryId}`);
}

/**
 * Loads products based on a URL.
 * @param {string} url - The URL to fetch products from.
 */
async function loadProducts(url) {
    try {
        const products = await ajaxUtil.get(url);
        const productContainer = $("#select-products");
        productContainer.empty();
        products.forEach(product => {
            productContainer.append(`<option value="${product.id}">${product.name}</option>`);
        });
    } catch (error) {
        handleError(error);
    }
}

/**
 * Loads products based on a keyword.
 */
async function loadProductsForKeyword() {
    try {
        const keyword = $("#keyword").val();
        if (!keyword) return;

        await loadProducts(`${MODULE_URL}products/search/${keyword}`);
    } catch (error) {
        handleError(error);
    }
}

/**
 * Adds selected products to the selected products list.
 */
function loadSelectedProducts() {
    const selectElement = document.getElementById('select-products');
    const selectedProducts = $("#selected-products");

    // Clear all existing options in selected-products first (if required)

    // Iterate over the options in select-products and add selected ones to selected-products
    [...selectElement.options].forEach(option => {
        if (option.selected && !selectedProducts.find(`option[value='${option.value}']`).length) {
            // Append the selected option to selected-products only if it doesn't already exist
            selectedProducts.append(`<option value="${option.value}" selected>${option.text}</option>`);
        }
    });
}

/**
 * Removes unselected options from the selected products list.
 */
function removeUnselectedOptions() {
    const selectElement = document.getElementById('selected-products');
    const selectedProducts = $("#selected-products");

    selectedProducts.find('option').each(function () {
        const value = $(this).val();
        const isSelectedInSource = [...selectElement.options].some(option => option.value === value && option.selected);
        if (!isSelectedInSource) {
            $(this).remove();
        }
    });
}

/**
 * Handles the change of promotion article type.
 * @param {string} type - The selected promotion article type.
 */
async function handleChangePromotionArticleType(type) {
    $("#selected-products").empty();
    if (type === 'EXISTING') {
        $("#existing-promotions").removeClass("d-none");
        const name = $("#existing-promotions-input").val();
        if (!name) return;

        $("#promotionName").val(name);
        await handleChangeOfExistingPromotions(name);
    } else {
        $("#promotionName").val("");
    }
}

/**
 * Handles the change of existing promotions by loading associated products.
 * @param {string} name - The selected promotion name.
 */
async function handleChangeOfExistingPromotions(name) {
    if (!name) return;

    try {
        const promotion = await ajaxUtil.get(`${MODULE_URL}promotions/${name}`);
        const products = $("#selected-products");
        products.empty();

        promotion.products.forEach(product => {
            products.append(`<option value="${product.id}" selected>${product.name}</option>`);
        });

        $('input[name="promotionName"]').val(name);
        console.log("Promotion name set to:", name);
    } catch (error) {
        handleError(error);
    }
}

/**
 * Handles errors during initialization.
 * @param {Error} error - The error object.
 */
function handleInitializationError(error) {
    console.error('Initialization failed:', error);
    showErrorModal(error.response);
}

/**
 * Handles generic errors in async operations.
 * @param {Error} error - The error object.
 */
function handleError(error) {
    console.error('An error occurred:', error);
    showErrorModal(error.response);
}
