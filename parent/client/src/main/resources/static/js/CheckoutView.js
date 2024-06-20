class CheckoutView {

    constructor() {
        this.continueToPaymentBtn = $("#continue-to-payment-btn");
        this.continueToSummaryBtn = $("#continue-to-summary-btn");
        this.placeOrderBtn = $("#place-order-btn");

        this.total = $(".total");
        this.subtotal = $("#subtotal");
        this.shipping = $("#shipping");
        this.shippingTax = $("#shipping-tax");
        this.totalTax = $("#total-tax");
        this.tax = $("#tax");
        this.products = $("#products");
        this.summaryProducts = $("#summary-products");
    }

    disableAddressTabBtnSpinner() {
        this.continueToPaymentBtn.find(".spinner-border").addClass('visually-hidden')
        this.continueToPaymentBtn.find(".continue-btn").removeClass('visually-hidden')
    }

    enableAddressTabBtnSpinner() {
        this.continueToPaymentBtn.find(".spinner-border").removeClass('visually-hidden')
        this.continueToPaymentBtn.find(".continue-btn").addClass('visually-hidden')
    }

    enablePaymentTabBtnSpinner() {
        this.continueToSummaryBtn.find(".spinner-border").removeClass('visually-hidden')
        this.continueToSummaryBtn.find(".continue-btn").addClass('visually-hidden')
    }

    disablePaymentTabBtnSpinner() {
        this.continueToSummaryBtn.find(".spinner-border").addClass('visually-hidden')
        this.continueToSummaryBtn.find(".continue-btn").removeClass('visually-hidden')
    }

    enableSummaryBtnSpinner() {
        this.placeOrderBtn.find(".spinner-border").removeClass('visually-hidden')
        this.placeOrderBtn.find(".continue-btn").addClass('visually-hidden')
    }

    disableSummaryBtnSpinner() {
        this.placeOrderBtn.find(".spinner-border").addClass('visually-hidden')
        this.placeOrderBtn.find(".continue-btn").removeClass('visually-hidden')
    }

    hideAddressTabBtn() {
        this.continueToPaymentBtn.addClass('visually-hidden');
    }

    showAddressTabBtn() {
        this.continueToPaymentBtn.removeClass('visually-hidden');
    }

    showPaymentTabBtn() {
        this.continueToSummaryBtn.removeClass('visually-hidden');
    }

    hidePaymentTabBtn() {
        this.continueToSummaryBtn.addClass('visually-hidden');
    }

    showSummaryTabBtn() {
        this.placeOrderBtn.removeClass("visually-hidden");
    }

    hideSummaryTabBtn() {
        this.placeOrderBtn.addClass("visually-hidden");
    }

    disableAddressTabBtn() {
        this.continueToPaymentBtn.attr('disabled', true);
    }

    enableAddressTabBtn() {
        this.continueToPaymentBtn.attr('disabled', false);
    }

    disablePaymentTabBtn() {
        this.continueToSummaryBtn.attr('disabled', true);
    }

    enablePaymentTabBtn() {
        this.continueToSummaryBtn.attr('disabled', false);
    }

    disableSummaryTabBtn() {
        this.placeOrderBtn.attr('disabled', true);
    }

    enableSummaryTabBtn() {
        this.placeOrderBtn.attr('disabled', false);
    }

    showPaymentMethodTab() {
        new bootstrap.Collapse("#checkout-payment-accordion", {toggle: true}).show();
    }

    showAddressTab() {
        new bootstrap.Collapse("#checkout-address-accordion", {toggle: true}).show();
    }

    showSummaryTab() {
        new bootstrap.Collapse("#checkout-summary-accordion", {toggle: true}).show();
    }

    renderCheckoutDetails(total, subtotal, tax, totalTax, shippingCost, shippingCostTax, currencySymbol, cart) {
        this.renderCheckoutDetailProducts(cart, currencySymbol);

        const convertPrice = (price) => `${price} ${currencySymbol}`

        this.total.text(convertPrice(total));
        this.subtotal.text(convertPrice(subtotal));
        this.shipping.text(convertPrice(shippingCost));
        this.shippingTax.text(convertPrice(shippingCostTax));
        this.totalTax.text(convertPrice(totalTax));
        this.tax.text(convertPrice(tax));
    }

    renderCheckoutDetailProducts(cart, currencySymbol) {
        const html = cart.map(item =>
            `
            <div class="d-flex justify-content-between">
                <div>
                    <span>${item.quantity} x </span>
                    <span>${item.product.name}</span>
                </div>
                <span>${item.subtotal} ${currencySymbol}</span>
            </div>
        `
        ).join("");

        this.products.empty();
        this.products.append(html);
    }

    showLoadCheckoutDetails() {
        $("#checkout-load-details").removeClass('visually-hidden');
    }

    hideLoadCheckoutDetails() {
        $("#checkout-load-details").addClass('visually-hidden');
    }

    showCheckoutDetails() {
        $("#checkout-details").removeClass('visually-hidden');
    }

    hideCheckoutDetails() {
        $("#checkout-details").addClass('visually-hidden');
    }

    renderShippingRates(rates) {
        const html = rates.map((rate, index) => {
            return `
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault${index}" ${index === 0 ? "checked" : ""} data-rate-index="${index}">
                  <label class="form-check-label" for="flexRadioDefault${index}">
                    <div class="d-flex justify-content-between gap-5">
                        <div style="max-height: 50px; max-width: 50px;">
                            <img src="${rate.courierLogoUrl}" alt="courier logo" class="img-fluid"/>
                        </div>
                        <div class="d-flex flex-column">
                            <span class="text-success fs-7 fw-bolder">Delivery days ${rate.minDeliveryTime} - ${rate.maxDeliveryTime}</span>
                            <span>${rate.courierName}</span>
                            <span class="fs-7">(${rate.currency}) ${rate.totalCharge} - <span class="fw-lighter">Delivery</span></span>
                        </div>
                    </div>
                  </label>
                </div>
            `;
        }).join("");

        $("#shipping-rates").empty().append(html);

    }

    renderSummaryProducts(cart, currencySymbol) {
        const html = cart.map(item =>
            `
                <div class="d-flex gap-3">
                    <img src="${item.product.mainImagePath}" alt="${item.product.name}" class="img-fluid" style="max-width: 100px; max-height: 100px; object-fit: contain"/>
                    <div>
                        <h6>${item.product.name}</h6>
                        <p>${item.quantity} x ${item.product.discountPrice} ${currencySymbol}</p>
                        <span>${item.subtotal} ${currencySymbol}</span>
                    </div>
                </div>
            `
        ).join("");

        this.summaryProducts.empty().append(html);
    }

    enableCurrencyPlaceholders() {
        $("#currencies-load").removeClass("visually-hidden");
    }

    disableCurrencyPlaceholders() {
        $("#currencies-load").addClass("visually-hidden");
    }

    showCurrencies() {
        $(".currencies").removeClass("visually-hidden");
    }

    hideCurrencies() {
        $(".currencies").addClass("visually-hidden");
    }

    /**
     * Renders the presentment total in the target currency.
     * @param {Object} countryData - The country data object.
     */
    renderPresentmentTotal(countryData,currencyCode, currencySymbol, convertedPrice) {

        const img = this.renderCountryFlag(countryData);
        $("#presentment-currency")
            .empty()
            .removeClass("d-none")
            .append(`${img}<span class="fw-bolder" id="presentment-total">(${currencySymbol}) ${convertedPrice}</span>`)
            .data("currency-code", currencyCode)
            .attr("data-currency-code", currencyCode);
    }


    /**
     * Renders the country flag.
     * @param {Object} countryData - The country data object.
     * @returns {string} - The HTML string for the country flag.
     */
    renderCountryFlag(countryData) {
        let img = "";
        if (countryData[0]) {
            const src = countryData[0].flags.png;
            const alt = countryData[0].flags.alt;
            img = `<img src="${src}" alt="${alt}" class="img-fluid border border-1" style="max-width: 40px; max-height: 25px"/>`;
        }
        return img;
    }

    renderSettlementTotal(settlementTotal, currencySymbol) {
        $("#settlement-total").empty().append(`${settlementTotal} ${currencySymbol}`);
    }


    selectSettlementCurrency() {
        $("#presentment-currency").removeClass("currency-checked");
        $("#settlement-currency").addClass("currency-checked");
    }

    selectPresentmentCurrency() {
        $("#presentment-currency").addClass("currency-checked");
        $("#settlement-currency").removeClass("currency-checked");
    }

    updatePresentmentPrice(price, symbol) {
        $("#presentment-total").empty().text(`${price} ${symbol}`);
    }

    updateSettlementPrice(price, symbol) {
        $("#settlement-total").empty().text(`${price} ${symbol}`);
    }

    setExchangeRate(exchangeRate, baseCode, targetCode) {
        $("#exchange-rate").empty().text(`1 ${baseCode} = ${exchangeRate} ${targetCode} (includes 2% conversion fee)`);
    }
}