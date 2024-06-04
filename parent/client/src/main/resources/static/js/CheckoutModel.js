class CheckoutModel {
    constructor() {
        this.baseCalculation = null;
        this.ratesResponse = null;
        this.selectedCurrency = null;
    }

    async getShippableCountries() {
        try {
            return await ajaxUtil.get(`${MODULE_URL}shipping/supported-countries`);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async getCustomerAddresses() {
        try {
            return await ajaxUtil.get(`${MODULE_URL}addresses`);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async getBasicCalculation() {
        try {
            return await ajaxUtil.post(`${MODULE_URL}checkout/calculate-basic`);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async getCalculation(request) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}checkout/calculate`, request);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async getShippingRates(address, page) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}shipping/rates?page=${page}`, address);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async validateAddress(addressRequest) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}addresses/validate`, addressRequest);
        } catch (error) {
            console.error(error);
            throw error;
        }
    }

    async getExchangeRatesForClientCountry(targetCurrency) {
        return await ajaxUtil.post(`${MODULE_URL}exchange-rates/amount/${this.baseCalculation.amountTotal}/${this.baseCalculation.unitAmount}/currency/${this.baseCalculation.currencyCode}/${targetCurrency}`);
    }

    async getCountryForClientIp() {
        const clientIpJson = await this.getClientIp();
        const response = await clientIpJson.json();
        return await ajaxUtil.post(`${MODULE_URL}countries/country-ip`, { ip: response.ip });
    }

    async getClientIp() {
        return await fetch('https://api.ipify.org?format=json');
    }

    async getCountryData(countryCode) {
        return await ajaxUtil.get(`https://restcountries.com/v3.1/alpha/${countryCode}`);
    }
}
