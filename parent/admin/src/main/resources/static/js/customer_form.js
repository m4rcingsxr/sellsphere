"use strict";

$(function () {
    init();
    initListeners();
})

function init() {
    const addressId = $("#addresses").val();
    handleAddressesVisibilityChange(addressId);
    handleInitialStateLoadForCountries();
}

function handleInitialStateLoadForCountries() {
    $('select[id^="addresses"][id$=".country"]').each(function() {
        const countryId = $(this).val();
        const addressId = $(this).data("address-id");

        handleCountryChange(countryId, addressId);
    });
}

function initListeners() {
    initAddressChangeListener();
    initCountryChangeListener();
    initAddressPrimaryChangeListener();
}

function initAddressChangeListener() {
    $("#addresses").on("change", function () {
        const addressId = this.value;
        handleAddressesVisibilityChange(addressId);
    });
}

function initCountryChangeListener() {
    $('[id^="addresses"][id*=".country"]').on('change', function () {

        const addressId = $(this).data("address-id");
        const countryId = this.value;

        handleCountryChange(countryId, addressId);
    });
}

function initAddressPrimaryChangeListener() {

    $('[id^="addresses"][id*=".primary"]').on("change", function() {
        const checkboxId = $(this).attr("id");
        $('[id^="addresses"][id*=".primary"]').each(function() {
            if(checkboxId !== $(this).attr("id")) {
                this.checked = false;
            }
        })
    })
}

function handleAddressesVisibilityChange(addressId) {

    // Hide all address containers
    $('[id^="addressContainer"]').addClass("d-none");

    // show required address container:
    $(`#addressContainer${addressId}`).removeClass("d-none");
}

function handleCountryChange(countryId, addressId) {
    const stateDataList = $(`#states${addressId}`);
    stateDataList.empty();

    showFullScreenSpinner();
    fetchStatesForCountry(countryId)
        .then(states => {
            states.forEach(state => {
                const html = generateStateOptionHtml(state);
                stateDataList.append(html);
            })
        })
        .catch(error => showErrorModal(error.response))
        .finally(hideFullScreenSpinner)
}

async function fetchStatesForCountry(countryId) {
    try {
        const url = `${MODULE_URL}states/by_country/${countryId}`;
        return await ajaxUtil.get(url);
    } catch (error) {
        console.error(error);
        throw error;
    }
}

function generateStateOptionHtml(state) {
    return `
        <option value="${state.name}">${state.name}</option>
    `;
}