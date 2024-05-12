"use strict";

class FilterModel {
    constructor(view) {
        this.totalPages = 0;
        this.currentPage = 0;
        this.view = view;
    }

    extractFiltersFromUrl(url) {
        const urlParams = new URLSearchParams(new URL(url).search);
        return [...urlParams.entries()].filter(([key]) => key === 'filter').map(([, value]) => value);
    }

    async fetchAndHandleFilterCounts(filters) {
        try {
            showFullScreenSpinner();
            const formattedFilters = this.formatFilters(filters);
            const countMap = await this.fetchFilterCounts(formattedFilters);
            this.view.renderProductFilters(countMap, filters);
            this.view.renderAllFilters(countMap);
            this.view.renderAllFilterNames(countMap);
        } catch (error) {
            throw error;
        } finally {
            hideFullScreenSpinner();
        }
    }

    async handleFilterChange(filters, pageNum, minPrice, maxPrice) {
        try {
            await this.fetchAndDisplayProducts(filters, pageNum, minPrice, maxPrice);
            await this.updateFilterDisplay(filters, minPrice, maxPrice);
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

    async fetchAndDisplayProducts(filters, pageNum, minPrice, maxPrice) {
        try {
            const formattedFilters = this.formatFilters(filters);
            const productsPage = await this.fetchProductsPage(formattedFilters, pageNum, minPrice, maxPrice);

            this.totalPages = productsPage.totalPages;
            this.currentPage = productsPage.page;

            this.view.renderProducts(productsPage);
            this.view.setPriceBoundaries(productsPage.minPrice, productsPage.maxPrice);
            this.view.renderPagination(productsPage);

        } catch (error) {
            throw error;
        }
    }


    async updateFilterDisplay(filters, minPrice, maxPrice) {
        try {
            const countMap = await this.fetchFilterCounts(filters, minPrice, maxPrice);
            this.view.renderAllFilters(countMap);
            this.view.renderProductFilters(countMap, filters);
            this.view.checkFilters(filters);
        } catch (error) {
            throw error;
        }
    }

    formatFilters(filters) {
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

    async fetchProductsPage(filters, pageNum, minPrice, maxPrice) {
        const baseUrl = `${MODULE_URL}filter/products`;
        const fullUrl = this.buildPageRequestUrl(baseUrl, filters, minPrice, maxPrice, pageNum);
        return await ajaxUtil.get(fullUrl);
    }

    gatherProductSelectedFilters() {
        const selectedFiltersSet = new Set();
        document.querySelectorAll('#filters > div > .d-flex.flex-column.gap-1.mt-2, #allFilters .row.g-3.mt-1').forEach(group => {
            const filterName = group.previousElementSibling.textContent.trim();
            const selectedCheckboxes = group.querySelectorAll('input.form-check-input:checked');
            selectedCheckboxes.forEach(checkbox => {
                let value = decodeURIComponent(checkbox.value).trim();
                selectedFiltersSet.add(`${filterName},${value}`);
            });
        });

        return Array.from(selectedFiltersSet);
    }

    async fetchFilterCounts(filters, minPrice, maxPrice) {
        const baseUrl = `${MODULE_URL}filter/filter_counts`;
        const pageRequestUrl = this.buildPageRequestUrl(baseUrl, filters, minPrice, maxPrice, 0);
        return await ajaxUtil.get(pageRequestUrl);
    }

    buildPageRequestUrl(baseUrl, filters, minPrice, maxPrice, pageNum) {
        const params = new URLSearchParams();
        const sortBy = $("#sortBy").val();

        params.append("sortBy", sortBy);

        if (pageNum !== null && pageNum !== undefined) {
            params.append("pageNum", pageNum);
        }
        filters?.forEach(filter => params.append("filter", filter));

        if (minPrice && maxPrice) {
            params.append("minPrice", minPrice);
            params.append("maxPrice", maxPrice);
        }

        const url = new URL(window.location.href);
        const pathname = url.pathname;
        const keywordParam = url.searchParams.get('keyword');
        if (keywordParam) {
            params.append("keyword", keywordParam);
        } else if (pathname.includes('/c/')) {
            params.append("category_alias", decodeURIComponent(pathname.split('/c/')[1].split('/')[0]));
        } else if (pathname.includes('/p/search')) {
            params.append("keyword", decodeURIComponent(pathname.split('/p/search/')[1].split('/')[0]));
        } else {
            throw new Error("Not supported URL. Supported['/c/{category_alias}','/p/search/{keyword}']");
        }

        return `${baseUrl}?${params.toString()}`;
    }

    /**
     * Groups filters by name and collects corresponding values into arrays.
     * @param {Array} filters - The array of filters in the format [name,value, name,value, ...].
     * @returns {Object} - The grouped filters as an object with names as keys and arrays of values.
     */
    groupFilters(filters) {
        return filters.reduce((acc, filter) => {
            const [name, value] = filter.split(',');
            if (!acc[name]) {
                acc[name] = [];
            }
            acc[name].push(value);
            return acc;
        }, {});
    }
}
