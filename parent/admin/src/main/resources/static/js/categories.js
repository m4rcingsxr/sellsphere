$(function () {
    initializeEventListeners();
});

function initializeEventListeners() {
    $(".content").on('click', '#statusEntity', handleStatusEntityClick);
    $(".content").on('click', '#deleteBranch', handleDeleteBranchClick);
}

function handleStatusEntityClick(event) {
    event.preventDefault();
    const statusURL = $(this).attr("href");
    const newStatus = getStatusFromURL(statusURL);
    const message = generateStatusMessage(newStatus);
    showModalDialog('statusModal', 'Warning', message, 'text', statusURL);
}

function handleDeleteBranchClick(event) {
    event.preventDefault();
    const deleteBranchURL = $(this).attr("href");
    const categoryId = extractIDFromURL(deleteBranchURL);
    const message = `Are you sure that you want to delete whole category ID: [${categoryId}] branch?`;
    showModalDialog('deleteModal', 'Delete confirmation', message, 'text', deleteBranchURL);
}

function getStatusFromURL(url) {
    return extractIDFromURL(url) === "true";
}

function extractIDFromURL(url) {
    return url.substring(url.lastIndexOf('/') + 1);
}

function generateStatusMessage(newStatus) {
    return `Are you sure you want to ${newStatus ? 'enable' : 'disable'} category? (${newStatus ? 'Enabling' : 'Disabling'} category will also ${newStatus ? 'enable' : 'disable'} all subcategories)`;
}