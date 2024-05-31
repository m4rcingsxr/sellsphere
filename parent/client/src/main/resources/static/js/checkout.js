"use strict"
const stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");

$(function() {
    init();


})

function init() {
    fetchSecret()
        .then(clientSecret => {
            const appearance = {
                theme : 'flat'
            };
            const options = { mode: 'shipping' };
            const elements = stripe.elements({ clientSecret : clientSecret, appearance});
            const addressElement = elements.create('address', options);
            const paymentElement = elements.create('payment', {
                layout: {
                    type: 'tabs',
                    defaultCollapsed: false,
                }
            });
            addressElement.mount('#address-element');
            paymentElement.mount('#payment-element');

            addressElement.on('change', async (event)  => {
                if (event.complete){
                    // Extract potentially complete address
                    const address = event.value.address;

                    address.postalCode = address.postal_code;

                    delete address.postal_code;


                    const map = await fetchCalculation(address);
                    console.log(map);

                }
            })
        })
}

async function fetchSecret() {
    const response = await ajaxUtil.post(`${MODULE_URL}checkout/create-payment-intent`);
    return response.clientSecret;
}

async function fetchCalculation(address) {
    return await ajaxUtil.post(`${MODULE_URL}checkout/calculate-tax`, address)
}

// // set allowed countries
// // contacts
// function initializeAddressElement() {
//
//     const options = {
//         // Fully customizable with appearance API.
//         appearance: { /* ... */ }
//     };
//
//     // Only need to create this if no elements group exist yet.
//     // Create a new Elements instance if needed, passing the
//     // optional appearance object.
//     const elements = stripe.elements(options);
//
//     // Create and mount the Address Element in shipping mode
//     const addressElement = elements.create("address", {
//         mode: "shipping",
//     });
//     addressElement.mount("#address-element");
//
//     addressElement.on('change', (event) => {
//         if (event.complete){
//             // Extract potentially complete address
//             const address = event.value.address;
//             console.log(address);
//         }
//     })
// }
//
// async function initializePaymentElement() {
//
//
//     const options = {
//         clientSecret: response.clientSecret,
//         // Fully customizable with appearance API.
//         appearance: {/*...*/},
//     };
//
// // Set up Stripe.js and Elements to use in checkout form
//     const elements = stripe.elements(options);
//
// // Create and mount the Payment Element
//     const paymentElement = elements.create('payment');
//     paymentElement.mount('#payment-element');
// }

// // embedded checkout
// // create stripe session checkout
// async function initializeCheckout() {
//     const fetchClientSecret = async () => {
//         const response = await ajaxUtil.post(`${MODULE_URL}checkout/create_session`);
//
//         return response.clientSecret;
//     };
//
//     const checkout = await stripe.initEmbeddedCheckout({
//         fetchClientSecret,
//     });
//
//     checkout.mount('#checkout');
// }