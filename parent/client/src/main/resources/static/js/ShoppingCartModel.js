class ShoppingCartModel {

    maxQuantityCartItem = 5;
    maxProductsCart = 25;
    cartBaseUrl = MODULE_URL + "cart/";
    productBaseUrl = MODULE_URL + "products/";

    constructor() {
        this.data = JSON.parse(localStorage.getItem('cart')) || [];
    }

    async initializeProducts() {
        try {
            console.debug(`ShoppingCartModel.initializeProducts()`)
            console.debug(`productBaseUrl : ${this.productBaseUrl}`)
            console.debug(`data : ${this.data}`)

            if (this.data.length > 0) {
                const productIds = this.data.map(product => product.productId);
                this.products = await ajaxUtil.post(`${this.productBaseUrl}`, productIds);
                console.debug(`ShoppingCartModel.initializeProducts() : Product initialized`)
            }
        } catch (error) {
            console.error('error occurred while fetching products from db', error);
            throw error;
        }
    }

    // target must have product-id as dataset attribute
    addItem(productId) {
        console.debug("[ShoppingCartModel.addItem()]");
        console.debug("[LOGGED IN]: ", LOGGED_IN);
        console.debug("[PRODUCTS]: ", this.products);
        console.debug("[START DATA]: ", this.data);
        console.debug("[productId]: ", productId);

        let productIndex = this.data.findIndex(item => item.productId === productId);

        if (productIndex !== -1) {

            // If item already exists, increase its quantity
            if (this.data[productIndex].quantity === this.maxQuantityCartItem) {

                console.info(`The quantity of one product[${productId}] cannot exceed ${this.maxQuantityCartItem}`);
                return;
            }

            this.data[productIndex].quantity++;
        } else {

            // If item doesn't exist, add it to the cart with quantity 1
            if (this.data.length >= this.maxProductsCart) {
                console.info(`Max ${this.maxProductsCart} products in cart`);
                return;
            }

            this.data.push({productId: productId, quantity: 1});
            productIndex = this.data.length - 1;
        }

        if (LOGGED_IN) {
            this._updateDBCart(this.data[productIndex].productId, this.data[productIndex].quantity)
                .then(() => {
                    console.debug('[ShoppingCartModel.addItem] : DB cart updated successfully');
                })
                .catch(error => {
                    console.error('[ShoppingCartModel.addItem] : Error updating database:', error);
                    showErrorModal(error.response);
                });
        }

        console.debug("[END DATA]: ", this.data);
        this.updateLocalStorage();
    }

    async _updateDBCart(productId, quantity) {
        try {
            const url = `${this.cartBaseUrl}add/${productId}/${quantity}`;
            await ajaxUtil.post(url, {});
            console.debug(`[ShoppingCartModel._updateDBCart] : Product id[${productId}] quantity[${quantity}] updated in DB cart successfully`);
        } catch (error) {
            console.error(`[ShoppingCartModel._updateDBCart()] : Error adding product id[${productId}] quantity[${quantity}] to cart:`, error);
            throw error;
        }
    }

    updateLocalStorage() {
        localStorage.setItem('cart', JSON.stringify(this.data));
    }
}