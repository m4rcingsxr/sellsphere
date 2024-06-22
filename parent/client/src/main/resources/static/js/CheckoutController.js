class CheckoutController {

    stripe = null;
    elements = null;
    paymentEvent = null;
    currencyUpdateInterval = null;

    constructor(model, view) {
        this.stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");
        this.model = model;
        this.view = view;

        this.init();
    }

    /**
     * Initializes the Stripe elements, payment, and address elements.
     */
    async init() {
        this.initPlaceOrderBtnTriggerFormSubmit();

        await this.initStripeElements();
        await this.initPaymentElement();
        await this.initAddressElement();

        this.initAddressElementListener();
        this.initPaymentElementListener();

        this.setupEventListeners();
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
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * Creates and mounts the Stripe payment element.
     */
    async initPaymentElement() {
        const options = CheckoutUtil.getPaymentElementOptions();
        this.elements.create('payment', options).mount("#payment-element");
    }

    /**
     * Initializes the Stripe address element by fetching customer addresses and shippable countries.
     */
    async initAddressElement() {
        try {
            this.view.disableAddressTabBtn();
            this.view.enableAddressTabBtnSpinner();

            const customerAddresses = await this.model.getCustomerAddresses();
            const allowedCountries = await this.model.getShippableCountries();

            this.model.customerAddresses = customerAddresses;

            const contacts = customerAddresses.length > 0
                ? customerAddresses.map(CheckoutUtil.mapAddressToStripeContact)
                : [];

            this.initStripeAddressElement(allowedCountries.map(country => country.code), contacts);
        } catch (error) {
            console.error("Error occurred during fetching addresses " + error);
            throw error;
        }
    }

    /**
     * Initializes the Stripe address element with allowed countries and contacts.
     */
    initStripeAddressElement(allowedCountries, contacts = []) {
        const options = CheckoutUtil.getAddressElementOptions(allowedCountries, contacts);
        this.elements.create('address', options).mount("#address-element");
    }

    /**
     * Sets up event listeners for the UI.
     */
    setupEventListeners() {
        $("#address-accordion-btn").on("click", this.handleShowAddressTab.bind(this));
        $("#payment-accordion-btn, #continue-to-payment-btn").on("click", this.showPaymentTab.bind(this));
        $("#summary-accordion-btn, #continue-to-summary-btn").on("click", this.showSummaryTab.bind(this));
        $("#currencies").on("click", "a", this.handleCurrencyChange.bind(this));
        $("#summary").on("click", 'input[type="radio"]', this.handleChangeCourier.bind(this));
        $("#payment-form").on("submit", this.handleSubmit.bind(this));
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

            // update payment intent - pass calculation id and create tax transaction
            // put transaction id in metadata of payment intent
            await this.model.savePaymentIntent();

            const {fetchError} = await this.elements.fetchUpdates();

            if (fetchError) {
                handleError(fetchError);
            }

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

    /**
     * Initializes the address element listener.
     */
    initAddressElementListener() {
        const addressElement = this.elements.getElement('address');
        addressElement.on("change", debounce(this.handleAddressChange.bind(this), 500));
    }

    /**
     * Initializes the payment element listener.
     */
    initPaymentElementListener() {
        const paymentElement = this.elements.getElement('payment');
        paymentElement.on("change", this.handlePaymentMethodChange.bind(this));
    }

    /**
     * Handles the address change event.
     *
     * @param {Object} event - The stripe event object.
     */
    async handleAddressChange(event) {
        if (!event.complete) {
            this.view.disableAddressTabBtn();
            this.model.rateResponse = null;
            return;
        }

        this.view.enableAddressTabBtnSpinner();

        const { address } = event.value;
        const easyshipAddress = CheckoutUtil.mapStripeAddressToRateRequest(address, event.value.name, event.value.phone);

        try {
            this.model.rateResponse = await this.model.getShippingRates(easyshipAddress, 1);

            this.validateRates();

            this.view.enableAddressTabBtn();
            this.view.disableAddressTabBtnSpinner();
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * Handles the payment method change event.
     *
     * @param {Object} event - The event object.
     */
    handlePaymentMethodChange(event) {
        if (event.complete) {
            this.view.enablePaymentTabBtnSpinner();

            setTimeout(() => {
                this.view.disablePaymentTabBtnSpinner();
                this.view.enablePaymentTabBtn();
            }, 1000);

            this.paymentEvent = event;
        } else {
            this.view.disablePaymentTabBtn();
        }
    }

    /**
     * Handles the display of the address tab.
     *
     * @param {Event} event - The event object.
     */
    handleShowAddressTab(event) {
        event.preventDefault();

        this.prepareForTabChange();
        this.view.hidePaymentTabBtn();
        this.view.hideSummaryTabBtn();

        this.view.showAddressTabBtn();
        this.view.showAddressTab();
    }

    /**
     * Displays the payment tab.
     *
     * @param {Event} event - The event object.
     */
    async showPaymentTab(event) {
        event.preventDefault();
        this.validateRates();


        this.view.hideAddressTabBtn();
        this.view.hideSummaryTabBtn();
        this.view.showPaymentTabBtn();

        this.prepareForTabChange();
        this.preparePaymentTab();

        try {
            await this.renderPaymentDetails();
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * Renders the payment details.
     */
    async renderPaymentDetails() {
        const rateResponse = this.model.rateResponse;
        const shippingCost = rateResponse.rates[0].totalCharge;

        const response = await this.model.getTaxCalculation({
            address: rateResponse.address,
            shippingCost,
        });

        // store applied calculation to update payment intent on placing order
        this.model.appliedCalculation = response;

        this.view.renderCheckoutDetails(
            response.displayAmount,
            response.displaySubtotal,
            response.displayTax,
            response.displayTotalTax,
            response.shippingCost.displayAmount,
            response.shippingCost.displayTax,
            response.currencySymbol,
            response.cart
        );

        this.view.hideLoadCheckoutDetails();
        this.view.showCheckoutDetails();
        this.view.showPaymentMethodTab();

        this.view.disablePaymentTabBtnSpinner();
        this.view.enablePaymentTabBtn();
    }

    /**
     * Prepares the payment tab.
     */
    preparePaymentTab() {
        this.view.showLoadCheckoutDetails();
        this.view.disablePaymentTabBtn();
        this.view.enablePaymentTabBtnSpinner();
    }

    /**
     * Validates the rates in the model.
     *
     * @throws {Error} If there are no available rates.
     */
    validateRates() {
        if (this.model.rateResponse == null || this.model.rateResponse.rates.length === 0) {
            throw new Error("Cannot process without available shipping rates. Please provide valid address.");
        }
    }

    /**
     * Prepares the UI for tab change.
     */
    prepareForTabChange() {
        this.model.clearSummaryData();
        this.view.selectSettlementCurrency();
        this.view.hideCurrencies();
        this.view.hideCheckoutDetails();
    }

    /**
     * Displays the summary tab.
     *
     * @param {Event} event - The event object.
     */
    async showSummaryTab(event) {
        event.preventDefault();

        if (this?.paymentEvent.complete && this.model?.rateResponse?.rates.length > 0) {
            this.prepareSummaryTab();

            try {
                await this.renderSummaryDetails();
            } catch (error) {
                console.error(error);
            }
        } else {
            console.error("Handle previous steps..");
        }
    }

    /**
     * Renders the summary details.
     */
    async renderSummaryDetails() {
        const targetCountry = await this.model.getCountryForClientIp();
        const targetCurrency = targetCountry.currencyCode;
        this.model.targetCurrency = targetCurrency;

        const rateResponse = this.model.rateResponse;
        const shippingCost = rateResponse.rates[0].totalCharge;

        const baseCalculation = await this.model.getTaxCalculation({
            address: rateResponse.address,
            shippingCost,
        });

        const basePrice = baseCalculation.displayAmount;
        const baseCurrencyCode = baseCalculation.currencyCode;

        this.model.baseCurrency = baseCurrencyCode;
        this.model.selectedCurrency = baseCurrencyCode;

        const exchangeRateResponse = await this.model.getExchangeRateWithPrice(
            basePrice,
            baseCurrencyCode,
            targetCurrency
        );

        this.model.exchangeRateResponse = exchangeRateResponse;
        const exchangeRate = exchangeRateResponse.result["rate"];

        const targetCalculation = await this.model.getTaxCalculation({
            address: rateResponse.address,
            shippingCost,
            currencyCode: targetCurrency,
            exchangeRate
        });

        const targetCountryDetails = await this.model.getCountryDetails(targetCountry.code);

        this.view.renderPresentmentTotal(
            targetCountryDetails,
            targetCalculation.targetCurrency,
            targetCalculation.currencySymbol,
            targetCalculation.displayAmount
        );

        this.view.renderSettlementTotal(
            baseCalculation.displayAmount,
            baseCalculation.currencySymbol
        );

        this.view.renderSummaryProducts(baseCalculation.cart, baseCalculation.currencySymbol);
        this.view.setExchangeRate(exchangeRate, baseCalculation.currencyCode, targetCalculation.currencyCode);

        this.view.enableSummaryTabBtn();
        this.view.disableSummaryBtnSpinner();
        this.view.disableCurrencyPlaceholders();
        this.view.showCurrencies();

        this.view.renderShippingRates(this.model.rateResponse.rates);
        this.view.showSummaryTab();
    }

    /**
     * Prepares the summary tab.
     */
    prepareSummaryTab() {
        this.view.hidePaymentTabBtn();
        this.view.hideAddressTabBtn();
        this.view.showSummaryTabBtn();
        this.view.disableSummaryTabBtn();
        this.view.enableSummaryBtnSpinner();
        this.view.enableCurrencyPlaceholders();
        this.startCountdown(20 * 60 * 1000);
        this.startCurrencyUpdateListener();
    }

    /**
     * Handles the currency change event.
     *
     * @param {Event} event - The event object.
     */
    async handleCurrencyChange(event) {
        event.preventDefault();
        const clickedElement = event.currentTarget;
        const selectedCurrency = clickedElement.id === "presentment-currency" ? this.model.targetCurrency : this.model.baseCurrency;

        // Check if the clicked currency is already selected
        if (this.model.selectedCurrency === selectedCurrency) return;

        // Update currency-checked classes
        document.querySelectorAll('.currency').forEach(el => el.classList.remove('currency-checked'));
        clickedElement.classList.add('currency-checked');

        // Ensure the selected rate index is preserved
        const rateIndex = this.model.selectedRateIndex;

        this.prepareCheckoutDetailsUpdate();

        try {
            await this.updateCheckoutDetails(rateIndex, true); // Pass the rate index through the update process
            this.completeCheckoutDetailsUpdate();
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * Handles the change of courier event.
     *
     * @param {Event} event - The event object.
     */
    async handleChangeCourier(event) {
        this.prepareCheckoutDetailsUpdate();

        const rateIndex = Number(event.currentTarget.dataset.rateIndex);
        this.model.selectedRateIndex = rateIndex; // Ensure the selected rate index is updated in the model

        try {
            await this.updateCheckoutDetails(rateIndex);
            this.completeCheckoutDetailsUpdate();
        } catch (error) {
            console.error(error);
        }
    }

    /**
     * Starts a listener that checks if 1 hour has passed since the last exchange rate update
     * and invokes loadCurrencyData if necessary.
     */
    startCurrencyUpdateListener() {
        if (this.currencyUpdateInterval) {
            clearInterval(this.currencyUpdateInterval);
        }

        this.currencyUpdateInterval = setInterval(async () => {
            if (this.model.exchangeRateResponse && CheckoutUtil.isExchangeRateExpired(this.model.exchangeRateResponse)) {
                this.startCountdown(20 * 60 * 1000);
                this.prepareCheckoutDetailsUpdate();

                try {
                    await this.updateCheckoutDetails(this.model.selectedRateIndex);
                    this.completeCheckoutDetailsUpdate();
                } catch (error) {
                    console.error(error);
                }
            } else {
                console.error("Address and payment step must be complete before summary step");
            }
        }, 60000 * 20); // Check every 20 minutes
    }

    /**
     * Prepares the UI for an update.
     */
    prepareCheckoutDetailsUpdate() {
        this.view.hideCheckoutDetails();
        this.view.showLoadCheckoutDetails();
        this.view.disableSummaryTabBtn();
        this.view.enableSummaryBtnSpinner();
        this.view.hideCurrencies();
        this.view.enableCurrencyPlaceholders();
    }

    /**
     * Completes the UI update.
     */
    completeCheckoutDetailsUpdate() {
        this.view.enableSummaryTabBtn();
        this.view.disableSummaryBtnSpinner();
        this.view.hideLoadCheckoutDetails();
        this.view.showCheckoutDetails();
        this.view.showCurrencies();
        this.view.disableCurrencyPlaceholders();
    }

    /**
     * Updates the checkout details.
     *
     * @param {number} rateIndex - The index of the selected rate.
     * @param {boolean} isPresentmentCurrencyChange - Whether the currency change is for presentment.
     */
    async updateCheckoutDetails(rateIndex, isPresentmentCurrencyChange = false) {
        const rateResponse = this.model.rateResponse;
        const selectedRate = this.getSelectedRate(rateIndex, rateResponse);

        if (isPresentmentCurrencyChange) {
            this.model.selectedCurrency = this.model.selectedCurrency === this.model.targetCurrency
                ? this.model.baseCurrency
                : this.model.targetCurrency;
        }

        const { targetCurrency, selectedCurrency } = this.model;
        const shippingCost = selectedRate.totalCharge;
        const exchangeRate = this.getExchangeRate();

        const base = await this.calculateBaseTax(rateResponse, shippingCost);
        const target = await this.calculateTargetTax(rateResponse, shippingCost, targetCurrency, exchangeRate);

        const selectedCalculation = this.getSelectedCalculation(target, base, selectedCurrency);
        this.model.appliedCalculation = selectedCalculation;

        this.renderCheckoutDetails(selectedCalculation, base, target, exchangeRate);
    }

    /**
     * Retrieves the selected rate from the rate response.
     *
     * @param {number} rateIndex - The index of the selected rate.
     * @param {Object} rateResponse - The rate response object.
     * @returns {Object} The selected rate.
     */
    getSelectedRate(rateIndex, rateResponse) {
        const selectedRate = rateResponse.rates[rateIndex];
        if (!selectedRate?.totalCharge) {
            throw new Error("Rate for index " + rateIndex + " not found");
        }
        return selectedRate;
    }

    /**
     * Retrieves the exchange rate from the model.
     *
     * @returns {number} The exchange rate.
     */
    getExchangeRate() {
        return this.model.exchangeRateResponse.result["rate"];
    }

    /**
     * Calculates the base tax for the given shipping cost and rate response.
     *
     * @param {Object} rateResponse - The rate response object.
     * @param {number} shippingCost - The shipping cost.
     * @returns {Object} The base tax calculation result.
     */
    async calculateBaseTax(rateResponse, shippingCost) {
        return await this.model.getTaxCalculation({
            address: rateResponse.address,
            shippingCost,
        });
    }

    /**
     * Calculates the target tax for the given shipping cost, target currency, and exchange rate.
     *
     * @param {Object} rateResponse - The rate response object.
     * @param {number} shippingCost - The shipping cost.
     * @param {string} targetCurrency - The target currency code.
     * @param {number} exchangeRate - The exchange rate.
     * @returns {Object} The target tax calculation result.
     */
    async calculateTargetTax(rateResponse, shippingCost, targetCurrency, exchangeRate) {
        return await this.model.getTaxCalculation({
            address: rateResponse.address,
            shippingCost,
            currencyCode: targetCurrency,
            exchangeRate
        });
    }

    /**
     * Determines the selected calculation based on the target and base calculations.
     *
     * @param {Object} target - The target tax calculation result.
     * @param {Object} base - The base tax calculation result.
     * @param {string} selectedCurrency - The selected currency code.
     * @returns {Object} The selected calculation result.
     */
    getSelectedCalculation(target, base, selectedCurrency) {
        return (target.currencyCode === selectedCurrency) ? target : base;
    }

    /**
     * Renders the checkout details using the selected calculation and updates the exchange rates.
     *
     * @param {Object} selectedCalculation - The selected tax calculation result.
     * @param {Object} base - The base tax calculation result.
     * @param {Object} target - The target tax calculation result.
     * @param {number} exchangeRate - The exchange rate.
     */
    renderCheckoutDetails(selectedCalculation, base, target, exchangeRate) {
        this.view.renderCheckoutDetails(
            selectedCalculation.displayAmount,
            selectedCalculation.displaySubtotal,
            selectedCalculation.displayTax,
            selectedCalculation.displayTotalTax,
            selectedCalculation.shippingCost.displayAmount,
            selectedCalculation.shippingCost.displayTax,
            selectedCalculation.currencySymbol,
            selectedCalculation.cart
        );

        this.view.setExchangeRate(exchangeRate, base.currencyCode, target.currencyCode);
        this.view.updatePresentmentPrice(target.displayAmount, target.currencySymbol);
        this.view.updateSettlementPrice(base.displayAmount, base.currencySymbol);
    }

    /**
     * Starts a countdown timer for the given duration.
     *
     * @param {number} duration - The countdown duration in milliseconds.
     */
    startCountdown(duration) {
        if (this.model.countdown) {
            clearInterval(this.model.countdown);
        }

        const countdownElement = document.getElementById('countdown');
        let remainingTime = duration;

        const updateCountdown = () => {
            if (remainingTime <= 0) {
                clearInterval(this.model.countdown);
                countdownElement.textContent = '00:00';
                return;
            }

            remainingTime -= 1000;

            const minutes = Math.floor((remainingTime / (1000 * 60)) % 60);
            const seconds = Math.floor((remainingTime / 1000) % 60);

            countdownElement.textContent =
                `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        };

        this.model.countdown = setInterval(updateCountdown, 1000);
    }
}
