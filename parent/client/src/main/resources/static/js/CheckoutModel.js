class CheckoutModel {

    constructor() {

    }

    async fetchShippableAddresses() {
        const addresses = await ajaxUtil.get(`${MODULE_URL}shipping/shippable-addresses`);

        // const validatedAddresses = [];

        // addresses.forEach(address => {
        //     const isValid = await this.validateAddress({
        //
        //     });
        //
        //     if(isValid) {
        //
        //     } else {
        //         // show address is not valid
        //     }
        // })

        return addresses;
    }

    async fetchShippingRates(address) {
        return await ajaxUtil.post(`${MODULE_URL}shipping/rates`, address);
    }

    async fetchShippableCountries() {
        return await ajaxUtil.get(`${MODULE_URL}shipping/supported-countries`);
    }

    async fetchCalculations(address = undefined, shippingCost = undefined) {
        let request = null;
        if(address && shippingCost) {
            request = {address, shippingCost};
        }

        return await ajaxUtil.post(`${MODULE_URL}checkout/calculate`, request);
    }

    async createPaymentIntent() {
        return await ajaxUtil.post(`${MODULE_URL}checkout/create-payment-intent`, {
            currencyCode : this.calculationResponse.currencyCode,
            amountTotal : this.calculationResponse.amountTotal,
            customerDetails : this.calculationResponse.customerDetails
        });
    }

    async validateAddress(request) {
        try {
            // Perform POST request
            return await ajaxUtil.post(`${MODULE_URL}addresses/validate`, request);

        } catch (error) {
            console.error('Error validating address:', error);
        }
    }

}



