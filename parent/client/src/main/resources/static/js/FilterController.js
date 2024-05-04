"use strict";

class FilterController {
    constructor(model) {
        this.model = model;
    }

    init() {
        const filters = this.model.extractFiltersFromUrl(window.location.href);
        this.model.fetchAndHandleFilterCounts(filters)
            .then(() => this.model.handleFilterChange(filters))
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
            this.model.handleFilterChange(filters, minPrice, maxPrice)
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
            this.model.handleFilterChange(filters, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        })

        $("#sortBy").on("change", event => {
            showFullScreenSpinner();
            this.model.synchronizeSingleFilterState(event.target);

            const minPrice = Number($("#lowerPrice").val());
            const maxPrice = Number($("#upperPrice").val());

            const filters = this.model.gatherProductSelectedFilters();
            this.model.handleFilterChange(filters, minPrice, maxPrice)
                .catch(error => showErrorModal(error.response))
                .finally(() => hideFullScreenSpinner());
        });
    }

}
