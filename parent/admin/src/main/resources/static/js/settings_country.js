$(function () {
    loadCountries();
    initListeners();
});

/**
 * Initializes event listeners for various elements.
 */
function initListeners() {
    $("#countriesCountrySettings").on("change", handleSelectedCountry);
    $("#updateCountriesCountrySettings").on("click", loadCountries);
    $("#updateCountry").on("click", updateCountry);
    $("#saveCountry").on("click", saveNewCountry);
    $("#hideCountryForm").on("click", handleHideCountryForm);
    $("#deleteCountry").on("click", deleteCountry);
}

/**
 * Loads the list of countries and populates the country settings dropdown.
 */
async function loadCountries() {
    try {
        const countries = await fetchCountries();
        const $countries = $("#countriesCountrySettings");
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
 * Fetches the list of countries from the server.
 *
 * @returns {Promise<Object[]>} A promise that resolves to the list of countries.
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
 * Handles the selection of a country from the dropdown.
 */
function handleSelectedCountry() {
    const $option = $('#countriesCountrySettings option:selected');
    $("#country").val($option.text());
    $("#code").val($option.data("code"));
}

/**
 * Saves a country to the server.
 *
 * @param {Object} country - The country object.
 * @param {number} [country.id] - The ID of the country (optional for new countries).
 * @param {string} country.code - The code of the country.
 * @param {string} country.name - The name of the country.
 * @returns {Promise<void>}
 */
async function saveCountry(country) {
    try {
        handleHideCountryForm();
        const url = `${MODULE_URL}countries/save`;
        const savedCountry = await ajaxUtil.post(url, country);
        const message = country.id ? `Successfully updated the country ${savedCountry.name}.` : `Successfully saved the country ${savedCountry.name}`;
        await loadCountries();
        $("#countriesCountrySettings").val(savedCountry.id);
        handleSelectedCountry();
        showSuccessAlert(message);
    } catch (error) {
        console.error("Error during saving the country:", error.response);
        showErrorModal(error.response);
    }
}

/**
 * Hides the country form.
 */
function handleHideCountryForm() {
    $("#countryFormCollapse").collapse("hide");
}

/**
 * Deletes the selected country.
 *
 * @returns {Promise<void>}
 */
async function deleteCountry() {
    try {
        const countryId = $('#countriesCountrySettings').val();
        const url = `${MODULE_URL}countries/delete/${countryId}`;
        await ajaxUtil.getVoid(url);
        await loadCountries();
        showSuccessAlert(`Successfully removed country ${countryId}`);
    } catch (error) {
        console.error("Error during deleting the country:", error.response);
        showErrorModal(error.response);
    }
}

/**
 * Displays a success alert with a given message.
 *
 * @param {string} message - The message to display in the alert.
 */
function showSuccessAlert(message) {
    $("#countries").prepend(generateSuccessAlert(message));
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
 * Updates an existing country with new data.
 */
function updateCountry() {
    const $option = $('#countriesCountrySettings option:selected');
    const country = {
        id: $option.val(), code: $("#code").val(), name: $("#country").val()
    };
    saveCountry(country);
}

/**
 * Saves a new country to the server.
 *
 * @returns {Promise<void>}
 */
function saveNewCountry() {
    const countryName = $("#newCountryName").val();
    const countryCode = $("#newCountryCode").val();
    const country = { name: countryName, code: countryCode };
    saveCountry(country)
        .then(() => {
            $("#newCountryName").val("");
            $("#newCountryCode").val("");
        });
}
