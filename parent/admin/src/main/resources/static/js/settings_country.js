$(function () {
    initializeCountrySettings();
});

function initializeCountrySettings() {
    showFullScreenSpinner();
    loadCountries('countriesCountrySettings')
        .catch(handleError)
        .finally(hideFullScreenSpinner);
    initCountryListeners();
}

/**
 * Initializes event listeners for various elements.
 */
function initCountryListeners() {
    debug("Country Setting up event listeners...");
    $("#countriesCountrySettings").on("change", handleSelectedCountry);
    $("#updateCountriesCountrySettings").on("click", () => updateCountries('countriesCountrySettings'));
    $("#updateCountry").on("click", updateCountry);
    $("#saveCountry").on("click", saveNewCountry);
    $("#hideCountryForm").on("click", handleHideCountryForm);
    $("#deleteCountry").on("click", deleteCountry);
    debug("Country Event listeners set up.");
}

/**
 * Handles the selection of a country from the dropdown.
 */
function handleSelectedCountry() {
    const $option = $('#countriesCountrySettings option:selected');
    setCountryForm($option.text(), $option.data("code"));
    debug(`Selected country: ${$option.text()} with code ${$option.data("code")}`);
}

/**
 * Sets the country form values.
 */
function setCountryForm(name, code) {
    $("#country").val(name);
    $("#code").val(code);
}

/**
 * Saves a country to the server.
 *
 * @param {Object} country - The country object.
 * @returns {Promise<void>}
 */
async function saveCountry(country) {
    handleHideCountryForm();
    showFullScreenSpinner();
    try {
        const url = `${MODULE_URL}countries/save`;
        const savedCountry = await ajaxUtil.post(url, country);
        await loadCountries('countriesCountrySettings');
        $("#countriesCountrySettings").val(savedCountry.id);
        handleSelectedCountry();
        showSuccessAlert(`Successfully ${country.id ? 'updated' : 'saved'} the country ${savedCountry.name}.`, 'countries');
        debug(`Successfully ${country.id ? 'updated' : 'saved'} the country ${savedCountry.name}.`);
    } catch (error) {
        handleError(error, "saving the country");
    } finally {
        hideFullScreenSpinner();
    }
}

/**
 * Hides the country form.
 */
function handleHideCountryForm() {
    $("#countryFormCollapse").collapse("hide");
    debug('Country form collapsed');
}

/**
 * Deletes the selected country.
 *
 * @returns {Promise<void>}
 */
async function deleteCountry() {
    showFullScreenSpinner();
    try {
        const countryId = $('#countriesCountrySettings').val();
        const url = `${MODULE_URL}countries/delete/${countryId}`;
        await ajaxUtil.getVoid(url);
        await loadCountries('countriesCountrySettings');
        showSuccessAlert(`Successfully removed country ${countryId}`, 'countries');
        debug(`Successfully removed country ${countryId}`);
    } catch (error) {
        handleError(error, "deleting the country");
    } finally {
        hideFullScreenSpinner();
    }
}

/**
 * Updates an existing country with new data.
 */
function updateCountry() {
    const $option = $('#countriesCountrySettings option:selected');
    const country = {
        id: $option.val(), code: $("#code").val(), name: $("#country").val()
    };
    debug(`Updating country: ${JSON.stringify(country)}`);
    saveCountry(country);
}

/**
 * Saves a new country to the server.
 *
 * @returns {Promise<void>}
 */
function saveNewCountry() {
    const country = {
        name: $("#newCountryName").val(),
        code: $("#newCountryCode").val()
    };
    debug(`Saving new country: ${JSON.stringify(country)}`);
    saveCountry(country).then(clearNewCountryForm);
}

/**
 * Clears the new country form.
 */
function clearNewCountryForm() {
    $("#newCountryName").val("");
    $("#newCountryCode").val("");
}

/**
 * Updates the list of countries.
 */
function updateCountries(selectInputId) {
    debug("Update countries country settings clicked.");
    showFullScreenSpinner();
    loadCountries(selectInputId)
        .catch(handleError)
        .finally(hideFullScreenSpinner);
}

/**
 * Handles errors and displays an error modal.
 */
function handleError(error, action = "performing the action") {
    console.error(`Error during ${action}:`, error);
    showErrorModal(error.response);
}