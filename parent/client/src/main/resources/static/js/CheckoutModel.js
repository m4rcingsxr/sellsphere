class CheckoutModel {

    exchangeRateResponse = null;
    targetCurrency = null;
    baseCurrency = null;
    selectedCurrency = null;
    selectedRateIndex = 0;

    constructor() {
    }

    clearSummaryData() {
        this.exchangeRateResponse = null;
        this.targetCurrency = null;
        this.baseCurrency = null;
        this.selectedCurrency = null;
        this.selectedRateIndex = 0;
    }

    /**
     * Fetches the customer addresses.
     * @returns {Promise<Object>} - A promise that resolves to an array of customer addresses.
     * @throws {Error} - If there is an error fetching the customer addresses.
     */
    async getCustomerAddresses() {
        try {
            return await ajaxUtil.get(`${MODULE_URL}addresses`);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    /**
     * Fetches the shippable countries.
     * @returns {Promise<Object>} - A promise that resolves to an array of shippable countries.
     * @throws {Error} - If there is an error fetching the shippable countries.
     */
    async getShippableCountries() {
        try {
            return await ajaxUtil.get(`${MODULE_URL}shipping/supported-countries`);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    //
    async fetchCustomerSessionClientSecret() {
        try {
            const response = await ajaxUtil.post(`${MODULE_URL}checkout/create-customer-session`, {});
            return response.customerSessionClientSecret;
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async fetchClientSecret() {
        try {
            const response = await ajaxUtil.post(`${MODULE_URL}checkout/init-payment-intent`, {});
            return response.clientSecret;
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    /**
     * Fetches the shipping rates for the given address.
     * @param {Object} address - The address object.
     * @param {number} page - The page number for pagination.
     * @returns {Promise<Object>} - A promise that resolves to the shipping rates object.
     * @throws {Error} - If there is an error fetching the shipping rates.
     */
    async getShippingRates(address, page) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}shipping/rates?page=${page}`, address);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    /**
     * Fetches the detailed calculation for the checkout.
     * @param {Object} request - The request object for the calculation.
     * @returns {Promise<Object>} - A promise that resolves to the calculation object.
     * @throws {Error} - If there is an error fetching the calculation.
     */
    async getTaxCalculation(request) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}checkout/calculate`, request);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async getBaseCurrencyCode() {
        try {
            const response = await ajaxUtil.get(`${MODULE_URL}settings/currency`);
            return response.currencyCode;
        } catch (error) {
            console.error(error);
            throw error;
        }
    }


    async getExchangeRateWithPrice(amount, baseCode, targetCode) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}exchange-rates/amount/${amount}/currency/${baseCode}/${targetCode}`);
        } catch (error) {
            console.error(error);
            throw (error);
        }
    }

    /**
     * Fetches the country information based on the client's IP address.
     * @returns {Promise<Object>} - A promise that resolves to the country information object.
     */
    async getCountryForClientIp() {
        try {
            const clientIpJson = await this.getClientIp();
            const response = await clientIpJson.json();
            return await ajaxUtil.post(`${MODULE_URL}countries/country-ip`, {ip: response.ip});
        } catch (error) {
            console.error(error);
            throw (error);
        }
    }

    /**
     * Fetches the client's IP address.
     * @returns {Promise<Response>} - A promise that resolves to the client's IP address.
     */
    async getClientIp() {
        try {
            return await fetch('https://api.ipify.org?format=json');
        } catch (error) {
            console.error(error);
            throw (error);
        }
    }

    async getCountryDetails(countryCode) {
        try {
            return await ajaxUtil.get(`https://restcountries.com/v3.1/alpha/${countryCode}`);
        } catch (error) {
            console.error(error);
            throw (error);
        }
    }

}