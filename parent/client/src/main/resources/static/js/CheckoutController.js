/**
 * Controller for managing the checkout process.
 */
class CheckoutController {

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
     * @returns {Promise<void>}
     */
    async init() {
        console.info("Initializing controller");
        try {
            this.view.showLoadTotal();
            this.view.showSummaryLoadNav();
            this.view.showAddressLoadBtn();
            this.view.showProductsSummaryNavPlaceholder();
            this.view.hideAddressButton();

            await this.initAddressElement();
            this.initEventListeners();
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            this.view.hideProductsSummaryNavPlaceholder();
            this.view.showProductSummaryNav();
        }
    }

    /**
     * Initializes all event listeners for the checkout process.
     * @returns {void}
     */
    initEventListeners() {
        console.debug("Initializing event listeners");
        this.addressElement.on("change", debounce(this.validateAddress.bind(this), 500));
        $("#address-btn, #payment-accordion-btn").on("click", this.handleContinueToPayment.bind(this));
        $("#address-accordion-btn").on("click", this.handleAddressAccordion.bind(this));
        $("#payment-btn, #summary-accordion-btn").on("click", this.handleShowSummary.bind(this));
        $("#summary").on("click", 'input[type="radio"]', this.handleChangeCourier.bind(this));
        $("#currencies").on("click", "a", this.handleChangeCurrency.bind(this));
    }

    /**
     * Validates the address input.
     * @async
     * @param {Object} event - The change event from the address element.
     * @returns {Promise<void>}
     */
    async validateAddress(event) {
        console.debug("Validating address", event);

        this.model.baseCalculation = undefined;

        this.view.hideSummaryNav();
        this.view.showSummaryLoadNav();
        this.view.hideTotal();
        this.view.showLoadTotal();
        this.view.hideAddressButton();

        if (!event.complete) return;

        const { address } = event.value;
        console.info("Address input complete, validating address", address);

        const addressLines = [address.line1, address.line2].filter(Boolean);
        const validateRequest = this.createAddressRequest(address, addressLines);
        try {
            const isValid = await this.model.validateAddress(validateRequest);
            if (isValid) {
                console.info("Address validated successfully", address);
                await this.handleValidAddress(address);
            } else {
                console.warn("Address validation failed", address);

                this.view.hideSummaryNav();
                this.view.hideAddressButton();
                this.view.showAddressLoadBtn();
            }
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }
    }

    /**
     * Creates an address validation request object.
     * @param {Object} address - The address object.
     * @param {Array<string>} addressLines - The address lines array.
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
     * @returns {Promise<void>}
     */
    async handleValidAddress(address) {
        console.info("Handling valid address", address);

        const easyShipAddress = this.createEasyShipAddress(address);
        try {
            const ratesResponse = await this.model.getShippingRates(easyShipAddress, 0);

            this.validateRatesResponse(ratesResponse, easyShipAddress);

            const shippingCost = ratesResponse.rates[0].totalCharge;
            const calculation = await this.model.getCalculation({ address, shippingCost });

            this.updateModelAfterCalculation(calculation, ratesResponse);
            await this.initStripeElements(calculation);

            this.model.selectedRateIdx = undefined;

            this.view.renderSummary(calculation);
            this.view.showSummaryView();
            this.view.showAddressButton();
            this.view.hideAddressLoadBtn();
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);

            this.view.hideSummaryNav();
            this.view.showAddressLoadBtn();
        } finally {
            this.view.showSummaryNav();
            this.view.hideLoadTotal();
            this.view.hideSummaryLoadNav();
        }
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
            state: address.state,
        };
    }

    /**
     * Validates the rates response and throws an error if necessary.
     * @param {Object} ratesResponse - The rates response object.
     * @param {Object} easyShipAddress - The easy ship address object.
     * @returns {void}
     */
    validateRatesResponse(ratesResponse, easyShipAddress) {
        console.debug("Validating rates response", ratesResponse);
        if (ratesResponse.length === 0) {
            throw new Error(`No shipping rate available for address: ${JSON.stringify(easyShipAddress)}`);
        }
        if (ratesResponse.rates[0].costRank !== 1.0) {
            throw new Error(`Cost rank of 1st rate should always be 1: ${JSON.stringify(ratesResponse)}`);
        }
    }

    /**
     * Updates the model after the calculation is fetched.
     * @param {Object} calculation - The calculation object.
     * @param {Object} ratesResponse - The rates response object.
     * @returns {void}
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
     * @returns {Promise<void>}
     */
    async initAddressElement() {
        console.info("Initializing address element");

        const calculation = await this.model.getBasicCalculation();
        await this.initStripeElements(calculation);
        await this.initAddressOptions();

        this.view.renderProductSummaryNav(calculation);
    }

    /**
     * Initializes the Stripe payment element.
     * @returns {void}
     */
    initPaymentElement() {
        console.info("Initializing payment element");
        const options = { layout: { type: "tabs", defaultCollapsed: false } };

        if (!this.paymentElement) {
            this.paymentElement = this.elements.create("payment", options);
            this.paymentElement.mount("#payment-element");
            this.paymentElement.on('change', event => {
                console.info("Payment element change event detected", event);
                this.paymentEvent = event;
                if(event.complete) {
                    this.view.showPaymentButton();
                    this.view.hidePaymentLoadBtn();
                } else {
                    this.view.hidePaymentButton();
                    this.view.showPaymentLoadBtn();
                }
            });
        }
    }

    /**
     * Initializes or updates the Stripe elements with the given calculation.
     * @param {Object} calculation - The calculation data.
     * @returns {void}
     */
    initStripeElements(calculation) {
        console.debug("Initializing Stripe elements", calculation);
        const options = {
            appearance: { theme: 'flat' },
            mode: 'payment',
            amount: calculation.amountTotal,
            currency: calculation.currencyCode.toLowerCase(),
        };
        this.elements = this.elements ? this.elements.update(options) : this.stripe.elements(options);
    }

    /**
     * Initializes the options for the Stripe address element.
     * @async
     * @returns {Promise<void>}
     */
    async initAddressOptions() {
        console.debug("Initializing address element options");

        const options = { mode: 'shipping' };
        try {
            const shippableCountries = await this.model.getShippableCountries();
            options.allowedCountries = shippableCountries.map(country => country.code);

            const addresses = await this.model.getCustomerAddresses();
            if (addresses?.length > 0) {
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
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }
    }

    /**
     * Handles the continue to payment action.
     * @async
     * @param {Object} event - The event object.
     * @returns {Promise<void>}
     */
    async handleContinueToPayment(event) {
        event.preventDefault();


        console.info("Handling continue to payment action");

        if (this.model?.baseCalculation?.customerDetails?.address) {
            if(!this.paymentEvent?.complete) {
                this.view.showPaymentLoadBtn();
                this.view.hidePaymentButton();
            } else {
                this.view.showPaymentButton()
            }

            this.view.hideAddressButton();
            this.view.hidePlaceOrderButton();
            this.view.hideCurrencies();

            try {
                if (!this.elements) {
                    const calculation = await this.model.getBasicCalculation();
                    await this.initStripeElements(calculation);
                }

                this.initPaymentElement();
                this.view.renderSummary(this.model.baseCalculation);
                this.view.renderProductSummaryNav(this.model.baseCalculation);

                new bootstrap.Collapse("#checkout-payment-accordion", { toggle: true }).show();
            } catch (error) {
                console.error(error);
                showErrorModal(error.response);
            }
        } else {
            console.warn("Address must be defined before payment step");
        }
    }

    /**
     * Handles the address accordion button click.
     * @param {Object} event - The event object.
     * @returns {void}
     */
    handleAddressAccordion(event) {
        event.preventDefault();
        console.info("Address accordion button clicked");

        this.view.showAddressButton();
        this.view.hidePaymentButton();
        this.view.hidePlaceOrderButton();
        this.view.hideCurrencies();

        this.view.renderSummary(this.model.baseCalculation);
        this.view.renderProductSummaryNav(this.model.baseCalculation);

        new bootstrap.Collapse("#checkout-address-accordion", { toggle: true }).show();
    }

    /**
     * Handles the show summary button click.
     * @async
     * @param {Object} event - The event object.
     * @returns {Promise<void>}
     */
    async handleShowSummary(event) {
        event.preventDefault();
        console.info("Show summary button clicked");

        if (this.model.baseCalculation?.customerDetails?.address && this.paymentEvent?.complete) {
            this.view.hideAddressButton();
            this.view.hidePaymentButton();
            this.view.showPlaceOrderButton();
            this.view.showCurrencies();

            new bootstrap.Collapse("#checkout-summary-accordion", { toggle: true }).show();

            this.view.renderSummaryData(this.model.baseCalculation, this.model.ratesResponse);
            this.view.checkRateRadio(this.model.selectedRateIdx);

            try {
                await this.loadCurrencyData();
            } catch (error) {
                console.error(error);
                showErrorModal(error.response);
            }
        } else {
            console.warn("Address and payment step must be complete before summary step");
        }
    }

    /**
     * Handles the change courier event.
     * @async
     * @param {Object} event - The event object.
     * @returns {Promise<void>}
     */
    async handleChangeCourier(event) {
        console.info("Courier change detected", event);

        this.view.hidePlaceOrderButton();
        this.view.hideTotal();
        this.view.hideProductSummaryNav();
        this.view.hideSummaryNav();

        this.view.showLoadPlaceOrderButton();
        this.view.showLoadTotal();
        this.view.showProductsSummaryNavPlaceholder();
        this.view.showSummaryLoadNav();

        this.model.selectedRateIdx = event.target.dataset.addressIdx;

        const rates = this.model.ratesResponse.rates;
        const selectedRate = rates[event.target.dataset.addressIdx];
        const currentAddress = this.model.baseCalculation.customerDetails.address;

        try {
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

            const isSameCurrency = this.model.selectedCurrency === this.model.baseCalculation.currencyCode;
            await this.initStripeElements(isSameCurrency ? this.model.baseCalculation : targetCalculation);

            await this.loadCurrencyData();

            if (!isSameCurrency) {
                this.view.renderSummary(targetCalculation);
                this.view.renderProductSummaryNav(targetCalculation);
            } else {
                this.view.renderSummary(this.model.baseCalculation);
                this.view.renderProductSummaryNav(this.model.baseCalculation);
            }
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            this.view.showPlaceOrderButton();
            this.view.showTotal();
            this.view.showProductSummaryNav();
            this.view.showSummaryNav();

            this.view.hideLoadPlaceOrderButton();
            this.view.hideLoadTotal();
            this.view.hideProductsSummaryNavPlaceholder();
            this.view.hideSummaryLoadNav();
        }
    }

    /**
     * Handles the change currency button click.
     * @async
     * @param {Object} event - The event object.
     * @returns {Promise<void>}
     */
    async handleChangeCurrency(event) {
        event.preventDefault();
        console.info("Currency change detected", event);

        this.view.hidePlaceOrderButton();
        this.view.hideTotal();
        this.view.hideProductSummaryNav();
        this.view.hideSummaryNav();

        this.view.showLoadPlaceOrderButton();
        this.view.showLoadTotal();
        this.view.showProductsSummaryNavPlaceholder();
        this.view.showSummaryLoadNav();


        const $currency = $(event.currentTarget);
        const targetCurrency = $currency.data("currency-code");

        if (!this.model.selectedCurrency && targetCurrency === this.model.baseCalculation.currencyCode) return;
        if (this.model.selectedCurrency === targetCurrency) return;

        const address = this.model.baseCalculation.customerDetails.address;
        const rates = this.model.ratesResponse.rates;
        const rateId = $(`input[type="radio"]:checked`).data("address-idx");
        const selectedRate = rates[rateId];

        const currentCurrency = this.model.selectedCurrency;
        this.model.selectedCurrency = targetCurrency;

        try {
            if (this.model.baseCalculation.currencyCode === targetCurrency) {
                this.view.renderSummary(this.model.baseCalculation);
                this.view.renderSummaryProducts(this.model.baseCalculation);
                this.view.renderProductSummaryNav(this.model.baseCalculation);
            } else {
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
                this.view.renderProductSummaryNav(newCalculation);
                this.view.renderSummaryProducts(newCalculation);
                this.view.renderExchangeRate(this.model.baseCalculation.currencyCode, exchangeRate, targetCurrency);
            }

            this.view.selectCurrency(currentCurrency, targetCurrency);
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            this.view.showPlaceOrderButton();
            this.view.showTotal();
            this.view.showProductSummaryNav();
            this.view.showSummaryNav();

            this.view.hideLoadPlaceOrderButton();
            this.view.hideLoadTotal();
            this.view.hideProductsSummaryNavPlaceholder();
            this.view.hideSummaryLoadNav();
        }
    }

    /**
     * Loads the currency data and updates the view accordingly.
     * @async
     * @returns {Promise<void>}
     */
    async loadCurrencyData() {
        console.debug("Loading currency data");
        try {
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
        } catch (error) {
            console.error(error);
            console.log(error.response);
            showErrorModal(error.response);
        }
    }
}
