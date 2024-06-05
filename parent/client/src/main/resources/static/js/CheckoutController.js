/**
 * Controller for managing the checkout process.
 */
class CheckoutController {

    /**
     * Constructs the CheckoutController with the given model and view.
     * @param {Object} model - The data model for the checkout process.
     * @param {Object} view - The view for the checkout process.
     */
    constructor(model, view) {
        console.info("Initializing CheckoutController");
        this.stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");
        this.model = model;
        this.view = view;
        this.elements = null;
        this.addressElement = null;
        this.paymentElement = null;
        this.paymentEvent = null;

        this.init();
    }

    /**
     * Initializes the controller by setting up the address element and event listeners.
     * @async
     */
    async init() {
        console.info("Initializing controller");
        await this.initAddressElement();
        this.initEventListeners();
    }

    /**
     * Initializes all event listeners for the checkout process.
     */
    initEventListeners() {
        console.debug("Initializing event listeners");
        this.initAddressChangeListener();
        this.initContinueToPaymentButton();
        this.initAddressAccordionButton();
        this.initShowSummaryButton();
        this.initChangeCourierListener();
        this.initChangeCurrencyButton();
    }

    /**
     * Initializes the event listener for address changes with debounced validation.
     */
    initAddressChangeListener() {
        console.debug("Initializing address change event listener");
        this.addressElement.on("change", this.debounce(this.validateAddress.bind(this), 1000));
    }

    /**
     * Creates a debounced version of the provided function.
     * @param {Function} fn - The function to debounce.
     * @param {number} delay - The debounce delay in milliseconds.
     * @returns {Function} - The debounced function.
     */
    debounce(fn, delay) {
        let timeout;
        return function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => fn.apply(this, args), delay);
        };
    }

    /**
     * Validates the address input.
     * @async
     * @param {Object} event - The change event from the address element.
     */
    async validateAddress(event) {
        console.debug("Validating address", event);
        if (event.complete) {
            const address = event.value.address;
            console.info("Address input complete, validating address", address);

            const addressLines = [address.line1, address.line2].filter(line => line);
            const validateRequest = this.createAddressRequest(address, addressLines);

            const isValid = await this.model.validateAddress(validateRequest);

            if (isValid) {
                console.info("Address validated successfully", address);
                await this.handleValidAddress(address);
            } else {
                console.warn("Address validation failed", address);
                this.view.showAddressError();
                this.view.hideAddressButton();
            }
        }
    }

    /**
     * Creates an address validation request object.
     * @param {Object} address - The address object.
     * @param {Array} addressLines - The address lines array.
     * @returns {Object} - The address validation request object.
     */
    createAddressRequest(address, addressLines) {
        console.debug("Creating address validation request", address, addressLines);
        return {
            address: {
                regionCode: address.country,
                postalCode: address.postal_code,
                locality: address.city,
                addressLines,
            },
        };
    }

    /**
     * Handles actions when the address is validated successfully.
     * @async
     * @param {Object} address - The validated address.
     */
    async handleValidAddress(address) {
        console.info("Handling valid address", address);

        const easyShipAddress = this.createEasyShipAddress(address);
        const ratesResponse = await this.model.getShippingRates(easyShipAddress, 0);

        this.validateRatesResponse(ratesResponse, easyShipAddress);

        const shippingCost = ratesResponse.rates[0].totalCharge;
        const calculation = await this.model.getCalculation({ address, shippingCost });

        this.updateModelAfterCalculation(calculation, ratesResponse);
        await this.initStripeElements(calculation);

        this.view.renderSummary(calculation);
        this.view.showSummaryView();
        this.view.showAddressButton();
    }

    /**
     * Creates an easy ship address object.
     * @param {Object} address - The address object.
     * @returns {Object} - The easy ship address object.
     */
    createEasyShipAddress(address) {
        console.debug("Creating easy ship address object", address);
        return {
            city: address.city,
            countryCode: address.country,
            addressLine1: address.line1,
            addressLine2: address.line2,
            postalCode: address.postal_code,
            state: address.state
        };
    }

    /**
     * Validates the rates response and throws an error if necessary.
     * @param {Object} ratesResponse - The rates response object.
     * @param {Object} easyShipAddress - The easy ship address object.
     */
    validateRatesResponse(ratesResponse, easyShipAddress) {
        console.debug("Validating rates response", ratesResponse);
        if (ratesResponse.length === 0) {
            console.warn("No shipping rate available for address", easyShipAddress);
            throw new Error("No shipping rate available for address: " + JSON.stringify(easyShipAddress));
        }

        if (ratesResponse.rates[0].costRank !== 1.0) {
            console.warn("Cost rank of 1st rate is not 1", ratesResponse);
            throw new Error("Cost rank of 1st rate should always be 1: " + JSON.stringify(ratesResponse));
        }
    }

    /**
     * Updates the model after the calculation is fetched.
     * @param {Object} calculation - The calculation object.
     * @param {Object} ratesResponse - The rates response object.
     */
    updateModelAfterCalculation(calculation, ratesResponse) {
        console.debug("Updating model after calculation", calculation, ratesResponse);
        this.model.baseCalculation = calculation;
        this.model.ratesResponse = ratesResponse;
        this.model.selectedCurrency = calculation.currencyCode;
    }

    /**
     * Initializes the Stripe address element.
     * @async
     */
    async initAddressElement() {
        console.info("Initializing address element");

        const calculation = await this.model.getBasicCalculation();

        await this.initStripeElements(calculation);
        await this.initAddressOptions();

        this.view.renderProductSummary(calculation);
    }

    /**
     * Initializes the Stripe payment element.
     */
    initPaymentElement() {
        console.info("Initializing payment element");

        const options = {
            layout: {
                type: "tabs",
                defaultCollapsed: false,
            }
        };

        if (!this.paymentElement) {
            this.paymentElement = this.elements.create("payment", options);
            this.paymentElement.mount("#payment-element");

            this.paymentElement.on('change', event => {
                console.info("Payment element change event detected", event);
                this.paymentEvent = event;
            });
        } else {
            console.debug("Payment element already initialized");
        }
    }

    /**
     * Initializes or updates the Stripe elements with the given calculation.
     * @param {Object} calculation - The calculation data.
     */
    initStripeElements(calculation) {
        console.debug("Initializing Stripe elements", calculation);
        if (!this.elements) {
            this.elements = this.stripe.elements({
                appearance: {
                    theme: 'flat'
                },
                mode: 'payment',
                amount: calculation.amountTotal,
                currency: calculation.currencyCode.toLowerCase(),
            });
        } else {
            console.debug("Updating Stripe elements", calculation);
            this.elements.update({
                amount: calculation.amountTotal,
                currency: calculation.currencyCode.toLowerCase()
            });
        }
    }

    /**
     * Initializes the options for the Stripe address element.
     * @async
     */
    async initAddressOptions() {
        console.debug("Initializing address element options");

        const options = {
            mode: 'shipping',
        };

        const shippableCountries = await this.model.getShippableCountries();
        options.allowedCountries = shippableCountries.map(country => country.code);

        const addresses = await this.model.getCustomerAddresses();
        if (addresses?.length > 0) {
            console.info("Setting contacts for address element", addresses);
            options.contacts = addresses.map(address => ({
                name: address.fullName,
                address: {
                    line1: address.addressLine1,
                    line2: address.addressLine2,
                    city: address.city,
                    state: address.state,
                    postal_code: address.postalCode,
                    country: address.countryCode,
                },
            }));
        }

        this.addressElement = this.elements.create('address', options);
        this.addressElement.mount("#address-element");
    }

    /**
     * Initializes the event listener for the continue to payment button.
     */
    initContinueToPaymentButton() {
        console.debug("Initializing continue to payment button event listener");
        $("#address-btn, #payment-accordion-btn").on("click", this.handleContinueToPayment.bind(this));
    }

    /**
     * Handles the continue to payment action.
     * @async
     * @param {Object} event - The event object.
     */
    async handleContinueToPayment(event) {
        event.preventDefault();
        console.info("Handling continue to payment action");

        if (this.model?.baseCalculation?.customerDetails?.address) {
            console.debug("Address is defined, proceeding to payment step");
            this.view.hideAddressButton();
            this.view.showPaymentButton();
            this.view.hidePlaceOrderButton();
            this.view.hideCurrencies();

            this.initPaymentElement();

            this.view.renderSummary(this.model.baseCalculation);

            const paymentAccordion = new bootstrap.Collapse("#checkout-payment-accordion", { toggle: true });
            paymentAccordion.show();
        } else {
            console.warn("Address must be defined before payment step");
        }
    }

    /**
     * Initializes the event listener for the address accordion button.
     */
    initAddressAccordionButton() {
        console.debug("Initializing address accordion button event listener");
        $("#address-accordion-btn").on("click", event => {
            event.preventDefault();
            console.info("Address accordion button clicked");

            this.view.showAddressButton();
            this.view.hidePaymentButton();
            this.view.hidePlaceOrderButton();
            this.view.hideCurrencies();

            this.view.renderSummary(this.model.baseCalculation);

            const addressAccordion = new bootstrap.Collapse("#checkout-address-accordion", { toggle: true });
            addressAccordion.show();
        });
    }

    /**
     * Initializes the event listener for the show summary button.
     */
    initShowSummaryButton() {
        console.debug("Initializing show summary button event listener");
        $("#payment-btn, #summary-accordion-btn").on("click", async event => {
            event.preventDefault();
            console.info("Show summary button clicked");

            if (this.model.baseCalculation?.customerDetails?.address && this.paymentEvent.complete) {
                console.debug("Address and payment step complete, proceeding to summary step");
                this.view.hideAddressButton();
                this.view.hidePaymentButton();
                this.view.showPlaceOrderButton();
                this.view.showCurrencies();


                const summaryAccordion = new bootstrap.Collapse("#checkout-summary-accordion", { toggle: true });

                this.view.renderSummaryData(this.model.baseCalculation, this.model.ratesResponse);
                await this.loadCurrencyData();

                summaryAccordion.show();
            } else {
                console.warn("Address and payment step must be complete before summary step");
            }
        });
    }

    /**
     * Initializes the event listener for changing the courier.
     */
    initChangeCourierListener() {
        console.debug("Initializing change courier event listener");
        $("#summary").on("click", 'input[type="radio"]', async event => {
            console.info("Courier change detected", event);

            const rates = this.model.ratesResponse.rates;
            const selectedRate = rates[event.target.dataset.addressIdx];
            const currentAddress = this.model.baseCalculation.customerDetails.address;

            const country = await this.model.getCountryForClientIp();
            const exchangeRateResponse = await this.model.getExchangeRatesForClientCountry(country.currencyCode);

            const exchangeRate = exchangeRateResponse.result["rate"];
            const targetCalculation = await this.model.getCalculation({
                address: currentAddress,
                shippingCost: selectedRate.totalCharge,
                currencyCode: this.model.selectedCurrency,
                exchangeRate
            });

            this.model.baseCalculation = await this.model.getCalculation({
                address: currentAddress,
                shippingCost: selectedRate.totalCharge,
            });

            if (this.model.selectedCurrency === this.model.baseCalculation.currencyCode) {
                this.initStripeElements(this.model.baseCalculation);
            } else {
                this.initStripeElements(targetCalculation);
            }

            await this.loadCurrencyData();

            if (this.model.selectedCurrency !== this.model.baseCalculation.currencyCode) {
                this.view.renderSummary(targetCalculation);
            } else {
                this.view.renderSummary(this.model.baseCalculation);
            }
        });
    }

    /**
     * Loads the currency data and updates the view accordingly.
     * @async
     */
    async loadCurrencyData() {
        console.debug("Loading currency data");

        const country = await this.model.getCountryForClientIp();
        const exchangeRateResponse = await this.model.getExchangeRatesForClientCountry(country.currencyCode);

        const targetCurrency = country.currencyCode;
        const exchangeRate = exchangeRateResponse.result["rate"];
        const convertedPrice = exchangeRateResponse.result[country.currencyCode];

        const countryData = await this.model.getCountryData(country.code);

        this.view.showCurrencies();
        this.view.renderPresentmentTotal(country, countryData, convertedPrice, targetCurrency);
        this.view.renderSettlementCurrency(this.model.baseCalculation);
        this.view.renderExchangeRate(exchangeRateResponse.base, exchangeRate, targetCurrency);
    }

    /**
     * Initializes the event listener for changing the currency.
     */
    initChangeCurrencyButton() {
        console.debug("Initializing change currency button event listener");
        $("#currencies").on("click", "a", async event => {
            event.preventDefault();
            console.info("Currency change detected", event);

            const $currency = $(event.currentTarget);
            const targetCurrency = $currency.data("currency-code");

            const address = this.model.baseCalculation.customerDetails.address;
            const rates = this.model.ratesResponse.rates;
            const rateId = $(`input[type="radio"]:checked`).data("address-idx");
            const selectedRate = rates[rateId];

            if (this.model.baseCalculation.currencyCode === targetCurrency) {
                console.debug("Selected currency matches base calculation currency");
                this.model.selectedCurrency = this.model.baseCalculation.currencyCode;
                this.view.renderSummary(this.model.baseCalculation);
            } else {
                console.debug("Selected currency differs from base calculation currency, fetching new calculation");
                this.model.selectedCurrency = targetCurrency;

                const country = await this.model.getCountryForClientIp();
                const exchangeRateResponse = await this.model.getExchangeRatesForClientCountry(country.currencyCode);

                const exchangeRate = exchangeRateResponse.result["rate"];
                const newCalculation = await this.model.getCalculation({
                    address,
                    shippingCost: selectedRate.totalCharge,
                    currencyCode: targetCurrency,
                    exchangeRate
                });

                this.view.renderSummary(newCalculation);
                this.view.renderExchangeRate(this.model.baseCalculation.currencyCode, exchangeRate, targetCurrency);
            }
        });
    }
}
