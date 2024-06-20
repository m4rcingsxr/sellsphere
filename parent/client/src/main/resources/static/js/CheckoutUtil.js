class CheckoutUtil {

    /**
     * Checks if 20 minutes have passed since the last update of the exchange rate response.
     * @param {Object} exchangeRateResponse - The exchange rate response object.
     * @returns {boolean} - True if 20 minutes have passed, false otherwise.
     */
    static isExchangeRateExpired(exchangeRateResponse) {
        const now = new Date();
        const updated = new Date(exchangeRateResponse.updated);
        const differenceInMinutes = (now - updated) / (1000 * 60);
        return differenceInMinutes >= 20;
    }

    /**
     * Maps a platform entity address to a Stripe contact format.
     */
    static mapAddressToStripeContact(address) {
        return {
            name: address.fullName,
            address: {
                line1: address.addressLine1,
                line2: address.addressLine2,
                city: address.city,
                state: address.state,
                postal_code: address.postalCode,
                country: address.countryCode,
            },
            phone: address.phoneNumber
        };
    }

    /**
     * Maps a Stripe address to a rate request format.
     */
    static mapStripeAddressToRateRequest(address, fullName, phoneNumber) {
        return {
            address: {
                city: address.city,
                countryCode: address.country,
                addressLine1: address.line1,
                addressLine2: address.line2,
                postalCode: address.postal_code,
                state: address.state,
                fullName,
                phoneNumber
            }
        };
    }

    /**
     * Returns options for the Stripe payment element.
     */
    static getPaymentElementOptions() {
        return {
            layout: {
                type: 'accordion',
                defaultCollapsed: false,
                radios: false,
                spacedAccordionItems: true,
                visibleAccordionItemsCount: 5,
                business: { name: 'SellSphere' }
            }
        };
    }

    /**
     * Returns options for the Stripe address element.
     */
    static getAddressElementOptions(allowedCountries, contacts) {
        return {
            mode: 'shipping',
            autocomplete: { mode: 'automatic' },
            allowedCountries,
            blockPoBoxes: 'true',
            contacts,
            fields: { phone: 'always' },
            validation: { phone: { required: 'always' } },
            name: 'full'
        };
    }
}