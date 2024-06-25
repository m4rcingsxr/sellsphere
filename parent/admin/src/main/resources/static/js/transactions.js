$(function () {
    initTransactionListeners();
})

function initTransactionListeners() {
    $("body").on("click", ".refund-request", async function () {
        const paymentId = $(this).attr("data-id");
        const currencySymbol = $(this).attr("data-currency-symbol");

        const {availableRefund} = await getAvailableRefund(paymentId);

        $("#amount").attr("placeholder", availableRefund);
        $("#currency-symbol").text(currencySymbol);
        $("#refund-btn").attr("data-id", paymentId);

        $("#detailModal").modal('hide');
        $("#refundModal").modal('show');
    })

    $("#refund-btn").on("click", async function () {
        const paymentId = $(this).attr("data-id");

        try {
            const response = await requestRefund({
                paymentIntent: paymentId,
                amount: $("#amount").val(),
                reason: $("#reason").val()
            })

            const {status, refunded} = response;

            if (status !== 'failed') {


                const $refundIcon = $(`#refund-request${paymentId}`);
                if (refunded) {
                    $refundIcon.remove();
                }

                $("#refundModal").modal('hide');
            } else {
                showErrorMessage();
            }
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }


    })

    $("#amount").on("change", async function () {
        try {
            // show spinner

            const $this = $(this);
            const requestedAmount = $this.val();


            // fetch available refund amount
            const response = await isRefundApproved($("#refund-btn").attr("data-id"), requestedAmount);
            const {approve, availableRefund} = response;

            if (approve) {
                $this.removeClass("is-invalid");
                $this.addClass("is-valid");
                hideErrorMessage();
            } else {
                $this.removeClass("is-valid");
                $this.addClass("is-invalid");
                showErrorMessage();
            }

        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            // hide spinner
        }

    })
}

function showErrorMessage() {
    $("#error").removeClass("d-none");
}

function hideErrorMessage() {
    $("#error").addClass("d-none");
}


async function requestRefund(request) {
    return await ajaxUtil.post(`${MODULE_URL}transactions/refund`, request);
}

async function isRefundApproved(id, amount) {
    return ajaxUtil.get(`${MODULE_URL}transactions/refunds/check-eligibility?id=${id}&amount=${amount}`)
}

async function getAvailableRefund(id) {
    return ajaxUtil.get(`${MODULE_URL}transactions/refunds/available-refund?id=${id}`)
}