"use strict";

class FilterController {
    constructor(model) {
        this.model = model;
    }

    init() {
        const filters = this.model.extractFiltersFromUrl(window.location.href);
        this.model.fetchAndHandleFilterCounts(filters)
            .then(() => this.model.handleFilterChange(filters, 0))
            .catch(error => showErrorModal(error.response))
            .finally(() => hideFullScreenSpinner());

        this.initListeners();
    }

    initListeners() {
        $("#filters, #allFilters").on("click", ".filter", event => {
            showFullScreenSpinner();
            this.model.synchronizeSingleFilterState(event.target);

            const minPrice = Number($("#lowerPrice").val());
            const maxPrice = Number($("#upperPrice").val());

            const filters = this.model.gatherProductSelectedFilters();
            this.model.handleFilterChange(filters, 0, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        });

        $("#showAllFilters").on("click", () => FilterView.toggleFilters());
        $(".viewProducts").on("click", () => FilterView.toggleFilters());

        $('input[type="range"]').on("change", event => {
            showFullScreenSpinner();
            this.model.synchronizeSingleFilterState(event.target);

            const minPrice = Number($("#lowerPrice").val());
            const maxPrice = Number($("#upperPrice").val());

            const filters = this.model.gatherProductSelectedFilters();
            this.model.handleFilterChange(filters,0, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        })

        $("#sortBy").on("change", event => {
            showFullScreenSpinner();
            this.model.synchronizeSingleFilterState(event.target);

            const minPrice = Number($("#lowerPrice").val());
            const maxPrice = Number($("#upperPrice").val());

            const filters = this.model.gatherProductSelectedFilters();
            this.model.handleFilterChange(filters,0, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        });

        $("#pagination").on("click", ".page", event => {
            const pageNum = Number($(event.target).text()) - 1;

            showFullScreenSpinner();
            this.model.synchronizeSingleFilterState(event.target);

            const minPrice = Number($("#lowerPrice").val());
            const maxPrice = Number($("#upperPrice").val());

            const filters = this.model.gatherProductSelectedFilters();
            this.model.handleFilterChange(filters,pageNum, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        });

        $("#pagination").on("change", ".page-input", event => {
            const pageNum = $(event.target).val() - 1;

            showFullScreenSpinner();
            this.model.synchronizeSingleFilterState(event.target);

            const minPrice = Number($("#lowerPrice").val());
            const maxPrice = Number($("#upperPrice").val());

            const filters = this.model.gatherProductSelectedFilters();
            this.model.handleFilterChange(filters,pageNum, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        });
    }

}
