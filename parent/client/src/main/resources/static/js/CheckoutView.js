class CheckoutView {

    constructor() {
        this.continueToPaymentBtn = $("#continue-to-payment-method-btn")
    }

    enableContinueToPaymentBtnSpinner() {
        this.continueToPaymentBtn.attr("disabled", true);
        this.continueToPaymentBtn.find(".spinner-border").removeClass("visually-hidden");
        this.continueToPaymentBtn.find(".continue-btn").addClass("visually-hidden");
    }

    disableContinueToPaymentBtnSpinner() {
        this.continueToPaymentBtn.attr("disabled", false);
        this.continueToPaymentBtn.find(".spinner-border").addClass("visually-hidden");
        this.continueToPaymentBtn.find(".continue-btn").removeClass("visually-hidden");
    }

}