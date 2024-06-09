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
        this.targetCurrency = null;

        this.init();
        this.startCurrencyUpdateListener();
        this.initPlaceOrderBtnTriggerFormSubmit();
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
        $("#payment-form").on("submit", this.handleSubmit.bind(this));
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

        const {address} = event.value;
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
            const calculation = await this.model.getCalculation({address, shippingCost});

            this.updateModelAfterCalculation(calculation, ratesResponse);


            const response = await this.model.savePaymentIntent(calculation.amountTotal, calculation.currencyCode);
            await this.initStripeElementsWithPaymentIntent(response.clientSecret);

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
    createEasyShipAddress(address, currencyCode = null) {
        console.debug("Creating easy ship address object", address);
        return {
            city: address.city,
            countryCode: address.country,
            addressLine1: address.line1,
            addressLine2: address.line2,
            postalCode: address.postal_code,
            state: address.state,
            currencyCode: currencyCode
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
        const options = {layout: {type: "tabs", defaultCollapsed: false}};

        if (!this.paymentElement) {
            this.paymentElement = this.elements.create("payment", options);
            this.paymentElement.mount("#payment-element");
            this.paymentElement.on('change', event => {
                console.info("Payment element change event detected", event);
                this.paymentEvent = event;
                if (event.complete) {
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
     * @returns {void}
     */
    async initStripeElementsWithPaymentIntent(clientSecret) {

        try {
            console.debug("Initializing Stripe elements with client secret");
            const options = {
                appearance: {theme: 'flat'},
                clientSecret: clientSecret
            };

            this.elements = this.stripe.elements(options);
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }
    }

    async initStripeElements(calculation) {
        console.debug("Initializing Stripe elements with currency and cart subtotal");
        const options = {};
        options.mode = 'payment';
        options.amount = calculation.amountTotal,
        options.currency = calculation.currencyCode.toLowerCase();

        this.elements = this.stripe.elements(options);
    }

    /**
     * Initializes the options for the Stripe address element.
     * @async
     * @returns {Promise<void>}
     */
    async initAddressOptions() {
        console.debug("Initializing address element options");

        const options = {mode: 'shipping'};
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

        this.model.exchangeRateResponse = undefined;

        console.info("Handling continue to payment action");

        if (this.model?.baseCalculation?.customerDetails?.address) {
            if (!this.paymentEvent?.complete) {
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
                    await this.model.savePaymentIntent(calculation.amountTotal, calculation.currencyCode);
                }

                this.initPaymentElement();
                this.view.renderSummary(this.model.baseCalculation);
                this.view.renderProductSummaryNav(this.model.baseCalculation);

                new bootstrap.Collapse("#checkout-payment-accordion", {toggle: true}).show();
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

        this.model.exchangeRateResponse = undefined;

        this.view.showAddressButton();
        this.view.hidePaymentButton();
        this.view.hidePlaceOrderButton();
        this.view.hideCurrencies();

        this.view.renderSummary(this.model.baseCalculation);
        this.view.renderProductSummaryNav(this.model.baseCalculation);

        new bootstrap.Collapse("#checkout-address-accordion", {toggle: true}).show();
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

            try {
                this.view.showLoadCurrency();
                await this.loadExchangeRate();

                const rates = this.model.ratesResponse.rates;
                const rateIdx = this.model.selectedRateIdx ? this.model.selectedRateIdx : 0;
                const selectedRate = rates[rateIdx];
                const currentAddress = this.model.baseCalculation.customerDetails.address;


                const targetCalculation = await this.model.getCalculation({
                    address: currentAddress,
                    shippingCost: selectedRate.totalCharge,
                    currencyCode: this.model.targetCurrency,
                    exchangeRate: this.model.exchangeRateResponse.result["rate"] * (1.02)
                });

                await this.loadCurrencyView(targetCalculation);
            } catch (error) {
                console.error(error);
                showErrorModal(error.response);
            } finally {
                this.view.hideLoadCurrency();
            }

            this.view.renderSummaryData(this.model.baseCalculation, this.model.ratesResponse);
            this.view.checkRateRadio(this.model.selectedRateIdx);


            this.view.hideAddressButton();
            this.view.hidePaymentButton();
            this.view.showCurrencies();
            this.view.showPlaceOrderButton();
            new bootstrap.Collapse("#checkout-summary-accordion", {toggle: true}).show();
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
            if (!this.model.exchangeRateResponse) throw new Error("Invalid state, exhcange rate must be defined to process this request");


            const targetCalculation = await this.model.getCalculation({
                address: currentAddress,
                shippingCost: selectedRate.totalCharge,
                currencyCode: this.model.targetCurrency,
                exchangeRate: this.model.exchangeRateResponse.result["rate"]
            });

            this.model.baseCalculation = await this.model.getCalculation({
                address: currentAddress,
                shippingCost: selectedRate.totalCharge,
                currencyCode: this.model.baseCalculation.currencyCode
            });

            const isSameCurrency = this.model.selectedCurrency === this.model.baseCalculation.currencyCode;

            const calc = isSameCurrency ? this.model.baseCalculation : targetCalculation;
            await this.model.savePaymentIntent(calc.amountTotal, calc.currencyCode);

            await this.loadCurrencyView(targetCalculation);

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

                await this.model.savePaymentIntent(this.model.baseCalculation.amountTotal, this.model.baseCalculation.currencyCode);
            } else {

                const exchangeRateResponse = this.model.exchangeRateResponse;

                if (!exchangeRateResponse) throw new Error("Invalid state, exhcange rate must be defined to process this request");

                const newCalculation = await this.model.getCalculation({
                    address,
                    shippingCost: selectedRate.totalCharge,
                    currencyCode: targetCurrency,
                    exchangeRate: exchangeRateResponse.result["rate"]
                });

                await this.model.savePaymentIntent(newCalculation.amountTotal, newCalculation.currencyCode);


                this.view.renderSummary(newCalculation);
                this.view.renderProductSummaryNav(newCalculation);
                this.view.renderSummaryProducts(newCalculation);
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
     * Loads the currency data and updates the view accordingly. Only on new address or when exchange rate expire
     * @async
     * @returns {Promise<void>}
     */
    async loadExchangeRate() {
        console.debug("Loading currency data");

        try {
            const country = await this.model.getCountryForClientIp();

            const exchangeRateResponse = await this.model.getExchangeRatesForClientCountry(country.currencyCode);
            exchangeRateResponse.updated = new Date();

            this.model.exchangeRateResponse = exchangeRateResponse;
            this.model.targetCurrency = country.currencyCode;

            this.startCountdown(20 * 60 * 1000); // 20 min
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }

    }

    async loadCurrencyView(targetCalculation) {
        const country = await this.model.getCountryForClientIp();
        const countryData = await this.model.getCountryData(country.code);

        this.view.renderSettlementCurrency(this.model.baseCalculation);

        this.view.renderPresentmentTotal(countryData, country.currencyCode, country.currencySymbol, (targetCalculation.amountTotal / targetCalculation.unitAmount));

        this.view.renderExchangeRate(this.model.baseCalculation.currencyCode, this.model.exchangeRateResponse.result["rate"], country.currencyCode);
        this.view.hideLoadCurrency();

        this.view.showCurrencies();
    }

    /**
     * Starts a listener that checks if 1 hour has passed since the last exchange rate update
     * and invokes loadCurrencyData if necessary.
     */
    startCurrencyUpdateListener() {
        setInterval(async () => {
            console.debug("Validating exchange rate...");
            if (this.model.exchangeRateResponse && this.exchangeRateExpired(this.model.exchangeRateResponse)) {
                console.info("One hour has passed since the last exchange rate update. Loading new currency data...");

                if (this.model.baseCalculation?.customerDetails?.address && this.paymentEvent?.complete) {

                    try {
                        this.view.hidePlaceOrderButton();
                        this.view.hideTotal();
                        this.view.hideProductSummaryNav();
                        this.view.hideSummaryNav();

                        this.view.showLoadCurrency();
                        this.view.hideCurrencies();

                        this.view.showLoadPlaceOrderButton();
                        this.view.showLoadTotal();
                        this.view.showProductsSummaryNavPlaceholder();
                        this.view.showSummaryLoadNav();


                        await this.loadExchangeRate();

                        const rates = this.model.ratesResponse.rates;
                        const rateIdx = this.model.selectedRateIdx ? this.model.selectedRateIdx : 0;
                        const selectedRate = rates[rateIdx];
                        const currentAddress = this.model.baseCalculation.customerDetails.address;


                        const targetCalculation = await this.model.getCalculation({
                            address: currentAddress,
                            shippingCost: selectedRate.totalCharge,
                            currencyCode: this.model.targetCurrency,
                            exchangeRate: this.model.exchangeRateResponse.result["rate"]
                        });

                        this.view.renderSummary(this.model.baseCalculation);
                        this.view.renderProductSummaryNav(this.model.baseCalculation);
                        this.view.renderSummaryProducts(this.model.baseCalculation);
                        await this.loadCurrencyView(targetCalculation);
                    } catch (error) {
                        console.error(error);
                        showErrorModal(error.response);
                    } finally {
                        if (this.model.selectedCurrency !== this.model.baseCalculation.currencyCode) {
                            this.view.selectCurrency(this.model.selectedCurrency, this.model.baseCalculation.currencyCode);
                        }

                        this.view.hideLoadPlaceOrderButton();
                        this.view.hideLoadTotal();
                        this.view.hideProductsSummaryNavPlaceholder();
                        this.view.hideSummaryLoadNav();

                        this.view.hideLoadCurrency();
                        this.view.showSummaryNav();
                        this.view.showTotal();
                        this.view.hideSummaryLoadNav();
                        this.view.showCurrencies();

                        this.view.showPlaceOrderButton();
                        this.view.showTotal();
                        this.view.showProductSummaryNav();
                        this.view.showSummaryNav();

                        this.view.hideAddressButton();
                        this.view.hidePaymentButton();
                    }


                } else {
                    console.warn("Address and payment step must be complete before summary step");
                }
            }
        }, 60000); // Check every minute
    }

    /**
     * Checks if 20 minutes have passed since the last update of the exchange rate response.
     * @param {Object} exchangeRateResponse - The exchange rate response object.
     * @returns {boolean} - True if 20 minutes have passed, false otherwise.
     */
    exchangeRateExpired(exchangeRateResponse) {
        const now = new Date();
        const updated = new Date(exchangeRateResponse.updated);

        const differenceInMinutes = (now - updated) / (1000 * 60);

        return differenceInMinutes >= 20;
    }

    startCountdown(duration) {
        if (this.model.countdown) {
            clearInterval(this.model.countdown);
        }

        const countdownElement = document.getElementById('countdown');
        let remainingTime = duration;

        function updateCountdown() {
            if (remainingTime <= 0) {
                clearInterval(interval);
                countdownElement.textContent = '00:00';
                return;
            }

            remainingTime -= 1000;

            const minutes = Math.floor((remainingTime / (1000 * 60)) % 60);
            const seconds = Math.floor((remainingTime / 1000) % 60);

            countdownElement.textContent =
                `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        }

        // Update the countdown every second (1000 milliseconds)
        this.model.countdown = setInterval(updateCountdown, 1000);
    }

    initPlaceOrderBtnTriggerFormSubmit() {
        const placeOrderBtn = document.getElementById("place-order-btn");
        if (placeOrderBtn) {
            placeOrderBtn.addEventListener("click", (event) => {
                event.preventDefault();
                const form = document.getElementById("payment-form");
                if (form) {
                    form.dispatchEvent(
                        new Event("submit", {cancelable: true, bubbles: true})
                    );
                } else {
                    console.error("Payment form not found.");
                }
            });
        } else {
            console.error("Place order button not found.");
        }
    }

    async handleSubmit(event) {

        const handleError = (error) => {
            const messageContainer = document.querySelector("#error-message");
            if (messageContainer) {
                messageContainer.textContent = error.message;
            }
            submitBtn.disabled = false;
        };

        const submitBtn = document.getElementById("place-order-btn");

        // Prevent multiple form submissions
        if (submitBtn.disabled) {
            return;
        }

        // Disable form submission while loading
        submitBtn.disabled = true;


        const {error: submitError} = await this.elements.submit();
        if (submitError) {
            handleError(submitError);
            return;
        }

        try {
            // Create the PaymentIntent and obtain clientSecret
            const rates = this.model.ratesResponse.rates;
            const rateIdx = this.model.selectedRateIdx ? this.model.selectedRateIdx : 0;
            const selectedRate = rates[rateIdx];
            const currentAddress = this.model.baseCalculation.customerDetails.address;


            const targetCalculation = await this.model.getCalculation({
                address: currentAddress,
                shippingCost: selectedRate.totalCharge,
                currencyCode: this.model.targetCurrency,
                exchangeRate: this.model.exchangeRateResponse.result["rate"] * (1.02)
            });


            const response = await this.model.savePaymentIntent(targetCalculation.amountTotal, targetCalculation.currencyCode);

            // Confirm the PaymentIntent using the details collected by the Payment Element
            const {error} = await this.stripe.confirmPayment({
                elements: this.elements,
                confirmParams: {
                    return_url: `http://localhost:8081${MODULE_URL}checkout/return`,
                },
            });

            if (error) {
                handleError(error);
            }
        } catch (error) {
            handleError(error);
        }

    }


}
