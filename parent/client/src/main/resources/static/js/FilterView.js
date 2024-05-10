"use strict";

class FilterView {

    renderProducts(productsPage) {
        const $products = $("#products").empty();
        productsPage.content.forEach(product => $products.append(this.generateProductHtml(product)));
    }

    generateProductHtml(product) {
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

    toggleFilters() {
        $("#products, #allFilters, .viewProducts, #showAllFilters, #filters, #allFilterNames").toggleClass("d-none");
    }

    renderAllFilterNames(countMap) {
        const allNamesHtml = Object.keys(countMap).map(name => `
            <a href="#allFilterNames${name}" class="list-group-item list-group-item-action bg-body-tertiary list-group-item-secondary filter-name">${name}</a>
        `).join('');

        $("#allFilterNames").empty().append(allNamesHtml);
    }

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

    generateProductFilterHtml(name, values, filters) {
        const filterSet = new Set(filters);

        return `
            <div>
                <span class="fw-bolder">${name}</span>
                <div class="d-flex flex-column gap-1 mt-2">
                    ${Object.entries(values).map(([value, count]) => `
                        <div class="form-check">
                            <input class="form-check-input filter" type="checkbox" data-name="${name}" value="${encodeURIComponent(value)}" id="${value}" ${count > 0 ? '' : 'disabled'} ${filterSet.has(`${name},${value}`) ? 'checked' : ''}>
                            <label class="form-check-label" for="${value}">(${count}) ${value}</label>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    renderAllFilters(countMap) {
        const filtersHtml = Object.entries(countMap).map(([name, values]) => this.generateListGroupItemHtmlForAllFilters(name, values)).join('');
        $('#allFilters').html(filtersHtml);
    }

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

    checkFilters(filters) {
        const filterCheckboxes = document.querySelectorAll('.form-check-input.filter');

        filterCheckboxes.forEach(checkbox => {
            const filterName = checkbox.getAttribute('data-name').trim();
            const filterValue = decodeURIComponent(checkbox.value).trim();

            // Construct the filter string in the same format used in the filters array
            const filterString = `${filterName},${filterValue}`;

            // Check the checkbox if it matches any of the filters
            checkbox.checked = filters.includes(filterString);
        });
    }

    setPriceBoundaries(minPrice, maxPrice) {
        const $lowerPrice = $("#lowerPrice");
        const $upperPrice = $("#upperPrice");

        if(minPrice && maxPrice && !$upperPrice.val() && !$lowerPrice.val()) {

            $lowerPrice.val(minPrice);
            $upperPrice.val(maxPrice);

            $("#lower").attr("min", minPrice).attr("max", maxPrice).val(minPrice);
            $("#upper").attr("min", minPrice).attr("max", maxPrice).val(maxPrice);
        }
    }


    renderPagination(productsPage) {
        const currentPage = Number(productsPage.page) + 1;
        const totalPages = productsPage.totalPages;
        const $pagination = $("#pagination");

        let html = ``;

        if (totalPages > 1) {
            html += this.generatePageNavItem('Previous', currentPage === 1, '&laquo;');
            html += this.generatePageNumbers(currentPage, totalPages);
            html += this.generatePageNavItem('Next', currentPage === totalPages, '&raquo;');
        }

        $pagination.html(html);
    }

    generatePageNavItem(label, isDisabled, symbol) {
        return `
            <li class="page-item ${isDisabled ? 'disabled' : ''}">
                <a class="page-link page" href="#" aria-label="${label}">
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


}
