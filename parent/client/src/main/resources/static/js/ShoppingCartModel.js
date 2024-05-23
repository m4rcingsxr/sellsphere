class ShoppingCartModel {

    MAX_QUANTITY_CART_ITEM = 5;
    MAX_PRODUCTS_IN_CART = 25;
    BASE_URL = MODULE_URL + "cart";

    constructor() {
        this.data = JSON.parse(localStorage.getItem('cart')) || [];
    }

    async initializeProducts() {
        try {
          if(this.data.length > 0) {
              const productIds = this.data.map(product => product.productId);
          }
        } catch (error) {
            throw error;
        }
    }
}