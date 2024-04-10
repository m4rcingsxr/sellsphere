$(function () {
    const brandId = $("#brand").val();

    // Loads categories associated with the selected brand.
    loadCategoriesForBrand(brandId);

    // Sets up a listener for when the brand selection changes.
    initializeBrandChangeListener();
});

/**
 * Initializes an event listener on the brand dropdown.
 * When the selected brand changes, it triggers the loading of new categories.
 */
function initializeBrandChangeListener() {
    $("#brand").on("change", function () {
        loadCategoriesForBrand(this.value);
    })
}

const loadCategoriesForBrand = async (brandId) => {
    if (brandId) {
        const url = `${MODULE_URL}brands/${brandId}/categories`;
        try {
            showFullScreenSpinner();
            const categories = await ajaxUtil.get(url);

            const $categoryContainer = $("#category").empty();
            categories.forEach(category => {
                const option = `<option value="${category.id}">${category.name}</option>`;
                $categoryContainer.append(option);
            });
        } catch (error) {
            console.error("Error during fetching categories:", error.response);
            showErrorModal(error.response);
        } finally {
            hideFullScreenSpinner();
        }
    }
}



