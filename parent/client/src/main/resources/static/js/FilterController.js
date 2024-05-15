"use strict";

/**
 * Controller class for managing filter interactions and updates.
 * This class handles initializing filters, updating the view based on filter changes,
 * and handling pagination and sorting events.
 */
class FilterController {
    constructor(model, view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Initializes the controller by loading filters from the URL and setting up event listeners.
     */
    init() {
        this.loadFiltersFromUrl();
        this.initEventListeners();
    }

    /**
     * Loads filters from the URL and fetches filter counts.
     * On success, it updates the view with the current filters and hides the spinner.
     * On failure, it shows an error modal.
     */
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
        this.initRemoveActiveFilterListener();
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
            this.handlePageChange(pageNum);
        });

        $("#pagination").on("change", ".page-input", event => {
            const pageNum = $(event.target).val() - 1;
            this.handlePageChange(pageNum);
        });

        $("#pagination").on("click", "#first", event => {
            event.preventDefault();
            this.handlePageChange(0);
        })

        $("#pagination").on("click", "#last", event => {
            event.preventDefault();
            this.handlePageChange(this.model.totalPages - 1);
        })
    }

    initRemoveActiveFilterListener() {
        $("#activeFilters").on("click", ".remove-filter", event => {
            this.view.removeActiveFilter(event.currentTarget);

            const $target = $(event.currentTarget);

            const checkboxValue = $target.data("filter-value");
            const checkbox = document.querySelector(`#filters input[value="${encodeURIComponent(checkboxValue)}"]`);

            this.handleFilterChange(checkbox);
        })
    }

    /**
     * Handles filter changes by updating the view and re-fetching the products based on the new filters.
     * @param {HTMLElement} target - The target element that triggered the filter change.
     */
    handleFilterChange(target ) {
        showFullScreenSpinner();
        this.model.synchronizeSingleFilterState(target);

        const minPrice = Number($("#lowerPrice").val());
        const maxPrice = Number($("#upperPrice").val());
        const filters = this.model.gatherProductSelectedFilters();
        const groupedFilters = this.model.groupFilters(filters);

        this.view.renderActiveFilters(groupedFilters);

        this.model.handleFilterChange(filters, 0, minPrice, maxPrice)
            .catch(error => showErrorModal(error.response))
            .finally(() => hideFullScreenSpinner());
    }

    /**
     * Handles page changes by updating the view and re-fetching the products based on the new page number.
     * @param {number} pageNum - The new page number to fetch.
     */
    handlePageChange(pageNum) {
        if (pageNum < 0 || pageNum > this.model.totalPages - 1) {
            showErrorModal({
                status: 400,
                message: `${pageNum > this.model.totalPages - 1 ? `Page number cannot be greater than ${this.model.totalPages}` : 'Page number must be greater than 0'}`
            });
        }

        showFullScreenSpinner();

        const minPrice = Number($("#lowerPrice").val());
        const maxPrice = Number($("#upperPrice").val());
        const filters = this.model.gatherProductSelectedFilters();

        this.model.handleFilterChange(filters, pageNum, minPrice, maxPrice)
            .catch(error => showErrorModal(error.response))
            .finally(() => hideFullScreenSpinner());
    }

}