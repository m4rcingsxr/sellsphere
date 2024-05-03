"use strict";

class FilterModel {
    extractFiltersFromUrl(url) {
        const urlParams = new URLSearchParams(new URL(url).search);
        return [...urlParams.entries()].filter(([key]) => key === 'filter').map(([, value]) => value);
    }

    async fetchAndHandleFilterCounts(filters) {
        try {
            showFullScreenSpinner();
            const formattedFilters = this.formatFiltersToIncludeCommaValues(filters);
            const countMap = await this.fetchFilterCounts(formattedFilters);
            FilterView.renderProductFilters(countMap, filters);
            FilterView.renderAllFilters(countMap);
            FilterView.renderAllFilterNames(countMap);
        } catch (error) {
            throw error;
        } finally {
            hideFullScreenSpinner();
        }
    }

    async handleFilterChange(filters) {
        try {
            await this.fetchAndHandleProducts(filters, 0);
            await this.updateFilterDisplay(filters);
            console.info("Successfully handled filter change");
        } catch (error) {
            throw error;
        }
    }

    synchronizeSingleFilterState(target) {
        const value = target.value;
        const isChecked = target.checked;
        const containerToSync = target.closest('#filters') ? '#allFilters' : '#filters';

        const matchingCheckbox = document.querySelector(`${containerToSync} .form-check-input[value="${value}"]`);
        if (matchingCheckbox) {
            matchingCheckbox.checked = isChecked;
        }
    }

    async fetchAndHandleProducts(filters, pageNum) {
        try {
            const formattedFilters = this.formatFiltersToIncludeCommaValues(filters);
            const productsPage = await this.fetchProductsPage(formattedFilters, pageNum);
            FilterView.renderProducts(productsPage);
        } catch (error) {
            throw error;
        }
    }

    async updateFilterDisplay(filters) {
        try {
            const countMap = await this.fetchFilterCounts(filters);
            FilterView.renderAllFilters(countMap);
            FilterView.renderProductFilters(countMap, filters);
            FilterView.checkFilters(filters);
        } catch (error) {
            throw error;
        }
    }

    formatFiltersToIncludeCommaValues(filters) {
        if (filters != null) {
            return filters.map(filter => {
                const [name, value] = filter.split(/,(.+)/);
                const formattedValue = value.includes(',') ? `'${value}'` : value;
                return `${name},${formattedValue}`;
            });
        } else {
            return filters;
        }
    }

    async fetchProductsPage(filters, pageNum) {
        const baseUrl = `${MODULE_URL}filter/products`;
        const fullUrl = this.buildPageRequestUrl(baseUrl, filters, pageNum);
        return await ajaxUtil.get(fullUrl);
    }

    gatherProductSelectedFilters(event) {
        const selectedFiltersSet = new Set();
        document.querySelectorAll('#filters > div > .d-flex.flex-column.gap-1.mt-2, #allFilters .row.g-3.mt-1').forEach(group => {
            const filterName = group.previousElementSibling.textContent.trim();
            const selectedCheckboxes = group.querySelectorAll('input.form-check-input:checked');

            if (selectedCheckboxes.length > 1) {
                event.target.checked = false;
                throw new Error(`Multiple values selected for the filter: ${filterName}`);
            }

            selectedCheckboxes.forEach(checkbox => {
                let value = decodeURIComponent(checkbox.value).trim();
                selectedFiltersSet.add(`${filterName},${value}`);
            });
        });

        return Array.from(selectedFiltersSet);
    }

    async fetchFilterCounts(filters) {
        const baseUrl = `${MODULE_URL}filter/all_counts`;
        const pageRequestUrl = this.buildPageRequestUrl(baseUrl, filters);
        return await ajaxUtil.get(pageRequestUrl);
    }

    buildPageRequestUrl(baseUrl, filters, pageNum) {
        const params = new URLSearchParams();
        if (pageNum !== null && pageNum !== undefined) {
            params.append("pageNum", pageNum);
        }
        filters?.forEach(filter => params.append("filter", filter));

        const url = new URL(window.location.href);
        const pathname = url.pathname;
        if (pathname.includes('/c/')) {
            params.append("category_alias", pathname.split('/c/')[1].split('/')[0]);
        } else if (pathname.includes('/p/search/')) {
            params.append("keyword", pathname.split('/p/search/')[1].split('/')[0]);
        } else {
            throw new Error("Not supported URL. Supported['/c/{category_alias}','/p/search/{keyword}']");
        }

        return `${baseUrl}?${params.toString()}`;
    }
}
