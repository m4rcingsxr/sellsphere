"use strict";

$(function () {
    initializeDeleteEntityListener();
    initializeDetailEntityListener();
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

function initializeDetailEntityListener() {
    $(".content").on('click', '.detailEntity', function (event) {
        event.preventDefault();
        const detailURL = $(this).attr("href");

        handleDetailShow(detailURL);
    });
}

function showDeleteEntityModal(message, deleteURL) {
    showModalDialog('deleteModal', "Delete confirmation", message, 'text' ,deleteURL);
}

async function handleDetailShow(detailURL) {
    try {
        const responseHtml = await ajaxUtil.getText(detailURL);
        showModalDialog('detailModal', 'Details', responseHtml, 'html');
    } catch(error) {
        console.error("Error during image compression:", error.response);
        showErrorModal(error.response);
    }
}

