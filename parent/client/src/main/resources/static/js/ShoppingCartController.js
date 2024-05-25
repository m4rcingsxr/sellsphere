/**
 * Controller for managing the shopping cart.
 * Required DOM elements:
 * - Navigation Quantity: #quantity-count
 * - Products Container: #products
 * - Clear Cart Button: #clear-cart
 * - Cart Item Container: .container
 * - Subtotal: #subtotal
 * - Saving: #saving
 * - Tax: #tax
 * - Shipping: #shipping
 * - Total: #total
 */
class ShoppingCartController {

    constructor(model, view) {
        this.model = model;
        this.view = view;

        this.synchronizeLocalAndRemoteState();
    }

    /**
     * Initializes the shopping cart controller.
     * Sets up event listeners and renders products if there are items in the model.
     */
    initialize() {
        if (this.model.data.length !== 0) {
            this.model.initializeProducts()
                .then(() => {
                    this.view.renderProducts(this.model.products, this.model.data);

                    this.initializeEventListeners();
                    this.updateCartView();
                });
        } else {
            // hide shopping cart
        }
    }

    /**
     * Synchronizes local and remote state if the URL indicates first login.
     */
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

    /**
     * Initializes all required event listeners for the shopping cart view.
     */
    initializeEventListeners() {
        this.initializeRemoveCartItemListener();
        this.initializeClearCartListener();
        this.initializeQuantityDecreaseAndIncreaseListeners();
    }

    /**
     * Initializes the add to cart with quantity event listener.
     */
    initializeAddToCartWithQuantityListener() {
        $(".container").on('click', '.add-to-cart-quantity', event => {
            event.preventDefault();
            const $target = $(event.currentTarget);
            const productId = Number($target.data("product-id"));
            const quantity = Number($target.parent().find(".quantity-input").val());

            this.model.addItem(productId, quantity)
                .then(() => this.updateNavigationQuantity())
                .catch(error => {
                    console.error(error);
                    showErrorModal(error.response);
                });
        });
    }

    /**
     * Initializes the add to cart event listener. All elements that trigger this event must hava dataset attr - data-product-id
     */
    initializeAddToCartListener() {
        $("#products").on('click', '.add-to-cart', event => {
            event.preventDefault();
            const productId = Number($(event.currentTarget).data("product-id"));
            this.model.addItem(productId)
                .then(() => this.updateNavigationQuantity())
                .catch(error => {
                    console.error(error);
                    showErrorModal(error.response);
                });
        });
    }

    /**
     * Initializes the remove cart item event listener. All elements that trigger this event must hava dataset attr - data-product-id
     */
    initializeRemoveCartItemListener() {
        $(".container").on('click', '.delete-cart-item', event => {
            const productId = Number($(event.currentTarget).data("product-id"));

            this.model.removeItem(productId);

            this.view.removeItem(productId);
            this.view.updateNavigationQuantity(this.model.data.length);

            this.updateCartView();
        })

    }

    /**
     * Initializes the clear cart event listener.
     */
    initializeClearCartListener() {
        $("#clear-cart").on("click", event => {
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

    /**
     * Initializes the quantity change event listeners. All elements that trigger this event must hava dataset attr - data-product-id
     */
    initializeQuantityDecreaseAndIncreaseListeners() {
        $(".container").on("click", ".quantity-plus, .quantity-minus", event => {
            event.preventDefault();
            const $target = $(event.currentTarget);
            const productId = Number($target.data("product-id"));

            const successHandler = () => {
                this.view.updateProductTotal(productId, this.model.getProductTotal(productId));
                this.updateCartView();
            }

            const errorHandler = (error) => {
                console.error(error);
                showErrorModal(error.response);
            }

            if ($target.hasClass("quantity-minus")) {
                this.model.decreaseQuantity(productId)
                    .then(successHandler)
                    .catch(errorHandler);
            } else {
                this.model.increaseQuantity(productId)
                    .then(successHandler)
                    .catch(errorHandler);
            }
        });
    }

    /**
     * Updates the navigation quantity display.
     */
    updateNavigationQuantity() {
        this.view.updateNavigationQuantity(this.model.data.length);
    }

    /**
     * Updates the cart view with the latest totals.
     */
    updateCartView() {
        this.view.updateSubtotal(this.model.getSubtotal());
        this.view.updateSaving(this.model.getSaving());
        this.view.updateTotal(this.model.getTotal());

        // implement later
        this.view.updateTax(this.model.getTax());
        this.view.updateShipping(this.model.getShipping());
    }
}
