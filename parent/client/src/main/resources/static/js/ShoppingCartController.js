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

                    this.initializeRemoveCartItemListener();
                    this.initializeClearCartListener();
                    this.initializeQuantityDecreaseAndIncreaseListeners();
                });
        } else {
            // hide shopping cart
        }
    }

    synchronizeLocalAndRemoteState() {
        if (window.location.href.endsWith("?continue")) {
            this.model.merge()
                .then(() => console.debug("Local and remote state successfully synchronized"))
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
            event.preventDefault();
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
            event.preventDefault();
            const productId = Number($(event.currentTarget).data("product-id"));
            this.model.addItem(productId);
            this.updateNavigationQuantity();
        });
    }

    // require on element product-id!
    initializeRemoveCartItemListener() {
        $(".container").on('click', '.delete-cart-item', event => {
            const productId = Number($(event.currentTarget).data("product-id"));

            this.model.removeItem(productId);

            this.view.removeItem(productId);
            this.view.updateNavigationQuantity(this.model.data.length);
        })

    }

    initializeClearCartListener() {
        $("#clear-cart").on("click",event => {
            event.preventDefault();

            this.model.clear()
                .then(() => {
                    this.view.updateNavigationQuantity(this.model.data.length);
                    this.view.clear();

                    console.debug("Successfully removed all items from cart")
                })
                .catch(error => {
                    console.error(error);
                    showErrorModal(error.response)
                });
        })
    }

    // require product id as data attr
    initializeQuantityDecreaseAndIncreaseListeners() {
        $(".container").on("click", ".quantity-plus, .quantity-minus", event => {
           event.preventDefault();
            const $target = $(event.currentTarget);

            const productId = Number($target.data("product-id"));
            if($target.hasClass("quantity-minus")) {
                this.model.decreaseQuantity(productId);
            } else {
                this.model.increaseQuantity(productId);
            }

        });
    }

    updateNavigationQuantity() {
        this.view.updateNavigationQuantity(this.model.data.length);
    }


}
