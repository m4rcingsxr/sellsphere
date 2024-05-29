"use strict"
const stripe = Stripe("pk_test_51PbVfkJrJARv0MdlEH5DxX7JRUSYQ93fy1eXxlucq712MqQLY6FrLIWceaT5Pc14faRUhvFrRFGe201s2dbHXPEg00bGFEsYtI");

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