/**
 * Model for managing the data related to the checkout process.
 */
class CheckoutModel {
    constructor() {
        this.baseCalculation = null;
        this.ratesResponse = null;
        this.selectedCurrency = null;
        this.selectedRateIdx = null;
        this.exchangeRateResponse = null;
        this.summaryAddress = null;
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
     * Fetches the basic calculation for the checkout.
     * @returns {Promise<Object>} - A promise that resolves to the basic calculation object.
     * @throws {Error} - If there is an error fetching the basic calculation.
     */
    async getBasicCalculation() {
        try {
            return await ajaxUtil.post(`${MODULE_URL}checkout/calculate-total`);
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
    async getCalculation(request) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}checkout/calculate-all`, request);
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
     * Validates the given address.
     * @param {Object} addressRequest - The address validation request object.
     * @returns {Promise<boolean>} - A promise that resolves to a boolean indicating whether the address is valid.
     * @throws {Error} - If there is an error validating the address.
     */
    async validateAddress(addressRequest) {
        return await ajaxUtil.post(`${MODULE_URL}addresses/validate`, addressRequest);

    }

    /**
     * Fetches the exchange rates for the client's country.
     * @param {string} targetCurrency - The target currency code.
     * @returns {Promise<Object>} - A promise that resolves to the exchange rates object.
     */
    async getExchangeRatesForClientCountry(targetCurrency) {
        return await ajaxUtil.post(`${MODULE_URL}exchange-rates/amount/${this.baseCalculation.amountTotal}/${this.baseCalculation.unitAmount}/currency/${this.baseCalculation.currencyCode}/${targetCurrency}`);
    }

    /**
     * Fetches the country information based on the client's IP address.
     * @returns {Promise<Object>} - A promise that resolves to the country information object.
     */
    async getCountryForClientIp() {
        const clientIpJson = await this.getClientIp();
        const response = await clientIpJson.json();
        return await ajaxUtil.post(`${MODULE_URL}countries/country-ip`, {ip: response.ip});
    }

    /**
     * Fetches the client's IP address.
     * @returns {Promise<Response>} - A promise that resolves to the client's IP address.
     */
    async getClientIp() {
        return await fetch('https://api.ipify.org?format=json');
    }

    /**
     * Fetches the country data for the given country code.
     * @param {string} countryCode - The country code.
     * @returns {Promise<Object>} - A promise that resolves to the country data object.
     * @throws {Error} - If there is an error fetching the country data.
     */
    async getCountryData(countryCode) {
        return await ajaxUtil.get(`https://restcountries.com/v3.1/alpha/${countryCode}`);
    }

    // create or update payment intent - based on authenticated customer
    async savePaymentIntent(amountTotal, currencyCode, courierId, recipientName, customerEmail, phoneNumber) {
        return await ajaxUtil.post(`${MODULE_URL}checkout/save-payment-intent`, {
            currencyCode,
            amountTotal,
            phoneNumber,
            customerDetails : this.baseCalculation.customerDetails,
            metadata : {
                "courier_id" : courierId,
                "recipient_name" : recipientName,
                "email" : customerEmail
            }
        });
    }
}