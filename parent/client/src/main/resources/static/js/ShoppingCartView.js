class ShoppingCartView {
    constructor() {
    }

    /**
     * Updates the navigation quantity display.
     * @param {number} itemCount - The number of items in the cart.
     */
    updateNavigationQuantity(itemCount) {
        $("#quantity-count").text(itemCount);
    }


    /**
     * Renders the products in the cart.
     * @param {Array} products - The list of products.
     * @param {Array} localProducts - The local cart data.
     */
    renderProducts(products, localProducts) {
        const tableBody = $("tbody");
        tableBody.empty(); // Clear existing rows

        const localCartMap = new Map(localProducts.map(item => [item.productId, item]));

        products.forEach(product => {
            const localProduct = localCartMap.get(product.id);
            const html = this._generateProductRowHtml(product, localProduct);
            tableBody.append(html);
        });

        setInitialButtonStates();
    }

    /**
     * Generates the HTML for a product row.
     * @param {Object} product - The product data.
     * @param {Object} localProduct - The local cart data for the product.
     * @returns {string} - The HTML for the product row.
     */
    _generateProductRowHtml(product, localProduct) {
        const productDetailsHtml = product.details.slice(0, 3).map(detail => `
            <p class="text-muted mb-0">${detail.name}: ${detail.value}</p>
        `).join('');

        const totalPrice = localProduct.quantity * product.discountPrice;

        return `
            <tr id="product-container${product.id}">
                <td>
                    <div class="d-flex gap-3 align-items-center">
                        <img src="${product.mainImagePath}" class="img-fluid product-image lazy">
                        <div class="text-truncate" style="max-width: 150px;">
                            <a href="#" class="link-dark text-decoration-none text-truncate">${product.name}</a>
                            ${productDetailsHtml}
                        </div>
                    </div>
                </td>
                <td class="align-middle">
                    <span>${formatPriceUtil.formatPrice(product.discountPrice)}</span>
                    ${product.discountPercent > 0 ? `<span class="fw-lighter fs-7 text-decoration-line-through d-block">${formatPriceUtil.formatPrice(product.price)}</span>` : ''}
                </td>
                <td class="align-middle">
                    <div class="input-group mb-3 quantity" style="max-width: 120px;">
                        <button class="btn border-dark-subtle border-end-0 quantity-minus" type="button" data-product-id="${product.id}">
                            <i class="bi bi-dash"></i>
                        </button>
                        <input type="text" class="form-control text-center border-dark-subtle border-start-0 border-end-0 border-1 quantity-input" value="${localProduct.quantity}">
                        <button class="btn border-dark-subtle border-1 border-start-0 quantity-plus" type="button" data-product-id="${product.id}">
                            <i class="bi bi-plus"></i>
                        </button>
                    </div>
                </td>
                <td class="align-middle">
                    <span id="product-total${product.id}">
                        ${formatPriceUtil.formatPrice(totalPrice)}
                    </span>
                </td>
                <td class="align-middle">
                    <a href="#" class="link-dark delete-cart-item" data-product-id="${product.id}"><i class="bi bi-x-lg"></i></a>
                </td>
            </tr>
        `;
    }

    /**
     * Removes an item from the view.
     * @param {number} productId - The ID of the product to remove.
     */
    removeItem(productId) {
        $(`#product-container${productId}`).remove();
    }

    /**
     * Clears the cart view.
     */
    clear() {
        this.updateSubtotal(0);
        this.updateSaving(0);
        this.updateTotal(0);

        this.updateTax(0);
        this.updateShipping(0);

        $("tbody").empty();
    }

    /**
     * Updates the total price for a specific product.
     * @param {number} productId - The ID of the product.
     * @param {number} total - The total price.
     */
    updateProductTotal(productId, total) {
        $(`#product-total${productId}`).text(formatPriceUtil.formatPrice(total));
    }

    /**
     * Updates the subtotal display.
     * @param {number} subtotal - The subtotal amount.
     */
    updateSubtotal(subtotal) {
        $("#subtotal").text(formatPriceUtil.formatPrice(subtotal));
    }

    /**
     * Updates the savings display.
     * @param {number} saving - The savings amount.
     */
    updateSaving(saving) {
        $("#saving").text(formatPriceUtil.formatPrice(saving));
    }

    /**
     * Updates the tax display.
     * @param {number} tax - The tax amount.
     */
    updateTax(tax) {
        $("#tax").text(formatPriceUtil.formatPrice(tax));
    }

    /**
     * Updates the shipping display.
     * @param {number} shipping - The shipping amount.
     */
    updateShipping(shipping) {
        $("#shipping").text(formatPriceUtil.formatPrice(shipping));
    }

    /**
     * Updates the total display.
     * @param {number} total - The total amount.
     */
    updateTotal(total) {
        $("#total").text(formatPriceUtil.formatPrice(total));
    }
}
