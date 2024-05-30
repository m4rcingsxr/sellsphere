$(function() {
    initTooltip();

    $('#supported-countries').select2();

    initSupportedCountryChange();

    loadSupportedCountries();
    loadSupportedCurrencies($("#supported-countries").val());
})

function initTooltip() {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))
}

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

async function loadSupportedCurrencies(countryIds) {
    if (Array.isArray(countryIds) && countryIds.length) {
        const url = `${MODULE_URL}currencies`;

        // Fetch the available currencies from the server
        const availableCurrencies = await ajaxUtil.post(url, countryIds);
        const $currency = $("#currency");
        $currency.empty();

        const currentCurrencyId = $("#current-currency").val();

        let currentCurrencyExists = false;

        availableCurrencies.forEach(currency => {
            // Check if the current currency exists in the available currencies
            if (currency.id == currentCurrencyId) {
                currentCurrencyExists = true;
            }

            // Append the currency options to the dropdown
            $currency.append(
                `<option value="${currency.id}" ${currency.id == currentCurrencyId ? 'selected' : ''}>
                    ${currency.name} (${currency.code})
                </option>`
            );
        });

        // If the current currency doesn't exist in the available currencies, you can set the default selected value
        if (!currentCurrencyExists) {
            // Optionally, select the first currency in the list
            if (availableCurrencies.length > 0) {
                $currency.val(availableCurrencies[0].id);
            }
        }
    }
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