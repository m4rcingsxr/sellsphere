$(function () {
    window.stripe = Stripe("pk_test_51PbnPoAx8ZpOoq6Y2e0LqnQAZamnRJ6rBeShPoZVd7up9My5tepRm9Vhowv6qGue29a8aDz4r0YT5BkN3XnqQPrR00yMU9sery");
    window.elements = stripe.elements();
    window.card = elements.create('card');
    window.card.mount('#card-element');

    const validator = new FormValidator('mainForm', [
        {
            name: 'email',
            display: 'Email',
            rules: 'required|valid_email|max_length[45]'
        },
        {
            name: 'password',
            display: 'Password',
            rules: 'min_length[8]|max_length[64]'
        },
        {
            name: 'confirmedPassword',
            display: 'Password confirmation',
            rules: 'matches[password]'
        },
        {
            name: 'firstName',
            display: 'First name',
            rules: 'required|alpha|max_length[45]'
        },
        {
            name: 'lastName',
            display: 'Last name',
            rules: 'required|alpha|max_length[45]'
        },
        {
            name: "g-recaptcha-response",
            display: "Recaptcha response",
            rules: "required"
        }
    ], handleValidations);

    $("#save-card").on("click", event => {
        createPaymentMethod();
    });

    $("#card-container").on("click", ".delete-card", function (event) {
        event.preventDefault();
        const cardId = $(this).data("card-id");
        detachPaymentMethod(cardId, $(this).parent().parent().parent())
    })

})


async function detachPaymentMethod(cardId, $container) {
    try {
        showFullScreenSpinner();
        const url = `${config.baseUrl}/payment-methods/detach`;
        await ajaxUtil.post(url, {paymentMethodId: cardId});

        $container.remove();
    } catch (error) {
        console.error(error);
        showErrorModal(error);
    } finally {
        hideFullScreenSpinner();
    }

}

async function createPaymentMethod() {
    try {
        showFullScreenSpinner()
        const paymentMethodResponse = await window.stripe.createPaymentMethod({
            type: 'card',
            card: window.card,
        })

        if (paymentMethodResponse.error) {
            console.error(paymentMethodResponse.error);
        } else {
            const url = `${config.baseUrl}/payment-methods/attach`;
            const response = await ajaxUtil.post(url, {paymentMethod: paymentMethodResponse.paymentMethod.id});

            if (response.status === "success") {
                const container = $("#card-container");
                const card = paymentMethodResponse.paymentMethod.card;

                const currentDate = new Date();
                const cardExpirationDate = new Date(card.exp_year, card.exp_month - 1); // Months are zero-indexed in JavaScript
                const isExpired = currentDate > cardExpirationDate;
                const formattedExpirationDate = `${String(card.exp_month).padStart(2, '0')}/${card.exp_year}`;

                const html = `
                        <div class="col-md-4 col-6">
                            <div class="border border-1 border-dark-subtle rounded rounded-3">
                                <div class="d-flex justify-content-between p-2">
                                    <img class="img-fluid rounded-3 rounded" src="${S3_BASE_URI}/card-photos/${card.display_brand}.jpg"
                                         style="height: 40px; width: 50px;"/>
                                    <a href="#" data-card-id="${paymentMethodResponse.paymentMethod.id}"
                                       class="delete-card  link-dark link-underline-dark link-underline-opacity-0 link-underline-opacity-50-hover fs-7 px-3 pt-1">Remove</a>
                                </div>
                                <div class="d-flex align-items-center flex-column">
                                    <span class="fw-bolder">**** **** **** ${card.last4}</span>
                                    <div class="text-start">
                                        ${!isExpired
                    ? `<span class="fs-7 text-light-emphasis">Expiration ${formattedExpirationDate}</span>`
                    : `<span class="text-danger fs-6">Expired ${formattedExpirationDate}</span>`}
                                    </div>
                                </div>
                                <div class="d-flex justify-content-end p-2">
                                    <a href="#" class="btn btn-sm btn-outline-secondary p-0 px-4 py-1">Edit</a>
                                </div>
                            </div>
                        </div>
                    `;

                const children = container.children();
                if (children.length > 0) {
                    $(html).insertBefore(children.last());
                } else {
                    container.append(html);
                }

                $("#exampleModal").removeClass("show");
                $(".modal-backdrop").removeClass("show");
                window.card.clear();

            }
        }


    } catch (error) {
        console.error(error);
    } finally {
        hideFullScreenSpinner();
    }

}