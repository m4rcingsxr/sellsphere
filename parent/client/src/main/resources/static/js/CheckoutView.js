/**
 * View for rendering the checkout process.
 */
class CheckoutView {

    /**
     * Renders the product summary.
     * @param {Object} calculation - The calculation object containing product data.
     */
    renderProductSummaryNav(calculation) {
        const $products = $("#products");
        $products.empty();

        calculation.cart.forEach(cartItem => {
            const html = `
                <div class="d-flex justify-content-between">
                    <span class="">${cartItem.product.name}</span>
                    <span>(${calculation.currencySymbol}) ${cartItem.quantity * cartItem.product.discountPrice}</span>
                </div>
            `;
            $products.append(html);
        });
    }

    showProductsSummaryNavPlaceholder() {
        $("#products-load").removeClass("d-none");
    }

    hideProductsSummaryNavPlaceholder() {
        $("#products-load").addClass("d-none");
    }

    showProductSummaryNav() {
        $("#products").removeClass("d-none")
    }

    hideProductSummaryNav() {
        $("#products").addClass("d-none")
    }

    showSummaryLoadNav() {
        $("#summary-load-nav").removeClass("d-none");
    }

    hideSummaryLoadNav() {
        $("#summary-load-nav").addClass("d-none");
    }

    hideSummaryNav() {
        $("#summary-nav").addClass("d-none");
    }

    showSummaryNav() {
        $("#summary-nav").removeClass("d-none");
    }

    showTotal() {
        $(".total").removeClass("d-none");
    }

    hideTotal() {
        $(".total").addClass("d-none");
    }

    showLoadTotal() {
        $("#total-load").removeClass("d-none");
    }

    hideLoadTotal() {
        $("#total-load").addClass("d-none");
    }

    showAddressLoadBtn() {
        $("#address-load-btn").parent().removeClass("d-none");
    }

    hideAddressLoadBtn() {
        $("#address-load-btn").parent().addClass("d-none");
    }

    showPaymentLoadBtn() {
        $("#payment-load-btn").parent().removeClass("d-none");
    }

    hidePaymentLoadBtn() {
        $("#payment-load-btn").parent().addClass("d-none");
    }

    showLoadPlaceOrderButton() {
        $("#place-order-load-btn").parent().removeClass("d-none");
    }

    hideLoadPlaceOrderButton() {
        $("#place-order-load-btn").parent().addClass("d-none");
    }


    /**
     * Renders the summary details.
     * @param {Object} calculation - The calculation object.
     */
    renderSummary(calculation) {
        console.log("Rendering summary details for calculation");
        const $shipping = $("#shipping");
        const $tax = $("#tax");
        const $totalTax = $("#total-tax");
        const $shippingTax = $("#shipping-tax");
        const $subtotal = $("#subtotal");
        const $total = $(".total");

        $total.text(`(${calculation.currencySymbol}) ${(calculation.amountTotal) / calculation.unitAmount}`);
        $subtotal.text(`(${calculation.currencySymbol}) ${(calculation.amountTotal - calculation.shippingCost.amount) / calculation.unitAmount}`);
        $shipping.text(`(${calculation.currencySymbol}) ${calculation.shippingCost.amount / calculation.unitAmount}`);
        $tax.text(`(${calculation.currencySymbol}) ${calculation.taxAmountInclusive / calculation.unitAmount}`);
        $shippingTax.text(`(${calculation.currencySymbol}) ${calculation.shippingCost.amountTax / calculation.unitAmount}`);
        $totalTax.text(`(${calculation.currencySymbol}) ${(calculation.taxAmountInclusive + calculation.shippingCost.amountTax) / calculation.unitAmount}`);
    }

    /**
     * Shows the summary view.
     */
    showSummaryView() {
        console.log("Showing summary checkout details");
        $("#summary-nav, .total").removeClass("d-none");
        $("#address-error").addClass("d-none");
    }

    /**
     * Hides the address button.
     */
    hideAddressButton() {
        $("#address-btn").parent().addClass("d-none");
    }

    /**
     * Shows the address button.
     */
    showAddressButton() {
        $("#address-btn").parent().removeClass("d-none");
    }

    /**
     * Hides the place order button.
     */
    hidePlaceOrderButton() {
        $("#place-order-btn").parent().addClass("d-none");
    }

    /**
     * Shows the place order button.
     */
    showPlaceOrderButton() {
        $("#place-order-btn").parent().removeClass("d-none");
    }

    /**
     * Hides the payment button.
     */
    hidePaymentButton() {
        $("#payment-btn").parent().addClass("d-none");
    }

    /**
     * Shows the payment button.
     */
    showPaymentButton() {
        $("#payment-btn").parent().removeClass("d-none");
    }

    showLoadCurrency() {
        $("#currencies-load").removeClass("d-none");
    }

    hideLoadCurrency() {
        $("#currencies-load").addClass("d-none");
    }

    /**
     * Renders the summary data including products and shipping rates.
     * @param {Object} calculation - The calculation object.
     * @param {Object} ratesResponse - The rates response object.
     */
    renderSummaryData(calculation, ratesResponse) {
        console.log("Rendering summary data..");

        this.renderSummaryProducts(calculation);
        this.renderSummaryRates(ratesResponse);
    }

    renderSummaryRates(ratesResponse) {
        let rates = "";
        ratesResponse.rates.forEach((rate, index) => {
            rates += `
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="flexRadioDefault" id="flexRadioDefault${index}" ${index === 0 ? "checked" : ""} data-address-idx="${index}">
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

        $("#summary-rates").empty().append(rates);
    }

    renderSummaryProducts(calculation) {
        let products = "";
        calculation.cart.forEach(item => {
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

        $("#summary-products").empty().append(products);

        return products;
    }

    /**
     * Renders the settlement currency details.
     * @param {Object} calculation - The calculation object.
     */
    renderSettlementCurrency(calculation) {

        $("#settlement-currency").data("currency-code", calculation.currencyCode).attr("data-currency-code", calculation.currencyCode);
        ;$("#settlement-total").empty().text(`(${calculation.currencySymbol}) ${(calculation.amountTotal) / calculation.unitAmount}`);
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

    /**
     * Renders the exchange rate.
     * @param {string} baseCurrency - The base currency code.
     * @param {number} exchangeRate - The exchange rate.
     * @param {string} targetCurrency - The target currency code.
     */
    renderExchangeRate(baseCurrency, exchangeRate, targetCurrency) {
        $("#exchange-rate").text(`1 ${baseCurrency} = ${exchangeRate} ${targetCurrency} (includes 2% conversion fee)`);
    }

    /**
     * Shows the currencies.
     */
    showCurrencies() {
        $(".currencies").removeClass("d-none");
    }

    /**
     * Hides the currencies.
     */
    hideCurrencies() {
        $(".currencies").addClass("d-none");
    }

    checkRateRadio(rateIdx) {
        $(`input[data-address-idx="${rateIdx}"]`).prop('checked', true);
    }

    selectCurrency(previousCurrency, currentCurrency) {
        $(`a[data-currency-code="${currentCurrency}"]`).addClass("currency-checked");
        $(`a[data-currency-code="${previousCurrency}"]`).removeClass("currency-checked");
    }
}
