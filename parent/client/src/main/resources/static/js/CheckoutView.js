class CheckoutView {

    renderProductSummary(calculation) {
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

    showSummaryView() {
        console.log("Showing summary checkout details");
        $("#summary-nav, .total").removeClass("d-none");
        $("#address-error").addClass("d-none");
    }

    showAddressError() {
        console.warn("Address validation failed, showing error element");
        $("#summary-nav, .total").addClass("d-none");
        $("#address-error").removeClass("d-none");
    }

    hideAddressButton() {
        $("#address-btn").parent().addClass("d-none");
    }

    showAddressButton() {
        $("#address-btn").parent().removeClass("d-none");
    }

    hidePlaceOrderButton() {
        $("#place-order-btn").parent().addClass("d-none");
    }

    showPlaceOrderButton() {
        $("#place-order-btn").parent().removeClass("d-none");
    }

    hidePaymentButton() {
        $("#payment-btn").parent().addClass("d-none");
    }

    showPaymentButton() {
        $("#payment-btn").parent().removeClass("d-none");
    }

    renderSummaryData(calculation, ratesResponse) {
        console.log("Rendering summary data..");
        const $summary = $("#summary");
        $summary.empty();

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

        $summary.append(html);
    }

    renderSettlementCurrency(calculation) {
        $("#settlement-currency").data("currency-code", calculation.currencyCode);
        $("#settlement-total").empty().text(`(${calculation.currencySymbol}) ${(calculation.amountTotal) / calculation.unitAmount}`);
    }

    renderPresentmentTotal(country, countryData, convertedPrice, targetCurrency) {
        const img = this.renderCountryFlag(countryData);
        $("#presentment-currency").empty().removeClass("d-none").append(`${img}<span class="fw-bolder" id="presentment-total">(${country.currencySymbol}) ${convertedPrice}</span>`).data("currency-code", targetCurrency);
    }

    renderCountryFlag(countryData) {
        let img = "";
        if (countryData[0]) {
            const src = countryData[0].flags.png;
            const alt = countryData[0].flags.alt;
            img = `<img src="${src}" alt="${alt}" class="img-fluid border border-1" style="max-width: 40px; max-height: 25px"/>`;
        }
        return img;
    }

    renderExchangeRate(baseCurrency, exchangeRate, targetCurrency) {
        $("#exchange-rate").text(`1 ${baseCurrency} = ${exchangeRate} ${targetCurrency} (includes 2% conversion fee)`);
    }

    showCurrencies() {
        $(".currencies").removeClass("d-none");
    }

    hideCurrencies() {
        $(".currencies").addClass("d-none");
    }
}
