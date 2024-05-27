"use strict";

$(function() {
    initialize();
})


async function initialize() {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    const sessionId = urlParams.get('session_id');
    const response = await fetch(`${MODULE_URL}checkout/session_status?session_id=${sessionId}`);
    const session = await response.json();

    if (session.status === 'open') {
        window.replace(`${MODULE_URL}checkout`)
    } else if (session.status === 'complete') {
        document.getElementById('success').classList.remove('d-none');
        document.getElementById('customer-email').textContent = session.customer_email
    }
}