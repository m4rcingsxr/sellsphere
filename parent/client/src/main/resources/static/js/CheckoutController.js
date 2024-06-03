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
                    },
                    fields: {
                        billingDetails: {
                            name: 'auto',
                            email: 'auto',
                            phone: 'auto',
                            address: 'auto',
                        }
                    }
                };

                this.paymentElement = this.elements.create('payment', options);
                this.paymentElement.mount('#payment-element');
                this._initializeChangePaymentElementEventListener();
                resolve();
            } catch (error) {
                reject(error);
            }
        });
    }

    _initializeChangeAddressEventListener() {

        // fix too many request - on every character change return not valid response
        this.addressElement.on('change', async event => {
            if (event.complete) {
                console.log(event.target);

                // validate address here
                const address = event.value.address;

                const addressLines = [];

                if (address.line1) addressLines.push(address.line1);
                if (address.line2) addressLines.push(address.line2);

                const isValid = await this.model.validateAddress({
                    "address": {
                        regionCode: address.country,
                        postalCode:
                        address.postal_code,
                        locality:
                        address.city,
                        addressLines,
                    }
                });

                if (isValid) {
                    address.postalCode = address.postal_code;
                    address.countryAlpha2 = address.country;

                    console.log(address);
                    address.currencyCode = this.model.calculationResponse.currencyCode;

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

                    $("#address-btn").removeClass("d-none");
                    $("#address-error").addClass("d-none");
                } else {
                    this.model.calculationResponse = undefined;
                    $("#address-btn").addClass("d-none");
                    $("#address-error").removeClass("d-none");
                }
            }
        });
    }

    _initializeChangePaymentElementEventListener() {
        this.paymentElement.on('change', async event => {
            if (event.complete) {
                this.model.payment = event;
            } else {
                this.model.payment = undefined;
            }
        })
    }

    initPlaceOrderBtnListener() {
        const placeOrderBtn = document.getElementById('place-order-btn');
        if (placeOrderBtn) {
            placeOrderBtn.addEventListener('click', event => {
                event.preventDefault();
                const form = document.getElementById('payment-form');
                if (form) {
                    form.dispatchEvent(new Event('submit', {cancelable: true, bubbles: true}));
                } else {
                    console.error('Payment form not found.');
                }
            });
        } else {
            console.error('Place order button not found.');
        }
    }

    initSubmitPaymentEventListener() {
        const form = document.getElementById('payment-form');
        const submitBtn = document.getElementById('place-order-btn');

        if (!form || !submitBtn) {
            console.error('Form or submit button not found.');
            return;
        }

        const handleError = (error) => {
            const messageContainer = document.querySelector('#error-message');
            if (messageContainer) {
                messageContainer.textContent = error.message;
            }
            submitBtn.disabled = false;
        };

        form.addEventListener('submit', async (event) => {
            // Prevent default form submission
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

            try {
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
                    handleError(error);
                }
            } catch (error) {
                handleError(error);
            }
        });
    }

    initAddressBtnAccordionListener() {
        $("#address-accordion-btn").on("click", event => {
            $("#address-btn").parent().removeClass("d-none");
            $("#place-order-btn").parent().addClass("d-none");
            $("#payment-btn").parent().addClass("d-none");
        })
    }

    initPaymentBtnAccordionListener() {
        $("#payment-accordion-btn").on("click", event => {
            event.preventDefault();

            if (this.model.calculationResponse?.customerDetails?.address) {
                const $addressBtn = $("#address-btn");
                $addressBtn.parent().addClass("d-none");
                $("#place-order-btn").parent().addClass("d-none");

                const paymentAccordion = new bootstrap.Collapse('#checkout-payment-accordion', {
                    toggle: true
                });
                paymentAccordion.show();

                $("#payment-btn").parent().removeClass("d-none");

            } else {
                console.warn("specify address first!");
            }
        });
    }


    initSummaryBtnAccordionListener() {
        $("#summary-button").on("click", event => {
            event.preventDefault();

            if (this.model.calculationResponse?.customerDetails?.address && this.model.payment) {
                const $paymentBtn = $("#payment-btn");
                $paymentBtn.parent().addClass("d-none");
                $("#address-btn").parent().addClass("d-none");

                $("#place-order-btn").parent().removeClass("d-none");

                const summaryAccordion = new bootstrap.Collapse('#checkout-summary-accordion', {
                    toggle: true
                });

                this._loadSummaryAccordion();
                summaryAccordion.show();

                $(".currencies").removeClass("d-none");
            } else {
                console.warn("specify address first!");
            }
        });
    }

    initContinueShipBtnListener() {
        $("#address-btn").on("click", event => {

            if (this.model.calculationResponse?.customerDetails?.address) {
                const $addressBtn = $("#address-btn");
                $addressBtn.parent().addClass("d-none");
                $("#place-order-btn").parent().addClass("d-none");
                $("#payment-btn").parent().removeClass("d-none");


                const paymentAccordion = new bootstrap.Collapse('#checkout-payment-accordion', {
                    toggle: true
                });
                paymentAccordion.show();
            } else {
                console.warn("specify address first!");
            }

        })
    }

    initContinuePaymentBtnListener() {
        $("#payment-btn").on("click", event => {
            const $paymentBtn = $("#payment-btn");
            $paymentBtn.parent().addClass("d-none");
            $("#address-btn").parent().addClass("d-none");

            $("#place-order-btn").parent().removeClass("d-none");

            if (this.model.calculationResponse?.customerDetails?.address && this.model.payment) {
                const summaryAccordion = new bootstrap.Collapse('#checkout-summary-accordion', {
                    toggle: true
                });

                this._loadSummaryAccordion();
                summaryAccordion.show();

                const $currencies = $(".currencies");
                this.loadCurrencyData()
                    .then(() => {

                        $currencies.removeClass("d-none");
                    });
            } else {
                console.warn("specify address first!");
            }
        });
    }

    async loadCurrencyData() {
        const country = await this.model.fetchCountryForClientIp();

        const exchangeRateResponse = await this.model.fetchExchangeRatesForClientCountry(country.currencyCode);

        const targetCurrency = country.currencyCode;
        const baseCurrency = exchangeRateResponse.base;
        const exchangeRate = exchangeRateResponse.result['rate'];
        const convertedPrice = exchangeRateResponse.result[country.currencyCode];

        const countryData = await this.model.fetchCountryData(country.code);

        let img = "";
        if(countryData[0]) {
            const src = countryData[0].flags.png;
            const alt = countryData[0].flags.alt;
            img = `
                <img src="${src}" alt="${alt}" class="img-fluid border border-1" style="max-width: 40px; max-height: 25px"/>
            `;

        }

        // get currency price from rest api
        $(".currencies").removeClass("d-none");
        $("#presentment-currency").empty().removeClass("d-none").append(
            `
                ${img}<span class="fw-bolder total">(${country.currencySymbol}) ${convertedPrice}</span>
            `
        ).data("currency-code", targetCurrency);
        $("#settlement-currency").data("currency-code", baseCurrency);
        $("#exchange-rate").text(`1 ${baseCurrency} = ${exchangeRate} ${targetCurrency} (includes 2% conversion fee)`);
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

    initChangeCurrencyBtn() {
        $("#currencies").on("click", "a", async event => {
            event.preventDefault();
            const $currency = $(event.currentTarget);
            const currencyCode = $currency.data("currency-code");


            const address = Object.assign({}, this.model.calculationResponse.customerDetails.address);
            address.countryAlpha2 = address.country;
            address.currencyCode = currencyCode;

            this.model.ratesResponse = await this.model.fetchShippingRates(address);
            const rates = this.model.ratesResponse.rates;

            this._loadSummaryAccordion();

            const rateId = $(`input[type="radio"]:checked`).data("address-idx");
            const selectedRate = rates[rateId];

            this.model.fetchCalculations(this.model.calculationResponse.customerDetails.address, selectedRate.totalCharge, currencyCode)
                .then(response => {
                    this.model.calculationResponse = response;

                    this.view.renderSummary(response);

                });
        });

    }
}
