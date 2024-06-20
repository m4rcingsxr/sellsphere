class CheckoutController {

    stripe = null;
    elements = null;
    model = null;
    view = null;


    constructor(model, view) {
        this.stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");
        this.model = model;
        this.view = view;
        this.init();
    }

    /**
     * Initializes the Stripe elements,payment and address element.
     */
    async init() {
        await this.initStripeElements();
        await this.initializeStripePaymentElement();
        await this.initAddressElement();

        this.initAddressElementListener();

        $("#address-btn, #continue-to-payment-method-btn").on("click", this.handleShowPaymentMethodTab.bind(this));
    }

    /**
     * Initializes Stripe elements by fetching customer session and client secrets.
     */
    async initStripeElements() {
        try {
            const customerSessionClientSecret = await this.model.fetchCustomerSessionClientSecret();
            const clientSecret = await this.model.fetchClientSecret();

            this.elements = this.stripe.elements({
                customerSessionClientSecret,
                clientSecret,
                loader: 'always'
            });
        } catch(error) {
            // handle creating customer session or retrieving/creating new payment intent with cart amount errors
            console.error(error);
        }
    }

    /**
     * Creates and mounts the Stripe payment element.
     */
    async initializeStripePaymentElement() {

        const options = {
            layout: {
                type: 'accordion',
                defaultCollapsed: false,
                radios: false,
                spacedAccordionItems: true,
                visibleAccordionItemsCount: 5,
                business: {
                    name: 'SellSphere'
                },
            },

        }

        this.elements.create('payment', options).mount("#payment-element");
    }

    /**
     * Initializes the Stripe address element by fetching customer addresses and shippable countries.
     * Maps the addresses to Stripe contacts and calls the initialization function.
     */
    async initAddressElement() {
        try {
            const customerAddresses = await this.model.getCustomerAddresses();
            const allowedCountries = await this.model.getShippableCountries();

            let contacts = [];
            if (customerAddresses?.length > 0) {
                contacts = customerAddresses.map(this.mapAddressToStripeContact);
            }

            this.initStripeAddressElement(allowedCountries.map(country => country.code), contacts)
        } catch (error) {
            console.error("Error occurred during fetching addresses " + error);
            throw error;
        }
    }

    /**
     * Maps a platform entity address to a Stripe contact format.
     *
     * @param {Object} address - The address object from the platform.
     * @returns {Object} - The address formatted as a Stripe contact.
     */
    mapAddressToStripeContact(address) {
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
        }
    }

    /**
     * Initializes the Stripe address element with allowed countries and contacts.
     *
     * @param {Array} allowedCountries - List of allowed country codes.
     * @param {Array} [contacts=[]] - List of contacts formatted for Stripe.
     */
    initStripeAddressElement(allowedCountries, contacts = []) {
        const options = {
            mode: 'shipping',
            autocomplete: {
                mode: 'automatic'
            },
            allowedCountries,
            blockPoBoxes: 'true', // block a post office box
            contacts,
            fields: {
                phone: 'always'
            },
            validation: {
                phone: {
                    required: 'always'
                }
            },
            name: 'full'
        };

        this.elements.create('address', options).mount("#address-element");
    }

    /**
     * Initializes the address element listener to validate the address on change.
     * Uses a debounce function to limit the rate of validation calls.
     */
    initAddressElementListener() {
        const addressElement = this.elements.getElement('address');
        addressElement.on("change", debounce(this.handleAddress.bind(this), 500));
    }

    /**
     * Validates the address when it changes and fetches shipping rates if the address is complete.
     *
     * @param {Object} event - The event object from the address element change.
     * @throws {Error} If no shipping rates are available for the destination address.
     */
    async handleAddress(event) {
        if (!event.complete) {
            return
        }

        this.view.enableContinueToPaymentBtnSpinner();


        const { address } = event.value;
        const easyshipAddress = this.mapStripeAddressToRateRequest(address, event.value.name, event.value.phone);

        // fetch shipping rates or throw error
        try {
            const response = await this.model.getShippingRates(easyshipAddress, 0);

            // validate rates
            if (response.rates.length === 0) throw new Error("No rates available for destination " + address);

            // persist shipping rates
            this.model.rateResponse = response;

            // enable continue to payment button
            this.view.disableContinueToPaymentBtnSpinner();
        } catch(error) {
            // handle not available rates or exception from rates request - tell user's that this address does not have available creates (not available for shipping)
            console.error(error);
        }
    }

    mapStripeAddressToRateRequest(address, fullName, phoneNumber) {
        return {
            address : {
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

    async handleShowPaymentMethodTab(event) {
        event.preventDefault();

        try {
            // validate if rates exist - otherwise cannot process
            if (this.model.rateResponse == null || this.model.rates.length === 0)
                throw new Error("Cannot process without available shipping rates. Please provide valid address.");
        } catch(error) {
            // handle no shipping rates available for the address
            // user can click it before providing address
            // prompt the user to provide correct address
            console.error(error);
            throw error;
        }

        try {
            // calculate tax to show payment details
            const taxCalculation = await this.model.getTaxCalculation({

            });
        } catch(error) {

            //calculation request error
            console.error(error);
        }

    }

}