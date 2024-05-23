// add to cart - require #products container
// add to cart with quantity - require element with .add-to-cart-quantity to listen
class ShoppingCartView {
    constructor() {
    }

    updateNavigationQuantity(itemCount) {
        $("#quantity-count").text(itemCount);
    }

}