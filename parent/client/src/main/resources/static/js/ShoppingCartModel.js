class ShoppingCartModel {

    maxQuantityCartItem = 5;
    maxProductsCart = 25;
    cartBaseUrl = MODULE_URL + "cart";
    productBaseUrl = MODULE_URL + "products";

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
    addItem(productId, quantityChange = 1) {
        console.debug("[ShoppingCartModel.addItem()]");
        console.debug("[LOGGED IN]: ", LOGGED_IN);
        console.debug("[PRODUCTS]: ", this.products);
        console.debug("[START DATA]: ", this.data);
        console.debug("[productId]: ", productId);
        console.debug("[quantityChange]: ", quantityChange);

        // Ensure the quantityChange is either -1 or a positive integer from 1-5
        if ((quantityChange < 1 || quantityChange > 5) && quantityChange !== -1) {
            console.error('Quantity change must be a positive integer between 1 and 5, or -1.');
            return;
        }

        let productIndex = this.data.findIndex(item => item.productId === productId);

        if (productIndex !== -1) {
            // If item already exists, adjust its quantity
            let newQuantity = this.data[productIndex].quantity + quantityChange;

            if (quantityChange === -1) {
                newQuantity = this.data[productIndex].quantity - 1;
            }

            if (newQuantity > this.maxQuantityCartItem || newQuantity < 1) {
                console.info(`The quantity of one product[${productId}] cannot exceed ${this.maxQuantityCartItem} or be less than 1`);
                return;
            }

            this.data[productIndex].quantity = newQuantity;

            if (LOGGED_IN) {
                this.addCartItemToDb(this.data[productIndex].productId, this.data[productIndex].quantity)
                    .then(() => {
                        console.debug('[ShoppingCartModel.addItem] : DB cart updated successfully');
                    })
                    .catch(error => {
                        console.error('[ShoppingCartModel.addItem] : Error updating database:', error);
                        showErrorModal(error.response);
                    });
            }
        } else {
            // If item doesn't exist, add it to the cart with the specified quantity
            if (this.data.length >= this.maxProductsCart) {
                console.info(`Max ${this.maxProductsCart} products in cart`);
                return;
            }

            const initialQuantity = quantityChange === -1 ? 1 : quantityChange;
            this.data.push({ productId: productId, quantity: initialQuantity });
            productIndex = this.data.length - 1;

            if (LOGGED_IN) {
                this.addCartItemToDb(this.data[productIndex].productId, this.data[productIndex].quantity)
                    .then(() => {
                        console.debug('[ShoppingCartModel.addItem] : DB cart updated successfully');
                    })
                    .catch(error => {
                        console.error('[ShoppingCartModel.addItem] : Error updating database:', error);
                        showErrorModal(error.response);
                    });
            }
        }

        this.updateLocalStorage();

        console.debug("[END DATA]: ", this.data);
    }

    async addCartItemToDb(productId, quantity) {
        try {
            const url = `${this.cartBaseUrl}/add/${productId}/${quantity}`;
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

    async merge() {
        try {
            // Fetch the current state of the cart from the database
            const dbCart = await ajaxUtil.get(`${this.cartBaseUrl}/items`);

            // Create a map for the local storage cart items for easy access
            const localCartMap = new Map(this.data.map(item => [item.productId, item]));

            // List to hold the merged cart items
            const mergedCart = [];

            // Iterate through the DB cart and update quantities in the local storage cart
            for (let dbCartItem of dbCart) {
                const localCartItem = localCartMap.get(dbCartItem.productId);

                if (localCartItem) {
                    // Replace the quantity in the local cart with the one from the db cart
                    localCartItem.quantity = dbCartItem.quantity;
                    mergedCart.push(localCartItem);
                    localCartMap.delete(dbCartItem.productId); // Remove from the map once merged
                } else {
                    // Add the DB cart item to the merged cart
                    mergedCart.push(dbCartItem);
                }
            }

            // Add remaining items from local storage that were not in the DB cart
            for (let localCartItem of localCartMap.values()) {
                if (mergedCart.length >= this.maxProductsCart) {
                    console.info(`Max ${this.maxProductsCart} products in cart reached`);
                    break;
                }
                mergedCart.push(localCartItem);
            }

            // Update the local storage and DB cart with the merged data
            this.data = mergedCart;
            this.updateLocalStorage();
            await this.setCart(this.data);

            console.debug(`[ShoppingCartModel.merge] : Cart quantities successfully merged with DB cart`);

        } catch (error) {
            console.error(`[ShoppingCartModel.merge] : Error merging cart items:`, error);
            throw error;
        }
    }

    async setCart(cart) {
        try {
            const url = `${this.cartBaseUrl}/set`;

            await ajaxUtil.post(url, this.data);

            console.debug(`[ShoppingCartModel._setCart] : Cart [${cart}] set as DB cart successfully`);
        } catch (error) {
            console.error(`[ShoppingCartModel._setCart] : Error setting cart [${cart}]:`, error);
        }
    }

    removeItem(productId) {
        const index = this.data.findIndex(item => Number(item.productId) === Number(productId));

        if(index !== -1) {
            this.data.splice(index, 1);
            this.updateLocalStorage();
        }

        if (LOGGED_IN) {
            this._removeProductFromRemote(productId)
                .then(() => {
                    console.debug("Successfully removed product id: " + productId);
                })
                .catch(error => {
                    console.error('Error removed cart item from database:', error);
                    showErrorModal(error.response);
                })
        }
    }

    async _removeProductFromRemote(productId) {
        try {
            await ajaxUtil.delete(`${this.cartBaseUrl}/delete/${productId}`);
            console.debug(`[ShoppingCartModel._removeProductDBCart] : Product id[${productId}] removed from DB cart successfully`);
        } catch (error) {
            console.error(`[ShoppingCartModel._removeProductDBCart] : Error deleting product id[${productId}] from cart:`, error);
            throw error;
        }
    }

    async clear() {
        if(this.data.length > 0) {
            const tempData = this.data;
            this.data = [];
            this.updateLocalStorage();

            if (LOGGED_IN) {
                try {
                    await this._clearDBCart();
                } catch(error) {
                    throw new Error();
                } finally {
                    // not done
                    this.data = tempData;
                }
            }
        }
    }

    async _clearDBCart() {
        try {
            await ajaxUtil.post(`${this.cartBaseUrl}/clear`);

            console.debug('[ShoppingCartModel._clearDBCart] : Database cart cleared successfully');
        } catch (error) {
            console.error('[ShoppingCartModel._clearDBCart] : Error updating database:', error);
            throw error;
        }
    }

    increaseQuantity(productId) {
        this.addItem(productId, 1);
    }


    decreaseQuantity(productId) {
        this.addItem(productId, -1)
    }

    getSubtotal() {
        return this.data.reduce((subtotal, item) => {
            const product = this.products.find(p => p.id === item.productId);
            return subtotal + (product ? product.price * item.quantity : 0);
        }, 0);
    }

    getSaving() {
        return this.data.reduce((saving, item) => {
            const product = this.products.find(p => p.id === item.productId);
            return saving + (product ? (product.price - product.discountPrice) * item.quantity : 0);
        }, 0);
    }

    getProductTotal(productId) {
        const product = this.products.find(p => p.id === productId);
        const cartItem = this.data.find(item => item.productId === productId);

        if (product && cartItem) {
            return product.discountPrice * cartItem.quantity;
        }

        return 0;
    }

    getTotal() {
        return this.getSubtotal() + this.getTax() + this.getShipping() - this.getSaving();
    }


    // implement later
    getTax() {
        return 0;
    }

    getShipping() {
        return 0;
    }

}
