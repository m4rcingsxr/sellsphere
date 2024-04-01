"use strict";

$(function () {
    initializeDeleteEntityListener();
})

function initializeDeleteEntityListener() {
    $(".content").on('click', '#deleteEntity', function (event) {
        event.preventDefault();
        const deleteEntityURL = $(this).attr("href");

        const entityIDStartIndex = deleteEntityURL.lastIndexOf("/") + 1;
        const userId = deleteEntityURL.substring(entityIDStartIndex);

        showDeleteEntityModal(`Are you sure you want to delete entity with ID ${userId}`, deleteEntityURL);
    });
}


function showDeleteEntityModal(message, deleteURL) {
    showModalDialog('deleteModal', "Delete confirmation", message, 'text' ,deleteURL);
}




