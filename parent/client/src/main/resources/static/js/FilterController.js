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

            try {
                const filters = this.model.gatherProductSelectedFilters(event);
                this.model.handleFilterChange(filters)
                    .catch(error => showErrorModal(error.response))
                    .finally(() => hideFullScreenSpinner());
            } catch (error) {
                console.error(error.message);
                hideFullScreenSpinner();
            }
        });

        $("#showAllFilters").on("click", () => FilterView.toggleFilters());
        $(".viewProducts").on("click", () => FilterView.toggleFilters());
    }
}
