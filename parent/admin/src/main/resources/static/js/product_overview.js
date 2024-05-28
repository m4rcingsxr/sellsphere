$(function () {
    // Get the selected brand ID from the brand dropdown
    const brandId = $("#brand").val();

    // Load categories associated with the selected brand
    loadCategoriesForBrand(brandId);

    // Set up a listener for when the brand selection changes
    initializeBrandChangeListener();


});

/**
 * Initializes an event listener on the brand dropdown.
 * When the selected brand changes, it triggers the loading of new categories.
 */
function initializeBrandChangeListener() {
    $("#brand").on("change", function () {
        // Load categories for the newly selected brand
        loadCategoriesForBrand(this.value);
    });
}

/**
 * Loads categories associated with the given brand ID.
 * Fetches the categories from the server and populates the category dropdown.
 *
 * @param {string} brandId - The ID of the selected brand.
 */
const loadCategoriesForBrand = async (brandId) => {
    if (brandId) {
        // Construct the URL to fetch categories for the selected brand
        const url = `${MODULE_URL}brands/${brandId}/categories`;

        try {
            // Show a spinner to indicate loading
            showFullScreenSpinner();

            // Fetch categories from the server
            const categories = await ajaxUtil.get(url);

            // Get the category dropdown and clear its current options
            const $categoryContainer = $("#category").empty();

            // Populate the category dropdown with the fetched categories
            categories.forEach(category => {
                const option = `<option value="${category.id}">${category.name}</option>`;
                $categoryContainer.append(option);
            });
        } catch (error) {
            // Log the error and show an error modal if the fetch fails
            console.error("Error during fetching categories:", error.response);
            showErrorModal(error.response);
        } finally {
            // Hide the spinner once loading is complete
            hideFullScreenSpinner();
        }
    }
}
