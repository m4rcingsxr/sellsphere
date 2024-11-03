"use strict";

/**
 * View class for managing the rendering of products, filters, pagination, and UI toggling.
 * This class handles updating the DOM based on changes in the filters, displaying products,
 * and managing the UI elements related to filtering and pagination.
 */
class FilterView {

    /**
     * Renders the products on the page.
     * @param {Object} productsPage - The page of products to render.
     */
    renderProducts(productsPage) {
        const $products = $("#products").empty();
        productsPage.content.forEach(product => $products.append(this.generateProductHtml(product)));
    }

    /**
     * Generates the HTML for a single product.
     * @param {Object} product - The product to generate HTML for.
     * @returns {string} - The HTML string for the product.
     */

    generateProductHtml(product) {
        const formattedDiscountPrice = formatPriceUtil.formatPrice(product.discountPrice);
        const formattedPrice = formatPriceUtil.formatPrice(product.price);
        const productDetails = Array.isArray(product.details) ? product.details.slice(0, 5) : [];

        // Properly encode the product alias for URLs
        const encodedAlias = encodeURIComponent(product.alias);

        return `
            

        <div class="col-md-4">
            <div class="product-carousel-card p-2 rounded-2 position-relative">
                <div class="d-flex flex-column">
                <a href="${MODULE_URL}p/${encodedAlias}" class="product-carousel-img-container mt-4 ">
                    <img src="${product.mainImagePath}" class="card-img-top" alt="${product.name}">
                </a>
                
                <div class="mt-4 p-1">
                    <a href="/p/${encodedAlias}" class="link-dark link-underline link-underline-opacity-0 fs-7 product-title">
                        <span class="product-title">${product.name}</span>
                    </a>
                    <div class="d-flex gap-2 mt-auto">
                        <strong>${formattedDiscountPrice}</strong>
                        ${product.discountPercent > 0 ? `<span class="fw-lighter text-decoration-line-through">${formattedPrice}</span>` : ''}
                    </div>
                </div>
                
                ${product.discountPercent > 0 ? `<div class="position-absolute top-0 p-1"><span class="badge bg-danger text-center fs-7">-${product.discountPercent}%</span></div>` : ''}
                
                <div class="position-absolute top-0 end-0">
                    <a href="#" class="cart-icon link-dark d-block add-to-cart" data-product-id="${product.id}"><i class="bi bi-cart cart-icon-size"></i></a>
                    <a href="#" class="heart-icon link-dark"><i class="bi bi-heart cart-icon-size"></i></a>
                </div>
                
                <div class="product-carousel-card-details p-2">
                    <div class="row g-2">${productDetails.map(detail => `
                        <div class="col-8 detail"><span class="text-light-emphasis">${detail.name}:</span></div>
                        <div class="col-4 detail fw-bolder">${detail.value}</div>
                    `).join('')}</div>
                </div>
                </div>
            </div>
        </div>
    `;
    }


    /**
     * Toggles the visibility of filter and product sections.
     */
    toggleFilters() {
        $("#products, #allFilters, .viewProducts, #showAllFilters, #filters, #allFilterNames, #pagination").toggleClass("d-none");
    }

    /**
     * Renders all filter names based on the provided count map.
     * @param {Object} countMap - The map of filter counts.
     */
    renderAllFilterNames(countMap) {
        const allNamesHtml = Object.keys(countMap).map(name => `
            <a href="#allFilterNames${name}" class="list-group-item list-group-item-action bg-body-tertiary list-group-item-secondary filter-name">${name}</a>
        `).join('');

        $("#allFilterNames").empty().append(allNamesHtml);
    }

    /**
     * Renders product filters based on the provided count map and selected filters.
     * @param {Object} countMap - The map of filter counts.
     * @param {Array} filters - The selected filters.
     */
    renderProductFilters(countMap, filters) {
        let filtersHtml = '';
        let filterCount = 0;
        const filterNames = filters.map(f => f.split(',')[0]);

        for (const [name, values] of Object.entries(countMap)) {
            if (filterCount < 5 || filterNames.includes(name)) {
                filtersHtml += this.generateProductFilterHtml(name, values, filters);
                filterCount++;
            }
        }

        $('#filters').html(filtersHtml);
    }

    /**
     * Generates the HTML for a single product filter.
     * @param {string} name - The name of the filter.
     * @param {Object} values - The values and counts for the filter.
     * @param {Array} filters - The selected filters.
     * @returns {string} - The HTML string for the filter.
     */
    generateProductFilterHtml(name, values, filters) {
        const filterSet = new Set(filters);
        const filterEntries = Object.entries(values);

        return `
            <div>
                <span class="fw-bolder">${name}</span>
                <div class="d-flex flex-column gap-1 mt-2">
                    ${filterEntries.slice(0, 5).map(([value, count]) => `
                        <div class="form-check">
                            <input class="form-check-input filter" type="checkbox" data-name="${name}" value="${encodeURIComponent(value)}" id="${value}" ${count > 0 ? '' : 'disabled'} ${filterSet.has(`${name},${value}`) ? 'checked' : ''}>
                            <label class="form-check-label" for="${value}">(${count}) ${value}</label>
                        </div>
                    `).join('')}
                    ${filterEntries.length > 5 ? `
                        <div class="collapse " id="${name}-collapse">
                                ${filterEntries.slice(5).map(([value, count]) => `
                                    <div class="form-check">
                                        <input class="form-check-input filter" type="checkbox" data-name="${name}" value="${encodeURIComponent(value)}" id="${value}" ${count > 0 ? '' : 'disabled'} ${filterSet.has(`${name},${value}`) ? 'checked' : ''}>
                                        <label class="form-check-label" for="${value}">(${count}) ${value}</label>
                                    </div>
                                `).join('')}
                        </div>
                        <a href="#" class="link-dark link-underline-opacity-0 link-underline-opacity-50-hover py-1 px-4"  data-bs-toggle="collapse" data-bs-target="#${name}-collapse" aria-expanded="false" aria-controls="${name}-collapse">
                            Show More
                        </a>
                    ` : ''}
                </div>
            </div>
        `;
    }

    /**
     * Renders all filters based on the provided count map.
     * @param {Object} countMap - The map of filter counts.
     */
    renderAllFilters(countMap) {
        const filtersHtml = Object.entries(countMap).map(([name, values]) => this.generateListGroupItemHtmlForAllFilters(name, values)).join('');
        $('#allFilters').html(filtersHtml);
    }

    /**
     * Generates the HTML for a list group item containing all filter values.
     * @param {string} name - The name of the filter.
     * @param {Object} values - The values and counts for the filter.
     * @returns {string} - The HTML string for the list group item.
     */
    generateListGroupItemHtmlForAllFilters(name, values) {
        return `
            <li id="allFilterNames${name}" class="list-group-item p-4">
                <span class="fw-bolder">${name}</span>
                <div class="row g-3 mt-1">
                    ${Object.entries(values).map(([value, count]) => `
                        <div class="col-sm-4">
                            <div class="form-check">
                                <input class="form-check-input filter" type="checkbox" data-name="${name}" value="${encodeURIComponent(value)}" id="${value}" ${count > 0 ? '' : 'disabled'}>
                                <label class="form-check-label" for="flexCheckDefault">
                                    (${count}) ${value} 
                                </label>
                            </div>
                        </div>
                    `).join('')}
                </div>
            </li>
        `;
    }

    /**
     * Checks the filters and updates their checked status based on the selected filters.
     * @param {Array} filters - The selected filters.
     */
    checkFilters(filters) {
        console.log(filters);
        const filterCheckboxes = document.querySelectorAll('.form-check-input.filter');

        filterCheckboxes.forEach(checkbox => {
            const filterName = checkbox.getAttribute('data-name').trim();
            const filterValue = decodeURIComponent(checkbox.value).trim();

            const filterString = filterValue.includes(',')
                ? `${filterName},'${filterValue}'`  // Wrap in single quotes if the value contains a comma
                : `${filterName},${filterValue}`;    // Otherwise, leave it as is

            console.log(filterString);

            // Check the checkbox if it matches any of the filters
            checkbox.checked = filters.includes(filterString);
        });
    }

    /**
     * Sets the price boundaries for the filter inputs.
     * @param {number} minPrice - The minimum price value.
     * @param {number} maxPrice - The maximum price value.
     */
    setPriceBoundaries(minPrice, maxPrice) {
        const $lowerPrice = $("#lowerPrice");
        const $upperPrice = $("#upperPrice");

        if (minPrice && maxPrice) {

            $lowerPrice.val(minPrice);
            $upperPrice.val(maxPrice);

            $("#lower").attr("min", minPrice).attr("max", maxPrice).val(minPrice);
            $("#upper").attr("min", minPrice).attr("max", maxPrice).val(maxPrice);
        }
    }

    /**
     * Renders the pagination controls for the products.
     * @param {Object} productsPage - The page of products to render pagination for.
     */
    renderPagination(productsPage) {
        const currentPage = Number(productsPage.page) + 1;
        const totalPages = productsPage.totalPages;
        const $pagination = $("#pagination");

        let html = ``;

        if (totalPages > 1) {
            html += this.generatePageNavItem("first", currentPage === 1, '&laquo;',);
            html += this.generatePageNumbers(currentPage, totalPages);
            html += this.generatePageNavItem("last", currentPage === totalPages, '&raquo;');
        }

        $pagination.html(html);
    }

    generatePageNavItem(id, isDisabled, symbol) {
        return `
            <li class="page-item ${isDisabled ? 'disabled' : ''}">
                <a class="page-link" href="#"  id="${id}">
                    <span aria-hidden="true">${symbol}</span>
                </a>
            </li>
        `;
    }

    generatePageNumbers(currentPage, totalPages) {
        let html = ``;

        if (totalPages <= 5) {
            for (let i = 1; i <= totalPages; i++) {
                html += this.generatePageItem(i, currentPage === i);
            }
        } else {
            html += this.generateDynamicPageItems(currentPage, totalPages);
        }

        return html;
    }

    generatePageItem(page, isActive) {
        return `<li class="page-item ${isActive ? 'active' : ''}"><a class="page-link page" href="#">${page}</a></li>`;
    }

    generateDynamicPageItems(currentPage, totalPages) {
        let html = ``;

        if (currentPage <= 3) {
            html += this.generateInitialPageItems(4, currentPage);
            html += this.generateEllipsisAndPageInput();
            html += this.generatePageItem(totalPages, false);
        } else if (currentPage >= totalPages - 2) {
            html += this.generatePageItem(1, false);
            html += this.generateEllipsisAndPageInput();
            html += this.generateFinalPageItems(totalPages, currentPage);
        } else {
            html += this.generatePageItem(1, false);
            if (currentPage - 1 > 2) {
                html += this.generateEllipsisAndPageInput();
            }
            html += this.generateMiddlePageItems(currentPage);
            if (currentPage + 1 < totalPages - 1) {
                html += this.generateEllipsisAndPageInput();
            }
            html += this.generatePageItem(totalPages, false);
        }

        return html;
    }

    generateInitialPageItems(upToPage, currentPage) {
        let html = ``;
        for (let i = 1; i <= upToPage; i++) {
            html += this.generatePageItem(i, currentPage === i);
        }
        return html;
    }

    generateFinalPageItems(totalPages, currentPage) {
        let html = ``;
        for (let i = totalPages - 3; i <= totalPages; i++) {
            html += this.generatePageItem(i, currentPage === i);
        }
        return html;
    }

    generateMiddlePageItems(currentPage) {
        let html = ``;
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
            html += this.generatePageItem(i, currentPage === i);
        }
        return html;
    }

    generateEllipsisAndPageInput() {
        return `
            <li class="page-item"><a class="page-link">
                <span class="select-page">...</span>
                <input type="number" class="form-control page-input"/>
            </a></li>
        `;
    }

    /**
     * Renders the active filters as badges.
     * @param {Object} groupedFilters - The grouped filters as an object with names as keys and arrays of values.
     */
    renderActiveFilters(groupedFilters) {
        console.log("activeFilters", groupedFilters);

        // Check if groupedFilters is empty
        if (Object.keys(groupedFilters).length === 0) {
            console.log("No active filters");
            $("#activeFilters").addClass("d-none"); // Hide active filters section if no filters are active
            return;
        }

        $("#activeFilters").removeClass("d-none");

        const $badges = $("#filterBadges");
        $badges.empty();

        for (const [name, values] of Object.entries(groupedFilters)) {
            // Remove single quotes around values
            const filteredValues = values.map(value => value.replace(/^'(.*)'$/, '$1'));

            // Only append if there are non-empty values
            if (filteredValues.length > 0) {
                $badges.append(this._getActiveFilterHtml(name, filteredValues));
            }
        }
    }



    /**
     * Generates the HTML for a group of active filters.
     * @param {string} name - The name of the filter.
     * @param {Array} values - The values of the filter.
     * @returns {string} - The HTML string for the group of active filters.
     */
    _getActiveFilterHtml(name, values) {
        return `
            <div class="d-flex gap-1 align-items-center">
                <div class="fs-7 pe-3">${name}:</div>
                ${values.map(value => this._getActiveFilterBadgeHtml(value)).join('')}
            </div>
        `;
    }

    /**
     * Generates the HTML for a single active filter badge.
     * @param {string} value - The value of the filter.
     * @returns {string} - The HTML string for the filter badge.
     */
    _getActiveFilterBadgeHtml(value) {
        return `
            <span class="badge d-flex p-2 align-items-center text-bg-secondary rounded-pill">
                <span class="px-1">${value}</span>
                <a href="#" class="text-white ms-2 remove-filter" data-filter-value="${value}"><i class="bi bi-x-lg"></i></a>
            </span>
        `;
    }

    removeActiveFilter(target) {
        const $target = $(target);


        if ($target.closest("div").children().length === 2) {
            $target.closest("div").remove();
        } else {
            $target.closest("span").remove();
        }

        const checkboxValue = $target.data("filter-value");

        // fetch one of the checkboxes
        const checkbox = document.querySelector(`#filters input[value="${encodeURIComponent(checkboxValue)}"]`);

        // Uncheck the checkbox if found and trigger a change event
        if (checkbox) {
            checkbox.checked = false;
            $(checkbox).trigger('change');
        }

        // check if hide active filter container is required
        if ($("#activeFilters div").children().length === 2) {
            $("#activeFilters").addClass("d-none")
        }
    }


}
