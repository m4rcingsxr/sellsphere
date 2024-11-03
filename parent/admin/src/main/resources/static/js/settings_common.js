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

/**
 * Fetches states from the server based on the selected country.
 * @returns {Promise<Array>} - A promise that resolves to an array of states.
 */
async function fetchStates(selectId) {
    try {
        const countryId = $(`#${selectId}`).val();
        const url = `${MODULE_URL}states/by_country/${countryId}`;
        debug(`Fetching states for country ID: ${countryId}`);
        const states = await ajaxUtil.get(url);
        debug(`States fetched: ${JSON.stringify(states)}`);
        return states;
    } catch (error) {
        console.error("Error fetching states:", error);
        throw new Error("Failed to fetch states.");
    }
}

/**
 * Loads states from the server and populates the state dropdown.
 */
async function loadStates(countrySelect, stateSelect) {
    try {
        debug("Loading states...");
        const $stateSelect = $(`#${stateSelect}`);
        const states = await fetchStates(countrySelect);

        $stateSelect.empty();
        states.forEach((state) => {
            $stateSelect.append(generateStateOptionHtml(state));
        });

        handleStateSelectionChange(stateSelect);
        debug("States loaded and dropdown populated.");
    } catch (error) {
        console.error("Error loading states:", error);
        throw new Error("Failed to load states.");
    }
}

/**
 * Handles state selection change by updating the state input field with the selected state name.
 */
function handleStateSelectionChange(stateSelect) {
    const selectedStateName = $(`#${stateSelect} option:selected`).text();
    debug(`State selection changed to: ${selectedStateName}`);
    $(`#${stateSelect}`).val(selectedStateName || "");
}