"use strict"

$(document).ready(function () {
    const $listGroup = $(".list-group");

    $listGroup.on("click", ".collapse-btn", function (event) {
        event.preventDefault();

        const collapseId = $(this).data("collapse-id");
        $(`#collapsePreview${collapseId}`).toggleClass("show");
        $(`#collapseForm${collapseId}`).toggleClass("show");

        $(`#collapseBtn${collapseId}`).toggleClass("d-none");
        $(`#deleteBtn${collapseId}`).toggleClass("d-none");

    });

    $listGroup.on("click", ".delete-btn", function (event) {
        event.preventDefault();

        $("#deleteModalBtn").attr("href", this.href);
        $("#deleteModal").modal('show');
    })

    $("#newAddress").on("click", function (event) {
        event.preventDefault();

        $("#staticBackdrop").modal("show");
    })

    $(".country").on("change", function (event) {
        const addressId = $(this).data("id");
        const countryId = $(this).val();

        let stateListId = `#states`;
        if (addressId) {
            stateListId = stateListId + addressId;
        }

        const $stateList = $(stateListId);

        $stateList.empty();
        fetchStates(countryId)
            .then(states => {
                states.forEach(state => {
                    $stateList.append(`<option value="${state.name}"></option>`);
                });
            })
    })
});

async function fetchStates(countryId) {

    try {
        const response = await fetch(`${MODULE_URL}states/list_by_country/${countryId}`)

        if (!response.ok) {
            throw new Error("Resposne was not ok!");
        }

        return await response.json();

    } catch (error) {
        throw error;
    }
}