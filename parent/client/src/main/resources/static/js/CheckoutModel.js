class CheckoutModel {

    constructor() {

    }

    async fetchShippableAddresses() {
        return await ajaxUtil.get(`${MODULE_URL}shipping/shippable-addresses`);
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

}