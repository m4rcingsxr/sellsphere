"use strict";

$(function () {
    const $categories = $("#categories");

    showChosenCategories();
    $categories.change(showChosenCategories);
});

function showChosenCategories() {
    const $categories = $("#categories");
    const $badges = $("#badges");

    $badges.empty();

    $categories.children("option:selected").each(function () {
        const $selectedCategory = $(this);
        const categoryName = removeLeadingDashFromCategory($selectedCategory.text());

        const badgeHtml = `<span class="badge large bg-dark-subtle fs-6">${categoryName}</span>`;
        $badges.append(badgeHtml);
    });
}

function removeLeadingDashFromCategory(text) {
    return text.replace(/-/g, "");
}
