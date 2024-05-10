"use strict";

class FilterController {
    constructor(model, view) {
        this.model = model;
        this.view = view;
    }

    init() {
        this.loadFiltersFromUrl();
        this.initEventListeners();
    }

    loadFiltersFromUrl() {
        const filters = this.model.extractFiltersFromUrl(window.location.href);
        this.model.fetchAndHandleFilterCounts(filters)
            .then(() => this.model.handleFilterChange(filters, 0))
            .catch(error => showErrorModal(error.response))
            .finally(() => hideFullScreenSpinner());
    }

    initEventListeners() {
        this.initFilterListeners();
        this.initToggleFilterUIListeners();
        this.initPriceRangeListeners();
        this.initSortByListener();
        this.initPaginationListeners();
    }

    initFilterListeners() {
        $("#filters, #allFilters").on("click", ".filter", event => {
            this.handleFilterChange(event.target)
        });
    }

    initToggleFilterUIListeners() {
        $("#showAllFilters").on("click", () => this.view.toggleFilters());
        $(".viewProducts").on("click", () => this.view.toggleFilters());
    }

    initPriceRangeListeners() {
        $('input[type="range"]').on("change", event => {
            this.handleFilterChange(event.target);
        });
    }

    initSortByListener() {
        $("#sortBy").on("change", event => {
            this.handleFilterChange(event.target);
        });
    }

    initPaginationListeners() {
        $("#pagination").on("click", ".page", event => {
            const pageNum = Number($(event.target).text()) - 1;
            this.handlePageChange(event.target, pageNum);
        });

        $("#pagination").on("change", ".page-input", event => {
            const pageNum = $(event.target).val() - 1;
            this.handlePageChange(event.target, pageNum);
        });
    }

    handleFilterChange() {
        showFullScreenSpinner();
        this.model.synchronizeSingleFilterState(target);

        const minPrice = Number($("#lowerPrice").val());
        const maxPrice = Number($("#upperPrice").val());
        const filters = this.model.gatherProductSelectedFilters();

        this.model.handleFilterChange(filters, 0, minPrice, maxPrice)
            .catch(error => showErrorModal(error.response))
            .finally(() => hideFullScreenSpinner());
    }

    handlePageChange(target, pageNum) {
        showFullScreenSpinner();
        this.model.synchronizeSingleFilterState(target);

        const minPrice = Number($("#lowerPrice").val());
        const maxPrice = Number($("#upperPrice").val());
        const filters = this.model.gatherProductSelectedFilters();

        this.model.handleFilterChange(filters, pageNum, minPrice, maxPrice)
            .catch(error => showErrorModal(error.response))
            .finally(() => hideFullScreenSpinner());
    }

}
