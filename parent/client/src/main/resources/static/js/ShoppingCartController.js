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
        $(".add-to-cart-quantity").on('click', event => {

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