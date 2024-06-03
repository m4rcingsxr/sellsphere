$(function() {
    initTooltip();

    $('#supported-countries').select2();

    initSupportedCountryChange();

    loadSupportedCountries();
})



function initSupportedCountryChange() {
    $("#supported-countries").on("change", () => {
        // refresh currencies
        saveSupportedCountries()
            .then(() => {
                return loadSupportedCurrencies($("#supported-countries").val());
            })
            .catch(error => {
                console.error(error);
                showErrorModal(error.response);
            })
    });
}



async function loadSupportedCountries() {
    const url = `${MODULE_URL}countries/support`;
    const countries = await ajaxUtil.get(url);

    const $supportedCountries = $('#supported-countries');

    const countryIdsToSelect = []; // Array to hold IDs of countries to be selected

    countries.forEach(country => {
        // Check if the option exists in the preloaded select element
        if ($supportedCountries.find(`option[value="${country.id}"]`).length > 0) {
            countryIdsToSelect.push(country.id);
        }
    });

    // Set the selected values in the Select2 dropdown
    $supportedCountries.val(countryIdsToSelect).trigger('change');
}

async function saveSupportedCountries() {
    const url = `${MODULE_URL}countries/support`;
    await ajaxUtil.post(url, $("#supported-countries").val());
}