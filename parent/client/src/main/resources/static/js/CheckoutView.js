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
        $("#recaptcha-order").addClass("visually-hidden");
    }

    showSummaryTabBtn() {
        this.placeOrderBtn.removeClass("visually-hidden");
        $("#recaptcha-order").removeClass("visually-hidden");
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
                    <input type="radio" class="btn-check" name="vbtn-radio" id="vbtn-radio${index}" autocomplete="off" ${index === 0 ? "checked" : ""} data-rate-index="${index}">
                    <label class="btn btn-outline-secondary" for="vbtn-radio${index}">
                       
                          <div class="d-flex gap-2 ">
                               <div class="w-25">
                                <img src="${rate.courierLogoUrl}" style="max-height: 50px; max-width: 50px;" alt="courier logo" class="img-fluid"/>
                            </div>
                            
                            <div class="d-flex flex-column gap-1 w-75">
                                <span class="fs-7 fw-bolder">Delivery days ${rate.minDeliveryTime} - ${rate.maxDeliveryTime}</span>
                                <span>${rate.courierName}</span>
                                <span class="fs-7">(${rate.currency}) ${rate.totalCharge} - <span class="fw-lighter">Delivery</span></span>
                            </div>
                        </div>
                    </label>
 
                 
            `;
        }).join("");

        $("#shipping-rates").empty().append(html);

    }

    renderSummaryProducts(cart, currencySymbol) {
        const html = cart.map(item => {
            // Determine if the discountPercent is greater than 0
            const hasDiscount = item.product.discountPercent > 0;

            // Format prices with 2 decimal places
            const formattedPrice = parseFloat(item.product.price).toFixed(2);
            const formattedDiscountPrice = parseFloat(item.product.discountPrice).toFixed(2);

            // Create HTML structure
            return `
            <div class="d-flex gap-3">
                <img src="${item.product.mainImagePath}" alt="${item.product.name}" class="img-fluid" style="max-width: 100px; max-height: 100px; object-fit: contain"/>
                <div>
                    <h6><a class="link link-dark link-underline link-underline-opacity-50-hover link-underline-opacity-0" href="${MODULE_URL}p/${encodeURIComponent(item.product.alias)}">${item.product.name}</a></h6>
                    <p class="fw-bolder">
                        ${item.quantity} x 
                        ${hasDiscount ? `<span class="fw-lighter text-decoration-line-through fs-6">${currencySymbol}${formattedPrice}</span> ` : ''}
                        <strong>${currencySymbol}${formattedDiscountPrice}</strong>
                    </p>
                </div>
            </div>
        `;
        }).join("");

        this.summaryProducts.empty().append(html);
    }




}