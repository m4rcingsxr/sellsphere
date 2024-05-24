class ShoppingCartController {

    constructor(model, view) {
        this.model = model;
        this.view = view;

        this.synchronizeLocalAndRemoteState();
    }

    synchronizeLocalAndRemoteState() {
        if(LOGGED_IN) {

        } else {

        }
    }

    initializeAddToCartWithQuantityListener() {
        $(".container").on('click', '.add-to-cart-quantity', event => {
            const $target = $(event.currentTarget);
            const productId = Number($target.data("product-id"));
            const quantity = Number($target.parent().find(".quantity-input").val());

            this.model.addItem(productId, quantity);
            this.view.updateNavigationQuantity(this.model.data.length);
        });
    }

    // target must have product-id as dataset attribute
    initializeAddToCartListener() {
        $("#products").on('click', '.add-to-cart', event => {
            const productId = Number($(event.currentTarget).data("product-id"));
            this.model.addItem(productId);
            this.view.updateNavigationQuantity(this.model.data.length);
        });
    }
}