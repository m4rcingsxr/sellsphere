/**
 * Loads the list of countries and populates the country settings dropdown.
 */
async function loadCountries(selectInputId) {
    try {
        const countries = await fetchCountries();
        const $countries = $(`#${selectInputId}`);
        $countries.empty();
        countries.forEach((country) => {
            $countries.append(generateCountryOptionHtml(country));
        });
        handleSelectedCountry();
    } catch (error) {
        console.error("Error during loading the countries:", error.response);
        showErrorModal(error.response);
    }
}

/**
 * Fetches the list of countries from the server.
 *
 * @returns {Promise<Object>} A promise that resolves to the list of countries.
 * @throws Will throw an error if the fetch operation fails.
 */
async function fetchCountries() {
    try {
        const url = `${MODULE_URL}countries/list`;
        return await ajaxUtil.get(url);
    } catch (error) {
        console.error("Error during fetching the countries:", error.response);
        throw error;
    }
}

/**
 * Generates the HTML for a country option in the dropdown.
 *
 * @param {Object} country - The country object.
 * @param {number} country.id - The ID of the country.
 * @param {string} country.code - The code of the country.
 * @param {string} country.name - The name of the country.
 * @returns {string} The HTML string for the country option.
 */
function generateCountryOptionHtml(country) {
    return `<option value="${country.id}" data-code="${country.code}">${country.name}</option>`;
}

/**
 * Displays a success alert with a given message.
 *
 * @param {string} message - The message to display in the alert.
 * @param {string} containerId - Container in which to place the alert.
 */
function showSuccessAlert(message, containerId) {
    $(`#${containerId}`).prepend(generateSuccessAlert(message));
    debug(`Success alert displayed: ${message}`);
}

/**
 * Generates the HTML for a success alert.
 *
 * @param {string} message - The message to display in the alert.
 * @returns {string} The HTML string for the success alert.
 */
function generateSuccessAlert(message) {
    $(".alert").remove();
    return `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}