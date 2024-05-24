class ShoppingCartView {
    constructor() {}

    updateNavigationQuantity(itemCount) {
        $("#quantity-count").text(itemCount);
    }

    renderProducts(products, localProducts) {
        const tableBody = $("tbody");
        tableBody.empty(); // Clear existing rows

        const localCartMap = new Map(localProducts.map(item => [item.productId, item]));

        products.forEach(product => {
            const localProduct = localCartMap.get(product.id);
            const html = this._generateProductRowHtml(product, localProduct);
            tableBody.append(html);
        });
    }

    _generateProductRowHtml(product, localProduct) {
        const productDetailsHtml = product.details.slice(0, 3).map(detail => `
            <p class="text-muted mb-0">${detail.name}: ${detail.value}</p>
        `).join('');

        const totalPrice = localProduct.quantity * product.discountPrice;

        return `
            <tr>
                <td>
                    <div class="d-flex gap-3 align-items-center">
                        <img src="${product.mainImagePath}" class="img-fluid product-image lazy">
                        <div class="text-truncate" style="max-width: 150px;">
                            <a href="#" class="link-dark text-decoration-none text-truncate">${product.name}</a>
                            ${productDetailsHtml}
                        </div>
                    </div>
                </td>
                <td class="align-middle">${formatPriceUtil.formatPrice(product.discountPrice)}</td>
                <td class="align-middle">
                    <div class="input-group mb-3 quantity" style="max-width: 120px;">
                        <button class="btn border-dark-subtle border-end-0 quantity-minus" type="button">
                            <i class="bi bi-dash opacity-25"></i>
                        </button>
                        <input type="text" class="form-control text-center border-dark-subtle border-start-0 border-end-0 border-1 quantity-input" value="${localProduct.quantity}">
                        <button class="btn border-dark-subtle border-1 border-start-0 quantity-plus" type="button">
                            <i class="bi bi-plus"></i>
                        </button>
                    </div>
                </td>
                <td class="align-middle">
                    ${formatPriceUtil.formatPrice(totalPrice)}
                </td>
                <td class="align-middle">
                    <a href="#" class="link-dark"><i class="bi bi-x-lg"></i></a>
                </td>
            </tr>
        `;
    }
}
