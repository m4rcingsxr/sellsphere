class CheckoutView {

    constructor() {

    }

    renderSummary(calcResponse) {
        const $products = $("#products");
        $products.empty();

        const $shipping = $("#shipping");
        const $tax = $("#tax");
        const $totalTax = $("#total-tax");
        const $shippingTax = $("#shipping-tax");

        const $subtotal = $("#subtotal");
        const $total = $(".total");


        calcResponse.products.forEach(product => {
            const html = `
                     <div class="d-flex justify-content-between">
                        <span class="">${product.name}</span>
                        <span>(${calcResponse.currencySymbol}) ${product.discountPrice}</span>
                    </div>
               `;
            $products.append(html);
        });


        if(calcResponse.taxAmountInclusive) {
            console.log(calcResponse);
            $total.text(`(${calcResponse.currencySymbol}) ${(calcResponse.amountTotal) / calcResponse.unitAmount}`);
            $subtotal.text(`(${calcResponse.currencySymbol}) ${(calcResponse.amountTotal - calcResponse.shippingCost.amount) / calcResponse.unitAmount}`);

            $shipping.text(`(${calcResponse.currencySymbol}) ${calcResponse.shippingCost.amount / calcResponse.unitAmount}`);

            $tax.text(`(${calcResponse.currencySymbol}) ${calcResponse.taxAmountInclusive / calcResponse.unitAmount}`);
            $shippingTax.text(`(${calcResponse.currencySymbol}) ${calcResponse.shippingCost.amountTax / calcResponse.unitAmount}`);
            $totalTax.text(`(${calcResponse.currencySymbol}) ${(calcResponse.taxAmountInclusive + calcResponse.shippingCost.amountTax) / calcResponse.unitAmount}`);
        } else {
            $total.text(`(${calcResponse.currencySymbol}) ${(calcResponse.amountTotal) / calcResponse.unitAmount}`);
            $subtotal.text(`(${calcResponse.currencySymbol}) ${(calcResponse.amountTotal) / calcResponse.unitAmount}`);

            $shippingTax.text('Provide address to finish calculation')
            $totalTax.text('Provide address to finish calculation')
            $shipping.text('Provide address to finish calculation');
            $tax.text('Provide address to finish calculation');
        }
    }
}