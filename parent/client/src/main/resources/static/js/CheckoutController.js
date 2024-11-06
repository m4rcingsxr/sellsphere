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
            const checkout = await this.model.getCartTotal();

            this.elements = this.stripe.elements({
                mode: 'payment',
                amount: checkout.amount,
                currency: checkout.currency,
                customerSessionClientSecret,
                loader: 'always'
            });
        } catch (error) {
            console.error("Error occurred during initialization of stripe elements" + error);
            showErrorModal(error.response);
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
            console.error("Error occurred during initialization of address element" + error);
            showErrorModal(error.response);
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
        $("#summary").on("click", 'input[type="radio"]', this.handleChangeCourier.bind(this));
        $("#payment-form").on("submit", this.handleSubmit.bind(this));
    }

    async handleSubmit() {

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
            const response = await this.model.savePaymentIntent();
            const clientSecret = response.clientSecret;

            // Confirm the PaymentIntent using the details collected by the Payment Element
            const {error} = await this.stripe.confirmPayment({
                elements: this.elements,
                clientSecret,
                confirmParams: {
                    return_url: `${config.baseUrl}${MODULE_URL}checkout/return`,
                },
            });

            if (error) {
                handleError(error);
            }

        } catch (error) {
            console.error("Error during transaction saving", error);
            showErrorModal(error.response);
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

        try {
            this.view.enableAddressTabBtnSpinner();

            const {address} = event.value;
            const easyshipAddress = CheckoutUtil.mapStripeAddressToRateRequest(address, event.value.name, event.value.phone);

            this.model.rateResponse = await this.model.getShippingRates(easyshipAddress, 1);

            this.validateRates();

        } catch (error) {
            console.error("Error occurred during fetching shipping rates", error);
            showErrorModal(error.response);
        } finally {
            this.view.enableAddressTabBtn();
            this.view.disableAddressTabBtnSpinner();
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
            console.error("Error during rendering payment details", error);
            showErrorModal(error.response);
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
                showErrorModal(error.response);
            }
        } else {
            console.error("Handle previous steps..");
            showErrorModal({
                status : 400,
                message : "Please fill previous steps before continuing"
            })
        }
    }

    /**
     * Renders the summary details.
     */
    async renderSummaryDetails() {
        const rateResponse = this.model.rateResponse;
        const shippingCost = rateResponse.rates[0].totalCharge;

        const calculation = await this.model.getTaxCalculation({
            address: rateResponse.address,
            shippingCost,
        });

        this.view.renderSummaryProducts(calculation.cart, calculation.currencySymbol);

        this.view.enableSummaryTabBtn();
        this.view.disableSummaryBtnSpinner();

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
    }

    /**
     * Handles the change of courier event.
     *
     * @param {Event} event - The event object.
     */
    async handleChangeCourier(event) {
        this.prepareCheckoutDetailsUpdate();

        const rateIndex = Number(event.currentTarget.dataset.rateIndex);
        this.model.selectedRateIndex = rateIndex;

        try {
            await this.updateCheckoutDetails(rateIndex);
            this.completeCheckoutDetailsUpdate();
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }
    }

    /**
     * Prepares the UI for an update.
     */
    prepareCheckoutDetailsUpdate() {
        this.view.hideCheckoutDetails();
        this.view.showLoadCheckoutDetails();
        this.view.disableSummaryTabBtn();
        this.view.enableSummaryBtnSpinner();
    }

    /**
     * Completes the UI update.
     */
    completeCheckoutDetailsUpdate() {
        this.view.enableSummaryTabBtn();
        this.view.disableSummaryBtnSpinner();
        this.view.hideLoadCheckoutDetails();
        this.view.showCheckoutDetails();
    }

    /**
     * Updates the checkout details.
     *
     * @param {number} rateIndex - The index of the selected rate.
     * @param {boolean} isPresentmentCurrencyChange - Whether the currency change is for presentment.
     */
    async updateCheckoutDetails(rateIndex) {
        const rateResponse = this.model.rateResponse;
        const selectedRate = this.getSelectedRate(rateIndex, rateResponse)

        const shippingCost = selectedRate.totalCharge;

        try {
            const base = await this.calculateBaseTax(rateResponse, shippingCost);
            this.model.appliedCalculation = base;

            this.renderCheckoutDetails(base);
        } catch(error) {
            console.error(error);
            showErrorModal(error.response);
        }
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
     * Renders the checkout details using the selected calculation and updates the exchange rates.
     *
     * @param {Object} selectedCalculation - The selected tax calculation result.
     * @param {Object} base - The base tax calculation result.
     * @param {Object} target - The target tax calculation result.
     * @param {number} exchangeRate - The exchange rate.
     */
    renderCheckoutDetails(selectedCalculation) {
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

    }

}
