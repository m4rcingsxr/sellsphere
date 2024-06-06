"use strict";

$(function() {
    loadSupportedCurrencies($("#supported-countries").val());
})

async function loadSupportedCurrencies(countryIds) {
    if (Array.isArray(countryIds) && countryIds.length) {
        const url = `${MODULE_URL}currencies`;

        // Fetch the available currencies from the server
        const availableCurrencies = await ajaxUtil.post(url, countryIds);
        const $currency = $("#currencyconversion");
        $currency.empty();

        const currentCurrencyId = $("#current-currencyconversion").val();

        let currentCurrencyExists = false;

        availableCurrencies.forEach(currency => {
            // Check if the current currencyconversion exists in the available currencies
            if (currency.id == currentCurrencyId) {
                currentCurrencyExists = true;
            }

            // Append the currencyconversion options to the dropdown
            $currency.append(
                `<option value="${currency.id}" ${currency.id == currentCurrencyId ? 'selected' : ''}>
                    ${currency.name} (${currency.code})
                </option>`
            );
        });

        // If the current currencyconversion doesn't exist in the available currencies, you can set the default selected value
        if (!currentCurrencyExists) {
            // Optionally, select the first currencyconversion in the list
            if (availableCurrencies.length > 0) {
                $currency.val(availableCurrencies[0].id);
            }
        }
    }
}