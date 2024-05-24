class ShoppingCartController {

    constructor(model, view) {
        this.model = model;
        this.view = view;

        this.synchronizeLocalAndRemoteState();
    }

    initialize() {
        if (this.model.data.length !== 0) {
            this.model.initializeProducts()
                .then(() => {
                    this.view.renderProducts(this.model.products, this.model.data);
                });
        } else {
            // hide shopping cart
        }
    }

    synchronizeLocalAndRemoteState() {
        if (window.location.href.endsWith("?continue")) {
            this.model.merge()
                .then()
                .catch(error => {
                    console.error(error);
                    this.updateNavigationQuantity();

                    console.debug("Successfully merged products in cart")
                    showErrorModal(error.response);
                });
        }
    }

    initializeAddToCartWithQuantityListener() {
        $(".container").on('click', '.add-to-cart-quantity', event => {
            const $target = $(event.currentTarget);
            const productId = Number($target.data("product-id"));
            const quantity = Number($target.parent().find(".quantity-input").val());

            this.model.addItem(productId, quantity);
            this.updateNavigationQuantity();
        });
    }

    // target must have product-id as dataset attribute
    initializeAddToCartListener() {
        $("#products").on('click', '.add-to-cart', event => {
            const productId = Number($(event.currentTarget).data("product-id"));
            this.model.addItem(productId);
            this.updateNavigationQuantity();
        });
    }

    updateNavigationQuantity() {
        this.view.updateNavigationQuantity(this.model.data.length);
    }

}
