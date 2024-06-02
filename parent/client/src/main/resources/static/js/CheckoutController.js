// todo : select as 1st primary address always - if any address exist


class CheckoutController {
    constructor(model, view) {
        this.stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");
        this.model = model;
        this.view = view;

        // Bind methods to ensure correct 'this' context
        this.initializeElements = this.initializeElements.bind(this);
        this._initializeStripeElements = this._initializeStripeElements.bind(this);
        this._initializeAddressElement = this._initializeAddressElement.bind(this);
        this._initializePaymentElement = this._initializePaymentElement.bind(this);
    }

    initializeElements() {
        const options = {
            mode: 'shipping',
        };

        // Fetch calculations (if needed) and countries, then addresses, then initialize elements
        this.model.fetchShippableCountries()
            .then(countries => {
                options.allowedCountries = countries.map(country => country.code);
                return this.model.fetchShippableAddresses();
            })
            .then(addresses => {
                this.model.addresses = addresses;

                options.contacts = addresses.map(address => ({
                    name: address.fullName,
                    address: {
                        line1: address.line1,
                        line2: address.line2,
                        city: address.city,
                        state: address.state,
                        postal_code: address.postalCode,
                        country: address.countryAlpha2,
                    },
                }));

                if (addresses && addresses.length > 0) {
                    // total, tax, shipping, address
                    // pass copy of the object
                    const stripeAddress = Object.assign({}, addresses[0]);
                    stripeAddress.country = addresses[0].countryAlpha2;
                    delete stripeAddress.countryAlpha2;


                    return this.model.fetchShippingRates(addresses[0])
                        .then(ratesResponse => {
                            const rates = ratesResponse.rates;
                            this.model.ratesResponse = ratesResponse;

                            const bestRate = rates.reduce((lowest, rate) => {
                                return (rate.costRank < lowest.costRank) ? rate : lowest;
                            }, rates[0]);

                            // Get the totalCharge from the best rate
                            const shippingCost = bestRate.totalCharge;

                            // Now use the totalCharge (shippingCost) to fetch calculations
                            return this.model.fetchCalculations(stripeAddress, shippingCost);
                        });
                } else {
                    // total
                    return this.model.fetchCalculations(null);
                }
            })
            .then(this._initializeStripeElements)
            .then(() => this._initializeAddressElement(options))
            .then(this._initializePaymentElement)
            .catch(error => {
                console.error(error);
                showErrorModal(error.response);
            });
    }

    _initializeStripeElements(calcResponse) {
        const appearance = {
            theme: 'flat'
        };

        return new Promise((resolve, reject) => {
            try {
                this.elements = this.stripe.elements({
                    appearance,
                    mode: 'payment',
                    amount: calcResponse.amountTotal,
                    currency: calcResponse.currencyCode.toLowerCase(),
                });

                this.model.calculationResponse = calcResponse;
                this.view.renderSummary(calcResponse);

                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }

    _initializeAddressElement(options) {
        return new Promise((resolve, reject) => {
            try {
                this.addressElement = this.elements.create('address', options);
                this.addressElement.mount('#address-element');
                this._initializeChangeAddressEventListener();
                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }

    _initializePaymentElement() {
        return new Promise((resolve, reject) => {
            try {
                const options = {
                    layout: {
                        type: 'tabs',
                        defaultCollapsed: false,
                    }
                };

                this.paymentElement = this.elements.create('payment', options);
                this.paymentElement.mount('#payment-element');
                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }

    _initializeChangeAddressEventListener() {
        this.addressElement.on('change', async event => {
            if (event.complete) {
                const address = event.value.address;

                address.postalCode = address.postal_code;
                address.countryAlpha2 = address.country;

                console.log(address);

                const ratesResponse = await this.model.fetchShippingRates(address);
                const rates = ratesResponse.rates;

                this.model.ratesResponse = ratesResponse;

                const bestRate = rates.reduce((lowest, rate) => {
                    return (rate.costRank < lowest.costRank) ? rate : lowest;
                }, rates[0]);

                // Get the totalCharge from the best rate
                const shippingCost = bestRate.totalCharge;


                const calcResponse = await this.model.fetchCalculations(address, shippingCost);
                this.model.calculationResponse = calcResponse;
                this.view.renderSummary(calcResponse);

                this.elements.update({
                    mode: 'payment',
                    amount: calcResponse.amountTotal,
                    currency: calcResponse.currencyCode.toLowerCase(),
                });

            }
        })
    }

    initSubmitPaymentEventListener() {
        const form = document.getElementById('payment-form');
        const submitBtn = document.getElementById('submit');

        const handleError = (error) => {
            const messageContainer = document.querySelector('#error-message');
            messageContainer.textContent = error.message;
            submitBtn.disabled = false;
        }

        form.addEventListener('submit', async (event) => {
            // We don't want to let default form submission happen here,
            // which would refresh the page.
            event.preventDefault();

            // Prevent multiple form submissions
            if (submitBtn.disabled) {
                return;
            }

            // Disable form submission while loading
            submitBtn.disabled = true;

            // Trigger form validation and wallet collection
            const {error: submitError} = await this.elements.submit();
            if (submitError) {
                handleError(submitError);
                return;
            }

            // Create the PaymentIntent and obtain clientSecret
            const res = await this.model.createPaymentIntent();

            const clientSecret = res.clientSecret;

            // Confirm the PaymentIntent using the details collected by the Payment Element
            const {error} = await this.stripe.confirmPayment({
                elements: this.elements,
                clientSecret: clientSecret,
                confirmParams: {
                    return_url: `http://localhost:8081${MODULE_URL}checkout/return`,
                },
            });

            if (error) {
                // This point is only reached if there's an immediate error when
                // confirming the payment. Show the error to your customer (for example, payment details incomplete)
                handleError(error);
            } else {
                // Your customer is redirected to your `return_url`. For some payment
                // methods like iDEAL, your customer is redirected to an intermediate
                // site first to authorize the payment, then redirected to the `return_url`.
            }
        });
    }

    initPaymentBtnAccordionListener() {
        $("#payment-btn").on("click", event => {
            event.preventDefault();

            if (this.model.calculationResponse?.customerDetails?.address) {
                const paymentAccordion = new bootstrap.Collapse('#checkout-payment-accordion', {
                    toggle: true
                });
                paymentAccordion.show();
            } else {
                console.warn("specify address first!");
            }
        });
    }


    initSummaryBtnListener() {
        $("#summary-button").on("click", event => {
            event.preventDefault();

            if (this.model.calculationResponse?.customerDetails?.address) {
                const summaryAccordion = new bootstrap.Collapse('#checkout-summary-accordion', {
                    toggle: true
                });

                this._loadSummaryAccordion();
                summaryAccordion.show();
            } else {
                console.warn("specify address first!");
            }
        });
    }

    _loadSummaryAccordion() {
        const $summary = $("#summary");
        $summary.empty();

        const cart = this.model.calculationResponse.cart;
        console.log(cart);

        let products = "";
        cart.forEach((item) => {
            products += `
                <div class="row">
                    <div class="col-sm-4">
                        <img src="${item.product.mainImagePath}" alt="${item.product.name}" class="img-fluid"/>
                    </div>
                    <div class="col-sm-8">
                        <h5>${item.product.name}</h5>
                        <p>Quantity: ${item.quantity}</p>
                        <span>${item.product.discountPrice}</span>
                    </div>
                </div>
            `;
        });

        let rates = "";
        this.model.ratesResponse.rates.forEach((rate, index) => {
            console.log(index, rate);
            rates += `
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault${index}" ${index === 0 ? 'checked' : ''} data-address-idx="${index}">
                  <label class="form-check-label" for="flexRadioDefault${index}">
                    <div class="row">
                        <div class="col-sm-4">
                            <img src="${rate.courierLogoUrl}" alt="courier logo" class="img-fluid"/>
                        </div>
                        <div class="col-sm-8">
                            <div class="d-flex flex-column">
                                <span class="text-success fs-7 fw-bolder">Delivery days ${rate.minDeliveryTime} - ${rate.maxDeliveryTime}</span>
                                <span>${rate.courierName}</span>
                                <span class="fs-7">(${rate.currency}) ${rate.totalCharge} - <span class="fw-lighter">Delivery</span></span>
                            </div>
                        </div>
                    </div>
                  </label>
                </div>
            `;
        });


        const html = `
            <div class="row">
                <div class="col-sm-8 row">
                    ${products}
                </div>
                
                <div class="col-sm-4 align-items-start">
                    <span class="fw-bolder">Choose a delivery option:</span>
                    <div class="d-flex flex-column gap-3 mt-2">
                        ${rates}
                    </div>
                </div>
            </div>
        `;

        $summary.prepend(html);
    }

    initChangeCourierEvenListener() {
        $("#summary").on("click", 'input[type="radio"]', async event => {
            const rates = this.model.ratesResponse.rates;
            const selectedRate = rates[event.target.dataset.addressIdx];

            const currentAddress = this.model.calculationResponse.customerDetails.address;
            this.model.calculationResponse = await this.model.fetchCalculations(currentAddress, selectedRate.totalCharge);

            this.elements.update({
                mode: 'payment',
                amount: this.model.calculationResponse.amountTotal,
                currency: this.model.calculationResponse.currencyCode.toLowerCase(),
            });

            this.view.renderSummary(this.model.calculationResponse);
        })
    }
}
