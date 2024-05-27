"use strict"
const stripe = Stripe("pk_test_51PailoF45HI7vp7sp5BbUCRTRYsFIyJvKGnnVHoz4dtfJIqGftHxectWWnZNgi4F29xCx6ENlBOKq9RNKCvX3NXt00ZLAgqbHn");

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