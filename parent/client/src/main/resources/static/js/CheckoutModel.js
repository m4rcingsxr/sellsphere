class CheckoutModel {

    customerAddresses = null;

    exchangeRateResponse = null;
    targetCurrency = null;
    baseCurrency = null;
    selectedCurrency = null;
    appliedCalculation = null;
    selectedRateIndex = 0;

    constructor() {
    }

    clearSummaryData() {
        this.exchangeRateResponse = null;
        this.targetCurrency = null;
        this.baseCurrency = null;
        this.selectedCurrency = null;
        this.appliedCalculation = null;
        this.selectedRateIndex = 0;
    }

    async getCartTotal() {
        try {
            return await ajaxUtil.get(`${MODULE_URL}checkout/cart-total`);
        } catch(error) {
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

    // on persist - update final collected payment intent data
    async savePaymentIntent() {
        try {
            const appliedCalculation = this.appliedCalculation;

            if (appliedCalculation == null) {
                throw new Error("Illegal state. Model should have applied calculation.");
            }

            const address = appliedCalculation.address;

            if (this?.customerAddresses.length > 0) {
                const matchingAddressId = this.findMatchingAddressId(address, this.customerAddresses);
                if (matchingAddressId) {
                    address.id = matchingAddressId;
                    console.log(`Matching address found with ID: ${matchingAddressId}`);
                } else {
                    console.log('No matching address found');
                }
            }

            if(!this?.rateResponse?.rates || this.rateResponse.rates.length === 0) {
                throw new Error("Illegal state. No shipping rates for provided destination: " + address)
            }

            const selectedRate = this.rateResponse.rates[this.selectedRateIndex];

            if(!this?.exchangeRateResponse) {
                throw new Error("Illegal state. Exchange rates should be presented.")
            }

            const exchangeRate = this.exchangeRateResponse.result["rate"];

            return await ajaxUtil.post(`${MODULE_URL}checkout/save-payment-intent`, {
                address,
                currencyCode : appliedCalculation.currencyCode,
                amountTotal : appliedCalculation.amountTotal,
                amountTax : appliedCalculation.taxAmountInclusive,
                shippingAmount : appliedCalculation.shippingCost.amount,
                shippingTax : appliedCalculation.shippingCost.shippingCostTax,
                courierName : selectedRate.courierName,
                courierLogoUrl : selectedRate.courierLogoUrl,
                maxDeliveryTime : selectedRate.maxDeliveryTime,
                minDeliveryTime : selectedRate.minDeliveryTime,
                exchangeRate
            });
        } catch(error) {
            console.error(error);
            throw error;
        }
    }

    findMatchingAddressId(address, customerAddresses) {
        // Normalize the address data for comparison (trim and convert to lower case)
        const normalize = (str) => (str ? str.trim().toLowerCase() : '');

        // Extract and normalize the essential fields from the given address
        const givenAddress = {
            addressLine1: normalize(address.addressLine1),
            addressLine2: normalize(address.addressLine2),
            city: normalize(address.city),
            state: normalize(address.state),
            countryCode: normalize(address.countryCode),
            postalCode: normalize(address.postalCode)
        };

        // Iterate over the customer addresses and compare with the given address
        for (const customerAddress of customerAddresses) {
            // Extract and normalize the essential fields from the customer address
            const currentAddress = {
                addressLine1: normalize(customerAddress.addressLine1),
                addressLine2: normalize(customerAddress.addressLine2),
                city: normalize(customerAddress.city),
                state: normalize(customerAddress.state),
                countryCode: normalize(customerAddress.countryCode),
                postalCode: normalize(customerAddress.postalCode)
            };

            // Check if all the essential fields match
            if (
                givenAddress.addressLine1 === currentAddress.addressLine1 &&
                givenAddress.addressLine2 === currentAddress.addressLine2 &&
                givenAddress.city === currentAddress.city &&
                givenAddress.state === currentAddress.state &&
                givenAddress.countryCode === currentAddress.countryCode &&
                givenAddress.postalCode === currentAddress.postalCode
            ) {
                // Return the matching customer address ID
                return customerAddress.id;
            }
        }

        // Return null if no matching address is found
        return null;
    }


}