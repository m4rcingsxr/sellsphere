"use strict";

$(function() {

    initialize();
})


async function initialize() {
    const stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");

    const clientSecret = new URLSearchParams(window.location.search).get(
        "payment_intent_client_secret"
    );

    if (!clientSecret) {
        return;
    }

    const { paymentIntent } = await stripe.retrievePaymentIntent(clientSecret);

    switch (paymentIntent.status) {
        case "succeeded":
            cartController.clear();
            showSuccessView();
            break;
        case "processing":
            cartController.clear();
            showProcessingView();
            break;
        case "requires_payment_method":
            showRequirePaymentView();
            break;
        default:
            showFailureView();
            break;
    }
}

function showSuccessView() {
    $("#success-view").removeClass("d-none");
}

function showProcessingView() {
    $("#processing-view").removeClass("d-none");
}

function showRequirePaymentView() {
    $("#require-payment-view").removeClass("d-none");
}

function showFailureView() {
    $("#failure-view").removeClass("d-none");
}
