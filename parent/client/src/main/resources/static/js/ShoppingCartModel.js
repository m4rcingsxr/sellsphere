/**
 * Model for managing the shopping cart data.
 */
class ShoppingCartModel {

    maxQuantityCartItem = 5;
    maxProductsCart = 25;
    cartBaseUrl = MODULE_URL + "cart";
    productBaseUrl = MODULE_URL + "products";

    constructor() {
        this.data = JSON.parse(localStorage.getItem('cart')) || [];
    }

    /**
     * Initializes the products from the database.
     */
    async initializeProducts() {
        try {
            console.debug(`Initializing products from ${this.productBaseUrl}`);

            if (this.data.length > 0) {
                const productIds = this.data.map(product => product.productId);
                this.products = await ajaxUtil.post(`${this.productBaseUrl}`, productIds);
                console.debug(`Products initialized successfully`);
            }
        } catch (error) {
            console.error('Error occurred while fetching products from db', error);
            throw error;
        }
    }

    /**
     * Adds an item to the cart with the specified quantity change.
     * @param {number} productId - The ID of the product to add.
     * @param {number} quantityChange - The quantity change (default is 1).
     */
    async addItem(productId, quantityChange = 1) {
        console.debug(`Adding item to cart: productId=${productId}, quantityChange=${quantityChange}`);

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
                await this.updateCartItemInDb(this.data[productIndex].productId, this.data[productIndex].quantity);
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
                await this.updateCartItemInDb(this.data[productIndex].productId, this.data[productIndex].quantity);
            }
        }

        this.updateLocalStorage();
    }

    /**
     * Updates the cart item quantity in the database.
     * @param {number} productId - The ID of the product to update.
     * @param {number} quantity - The new quantity of the product.
     */
    async updateCartItemInDb(productId, quantity) {
        try {
            const url = `${this.cartBaseUrl}/add/${productId}/${quantity}`;
            await ajaxUtil.post(url, {});
            console.debug(`Product id[${productId}] quantity[${quantity}] updated in DB cart successfully`);
        } catch (error) {
            console.error(`Error adding product id[${productId}] quantity[${quantity}] to cart:`, error);
            throw error;
        }
    }

    /**
     * Merges the local cart state with the remote cart state.
     */
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

            console.debug(`Cart quantities successfully merged with DB cart`);

        } catch (error) {
            console.error(`[ShoppingCartModel.merge] : Error merging cart items:`, error);
            throw error;
        }
    }

    /**
     * Sets the cart in the database.
     * @param {Array} cart - The cart data to set.
     */
    async setCart(cart) {
        try {
            const url = `${this.cartBaseUrl}/set`;
            await ajaxUtil.post(url, this.data);
            console.debug(`Cart set as DB cart successfully`);
        } catch (error) {
            console.error(`Error setting cart:`, error);
        }
    }

    async removeItem(productId) {
        const index = this.data.findIndex(item => Number(item.productId) === Number(productId));

        if(index !== -1) {
            this.data.splice(index, 1);
            this.updateLocalStorage();
        }

        if (LOGGED_IN) {
           await this.removeCartItemFromDb(productId);
        }
    }

    /**
     * Removes a cart item from the database.
     * @param {number} productId - The ID of the product to remove.
     */
    async removeCartItemFromDb(productId) {
        try {
            await ajaxUtil.delete(`${this.cartBaseUrl}/delete/${productId}`);
            console.debug(`Product id[${productId}] removed from DB cart successfully`);
        } catch (error) {
            console.error(`Error deleting product id[${productId}] from cart:`, error);
            showErrorModal(error.response);
        }
    }

    /**
     * Clears the cart.
     */
    async clear() {
        if(this.data.length > 0) {
            const tempData = this.data;
            this.data = [];
            this.updateLocalStorage();

            if (LOGGED_IN) {
                try {
                    await this.clearDbCart();
                } catch(error) {
                    this.data = tempData;
                    throw new Error();
                }
            }
        }
    }

    /**
     * Clears the cart in the database.
     */
    async clearDbCart() {
        try {
            await ajaxUtil.post(`${this.cartBaseUrl}/clear`);

            console.debug('[ShoppingCartModel._clearDBCart] : Database cart cleared successfully');
        } catch (error) {
            console.error('[ShoppingCartModel._clearDBCart] : Error updating database:', error);
            throw error;
        }
    }

    updateLocalStorage() {
        localStorage.setItem('cart', JSON.stringify(this.data));
    }

    /**
     * Increases the quantity of a product in the cart.
     * @param {number} productId - The ID of the product to increase the quantity of.
     */
    async increaseQuantity(productId) {
        await this.addItem(productId, 1);
    }

    /**
     * Decreases the quantity of a product in the cart.
     * @param {number} productId - The ID of the product to decrease the quantity of.
     */
    async decreaseQuantity(productId) {
        await this.addItem(productId, -1)
    }

    /**
     * Gets the subtotal of the cart.
     * @returns {number} - The subtotal of the cart.
     */
    getSubtotal() {
        return this.data.reduce((subtotal, item) => {
            const product = this.products.find(p => p.id === item.productId);
            return subtotal + (product ? product.price * item.quantity : 0);
        }, 0);
    }

    /**
     * Gets the savings of the cart.
     * @returns {number} - The savings of the cart.
     */
    getSaving() {
        return this.data.reduce((saving, item) => {
            const product = this.products.find(p => p.id === item.productId);
            return saving + (product ? (product.price - product.discountPrice) * item.quantity : 0);
        }, 0);
    }

    /**
     * Gets the total price for a specific product in the cart.
     * @param {number} productId - The ID of the product.
     * @returns {number} - The total price for the product.
     */
    getProductTotal(productId) {
        const product = this.products.find(p => p.id === productId);
        const cartItem = this.data.find(item => item.productId === productId);

        if (product && cartItem) {
            return product.discountPrice * cartItem.quantity;
        }

        return 0;
    }

    /**
     * Gets the total price of the cart.
     * @returns {number} - The total price of the cart.
     */
    getTotal() {
        return this.getSubtotal() + this.getTax() + this.getShipping() - this.getSaving();
    }


    // TODO:
    /**
     * Gets the tax for the cart (placeholder).
     * @returns {number} - The tax for the cart.
     */
    getTax() {
        return 0;
    }

    /**
     * Gets the shipping cost for the cart (placeholder).
     * @returns {number} - The shipping cost for the cart.
     */
    getShipping() {
        return 0;
    }


}
