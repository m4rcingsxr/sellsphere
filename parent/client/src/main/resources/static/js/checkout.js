"use strict"
const stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");

$(function() {
    initializeCheckout();
})

// create stripe session checkout
async function initializeCheckout() {
    const fetchClientSecret = async () => {
        const response = await ajaxUtil.post(`${MODULE_URL}checkout/create_session`);

        return response.clientSecret;
    };

    const checkout = await stripe.initEmbeddedCheckout({
        fetchClientSecret,
    });

    checkout.mount('#checkout');
}