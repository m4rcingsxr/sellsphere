"use strict";

$(function() {
    initializeProductTaxSelect2();

    const type = $("#product-tax-category").val();

    // Load product taxes associated with the selected type
    loadProductTaxesForType(type)
        .then(handleLoadTaxes)
        .catch(error => {
            console.error(error);
            showErrorModal(error.response)
        });

    initializeTaxCategoryChangeListener();
})

function initializeProductTaxSelect2() {
    function formatState(state) {
        if (!state.id) {
            return state.text;
        }

        return $(
            '<span><strong>' + state.text + '</strong><br><small>' + $(state.element).data('tax-description') + '</small></span>'
        );
    }

    $('#tax').select2({
        placeholder: 'Select a product tax',
        allowClear: true,
        templateResult: formatState,
        templateSelection: formatState
    });
}

function initializeTaxCategoryChangeListener() {
    $("#product-tax-category").on("change", function () {
        const type = $(this).val();

        loadProductTaxesForType(type)
            .then(handleLoadTaxes)
            .catch(error => {
                console.error(error);
                showErrorModal(error.response())
            });
    });
}

async function loadProductTaxesForType(type) {
    return await ajaxUtil.get(`${MODULE_URL}products/tax/${type}`)
}

function handleLoadTaxes(taxes) {
    const $datalist = $("#tax");

    $datalist.empty();

    taxes.forEach(tax => {
        $datalist.append(
            `<option data-tax-description="${tax.description}" value="${tax.id}">${tax.name}</option>`
        );
    });

}

