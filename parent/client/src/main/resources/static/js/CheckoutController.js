// todo : select as 1st primary address always - if any address exist
// todo : on change update view and payment element - to crate payment intent later


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
                this.addressElement =  this.elements.create('address', options);
                this.addressElement.mount('#address-element');
                this._initializeChangeAddressEventListener();
                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }

    _initializePaymentElement(currencyCode = undefined, amount = undefined) {
        return new Promise((resolve, reject) => {
            try {
                const options = {
                    layout: {
                        type: 'tabs',
                        defaultCollapsed: false,
                    }
                };

                if(amount && currencyCode) {
                    options.currency = currencyCode;
                    options.amount = amount;
                }

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
            if(event.complete) {
                const address = event.value.address;

                address.postalCode = address.postal_code;
                address.countryAlpha2 = address.country;

                console.log(address);

                const rateResponse = await this.model.fetchShippingRates(address);
                const rates = rateResponse.rates;

                const bestRate = rates.reduce((lowest, rate) => {
                    return (rate.costRank < lowest.costRank) ? rate : lowest;
                }, rates[0]);

                // Get the totalCharge from the best rate
                const shippingCost = bestRate.totalCharge;



                const calcResponse = await this.model.fetchCalculations(address, shippingCost);
                this.model.calculationResponse = calcResponse;
                this.view.renderSummary(calcResponse);

                // update payment element

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
                elements : this.elements,
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
}
