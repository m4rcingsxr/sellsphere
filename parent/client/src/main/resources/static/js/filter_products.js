"use strict";

/**
 * This script manages the filtering and display of products on a webpage.
 *
 * Required DOM elements:
 * - #filters: Container for filter options.
 *     <div>
 *         <span class="fw-bolder">Filter Name</span>
 *         <div class="d-flex flex-column gap-1 mt-2">
 *             <div class="form-check">
 *                 <input class="form-check-input filter" type="checkbox" value="Filter Value" id="Filter Value">
 *                 <label class="form-check-label" for="Filter Value">(Count) Filter Value</label>
 *             </div>
 *             <!-- More filter options -->
 *         </div>
 *     </div>
 * - #products: Container where products will be rendered.
 * - showFullScreenSpinner(): Function to show a loading spinner overlay.
 * - hideFullScreenSpinner(): Function to hide the loading spinner overlay.
 * - showErrorModal(error): Function to show an error modal with the given error response.
 * */
$(function () {
    init();
});


function init() {
    // Fetch filter counts initially without any filters

    // Extract and apply filters from URL if they exist
    const filters = extractFiltersFromUrl(window.location.href);
    fetchAndHandleFilterCounts(null)
        .then(() => {
            return handleFilterChange(filters);
        })
        .catch(error => showErrorModal(error.response))
        .finally(hideFullScreenSpinner);

    // Initialize event listeners
    initListeners();
}

/**
 * Extracts filter parameters from the given URL.
 * @param {string} url - The URL to extract filters from.
 * @returns {Array<string>} - An array of filter values.
 */
function extractFiltersFromUrl(url) {
    const urlParams = new URLSearchParams(new URL(url).search);
    return [...urlParams.entries()].filter(([key]) => key === 'filter').map(([, value]) => value);
}

/**
 * Initializes event listeners for filter changes.
 */
function initListeners() {
    $("#filters, #allFilters").on("click", ".filter", event => {
        showFullScreenSpinner();
        synchronizeSingleFilterState(event.target);

        try {
            const filters = gatherProductSelectedFilters(event);
            handleFilterChange(filters)
                .catch(error => showErrorModal(error.response))
                .finally(hideFullScreenSpinner);
        } catch (error) {
            console.error(error.message);
            hideFullScreenSpinner();
        }
    });

    $("#showAllFilters").on("click", event => {
        $("#products").toggleClass("d-none");
        $("#allFilters").toggleClass("d-none");
        $(".viewProducts").toggleClass("d-none");
        $("#showAllFilters").toggleClass("d-none");
    })

    $(".viewProducts").on("click", event => {
        $("#products").toggleClass("d-none");
        $("#allFilters").toggleClass("d-none");
        $(".viewProducts").toggleClass("d-none");
        $("#showAllFilters").toggleClass("d-none");
    })
}

/**
 * Synchronizes the state of a single checkbox between #filters and #allFilters.
 * @param {Element} target - The checkbox element that triggered the event.
 */
function synchronizeSingleFilterState(target) {
    const value = target.value;
    const isChecked = target.checked;
    const containerToSync = target.closest('#filters') ? '#allFilters' : '#filters';

    const matchingCheckbox = document.querySelector(`${containerToSync} .form-check-input[value="${value}"]`);
    if (matchingCheckbox) {
        matchingCheckbox.checked = isChecked;
    }
}


/**
 * Handles filter changes by fetching products and updating the filter display.
 * @param {Array<string>} filters - The filters to apply.
 * @throws Will throw an error if fetching products or updating filters fails.
 */
async function handleFilterChange(filters) {
    try {
        await fetchAndHandleProducts(filters, 0);
        await updateFilterDisplay(filters);
        console.info("Successfully handled filter change");
    } catch (error) {
        throw error;
    }
}

/**
 * Updates the display of filters based on the applied filters.
 * @param {Array<string>} filters - The filters to apply.
 * @throws Will throw an error if fetching filter counts fails.
 */
async function updateFilterDisplay(filters) {
    try {
        const countMap = await fetchFilterCounts(filters);

        updateCheckboxesDisplay('#filters', filters, countMap);
        updateCheckboxesDisplay('#allFilters', filters, countMap);

    } catch (error) {
        throw error;
    }
}

/**
 * Updates the display of checkboxes based on the filter counts.
 * @param {string} containerSelector - The CSS selector for the container.
 * @param {Array<string>} filters - The filters to apply.
 * @param {Object} countMap - The map of filter counts.
 */
function updateCheckboxesDisplay(containerSelector, filters, countMap) {
    const filterCheckboxes = document.querySelectorAll(`${containerSelector} .form-check-input`);

    filterCheckboxes.forEach(checkbox => {
        const filterName = checkbox.closest(containerSelector === '#filters' ? '.d-flex.flex-column.gap-1.mt-2' : '.row.g-3.mt-1').previousElementSibling.textContent.trim();
        const filterValue = decodeURIComponent(checkbox.value).trim();

        if (countMap[filterName]?.[filterValue] !== undefined) {
            checkbox.disabled = false;
            checkbox.nextElementSibling.innerHTML = `(${countMap[filterName][filterValue]}) ${filterValue}`;
        } else {
            checkbox.disabled = true;
            checkbox.nextElementSibling.innerHTML = `(0) ${filterValue}`;
        }

        checkbox.checked = filters.includes(`${filterName},${filterValue}`);
    });
}

/**
 * Fetches and handles the display of products based on the applied filters.
 * @param {Array<string>} filters - The filters to apply.
 * @param {number} pageNum - The page number for pagination.
 * @throws Will throw an error if fetching products fails.
 */
async function fetchAndHandleProducts(filters, pageNum) {
    try {
        const formattedFilters = formatFiltersToIncludeCommaValues(filters);
        const productsPage = await fetchProductsPage(formattedFilters, pageNum);
        renderProducts(productsPage);
    } catch (error) {
        throw error;
    }
}

function formatFiltersToIncludeCommaValues(filters) {
    if (filters != null) {
        return filters.map(filter => {
            const [name, value] = filter.split(/,(.+)/); // Split only on the first comma
            const formattedValue = value.includes(',') ? `'${value}'` : value;
            return `${name},${formattedValue}`;
        });
    } else {
        return filters;
    }
}

/**
 * Renders the products on the page.
 * @param {Object} productsPage - The page of products to render.
 */
function renderProducts(productsPage) {
    const $products = $("#products").empty();
    productsPage.content.forEach(product => $products.append(generateProductHtml(product)));
}


/**
 * Generates the HTML for a single product.
 * @param {Object} product - The product data.
 * @returns {string} - The HTML string for the product.
 */
function generateProductHtml(product) {
    const formattedDiscountPrice = formatPriceUtil.formatPrice(product.discountPrice);
    const formattedPrice = formatPriceUtil.formatPrice(product.price);
    const productDetails = Array.isArray(product.details) ? product.details.slice(0, 3) : [];

    return `
        <div class="col-sm-4">
            <div class="product-carousel-card p-2 overflow-visible rounded-2 position-relative">
                <a href="${MODULE_URL}p/${product.alias}" class="product-carousel-img-container mt-4 ">
                    <img src="${product.mainImage}" class="card-img-top" alt="${product.name}">
                </a>
                <div class="mt-4 p-1">
                    <a href="/p/${product.alias}" class="link-dark link-underline link-underline-opacity-0 fs-7 product-title">
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
                <div class="product-carousel-card-details">
                    <div class="row g-2">${productDetails.map(detail => `
                        <div class="col-sm-8 detail"><span class="text-light-emphasis">${detail.name}:</span></div>
                        <div class="col-sm-4 detail fw-bolder">${detail.value}</div>
                    `).join('')}</div>
                </div>
            </div>
        </div>
    `;
}

/**
 * Fetches a page of products based on the applied filters and page number.
 * @param {Array<string>} filters - The filters to apply.
 * @param {number} pageNum - The page number for pagination.
 * @returns {Promise<Object>} - The fetched products page.
 */
async function fetchProductsPage(filters, pageNum) {
    const baseUrl = `${MODULE_URL}filter/products`;
    const fullUrl = buildPageRequestUrl(baseUrl, filters, pageNum);
    return await ajaxUtil.get(fullUrl);
}

/**
 * Gathers selected filters from the UI.
 * @param {Event} event - The event that triggered the filter selection.
 * @returns {Array<string>} - An array of selected filters.
 * @throws Will throw an error if multiple values are selected for a single filter.
 */
function gatherProductSelectedFilters(event) {
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

/**
 * Fetches and handles filter counts.
 * @param {Array<string>} filters - The filters to apply.
 */
async function fetchAndHandleFilterCounts(filters) {
    try {
        showFullScreenSpinner();
        const formattedFilters = formatFiltersToIncludeCommaValues(filters);
        const countMap = await fetchFilterCounts(formattedFilters)
        renderProductFilters(countMap);
        renderAllFilters(countMap);
    } catch (error) {
        throw error;
    } finally {
        hideFullScreenSpinner()
    }

}

/**
 * Renders the filter options based on the fetched counts.
 * @param {Object} countMap - The map of filter counts.
 */
function renderProductFilters(countMap) {
    let filtersHtml = '';
    for (const [name, values] of Object.entries(countMap)) {
        filtersHtml += generateProductFilterHtml(name, values);
    }
    document.getElementById('filters').innerHTML = filtersHtml;
}

/**
 * Generates the HTML for a single filter group.
 * @param {string} name - The name of the filter group.
 * @param {Object} values - The values and counts for the filter group.
 * @returns {string} - The HTML string for the filter group.
 */
function generateProductFilterHtml(name, values) {
    return `
        <div>
            <span class="fw-bolder">${name}</span>
            <div class="d-flex flex-column gap-1 mt-2">
                ${Object.entries(values).map(([value, count]) => `
                    <div class="form-check">
                        <input class="form-check-input filter" type="checkbox" value="${encodeURIComponent(value)}" id="${value}">
                        <label class="form-check-label" for="${value}">(${count}) ${value}</label>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

function renderAllFilters(countMap) {
    let filtersHtml = '';
    for (const [name, values] of Object.entries(countMap)) {
        filtersHtml += generateListGroupItemHtmlForAllFilters(name, values);
    }
    document.getElementById('allFilters').innerHTML = filtersHtml;
}

function generateListGroupItemHtmlForAllFilters(name, values) {
    return `
        <li class="list-group-item p-4">
            <span class="fw-bolder">${name}</span>
            <div class="row g-3 mt-1">
                ${Object.entries(values).map(([value, count]) => `
                    <div class="col-sm-4">
                        <div class="form-check">
                            <input class="form-check-input filter" type="checkbox" value="${encodeURIComponent(value)}" id="${value}">
                            <label class="form-check-label" for="flexCheckDefault">
                                (${count}) ${value} 
                            </label>
                        </div>
                    </div>
                `).join('')}
            </div>
        </li>
    `
}

/**
 * Fetches the counts for the filters based on the applied filters.
 * @param {Array<string>} filters - The filters to apply.
 * @returns {Promise<Object>} - The fetched filter counts.
 */
async function fetchFilterCounts(filters) {
    const baseUrl = `${MODULE_URL}filter/counts`;
    const pageRequestUrl = buildPageRequestUrl(baseUrl, filters);
    return await ajaxUtil.get(pageRequestUrl);
}

/**
 * Builds the URL for fetching a page of products or filter counts.
 * @param {string} baseUrl - The base URL for the request.
 * @param {Array<string>} filters - The filters to apply.
 * @param {number} [pageNum] - The page number for pagination (optional).
 * @returns {string} - The full URL with query parameters.
 * @throws Will throw an error if the URL path is not supported.
 */
function buildPageRequestUrl(baseUrl, filters, pageNum) {
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

