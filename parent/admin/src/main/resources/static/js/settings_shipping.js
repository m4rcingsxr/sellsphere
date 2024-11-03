"use strict";

$(function () {
    initializeCountryShipping();

    // Event listener for country change
    $("#countriesShippingSettings").on("change", async function () {
        await loadStates("countriesShippingSettings", "statesShippingSettings");
        selectFirstAvailableState();  // Select the first available state
        updateCountryHiddenField();   // Update the hidden country field with the selected value
    });

    // Event listener for state change
    $("#statesShippingSettings").on("change", function () {
        updateStateHiddenField();   // Update the hidden state field with the selected text
    });
});

async function initializeCountryShipping() {
    try {
        await loadCountries("countriesShippingSettings");
        await selectCountryFromHiddenValue();  // Select the correct country
        await loadStates("countriesShippingSettings", "statesShippingSettings");
        await selectStateFromHiddenValue();  // Select the correct state or the first state
    } catch (error) {
        console.error(error);
        showErrorModal(error.response);
    }
}

async function selectCountryFromHiddenValue() {
    const countryId = $("#ORIGIN_ADDRESS_COUNTRY").val();
    $("#countriesShippingSettings").val(countryId);  // Set the country dropdown value
}

async function selectStateFromHiddenValue() {
    const stateName = $("#ORIGIN_ADDRESS_STATE").val();
    if (stateName) {
        // Loop through the options and select the one that matches the hidden state name
        $("#statesShippingSettings option").each(function () {
            if ($(this).text() === stateName) {
                $(this).prop("selected", true);
            }
        });
    } else {
        selectFirstAvailableState();  // If no state is provided, select the first available one
    }
}

function selectFirstAvailableState() {
    const $statesDropdown = $("#statesShippingSettings");
    const firstStateOption = $statesDropdown.find("option:first");

    if (firstStateOption.length) {
        firstStateOption.prop("selected", true);
        updateStateHiddenField();  // Update the hidden state field with the first state's text
    }
}

function updateCountryHiddenField() {
    const selectedCountryValue = $("#countriesShippingSettings").val();
    $("#ORIGIN_ADDRESS_COUNTRY").val(selectedCountryValue);  // Update the hidden country field
}

function updateStateHiddenField() {
    const selectedStateText = $("#statesShippingSettings option:selected").text();
    $("#ORIGIN_ADDRESS_STATE").val(selectedStateText);  // Update the hidden state field
}

