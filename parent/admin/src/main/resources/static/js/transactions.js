$(function () {
    initTransactionListeners();
});

function initTransactionListeners() {
    $("body").on("click", ".refund-request", async function () {
        const paymentId = $(this).attr("data-id");
        const currencySymbol = $(this).attr("data-currency-symbol");

        showFullScreenSpinner();
        try {
            const { availableRefund } = await getAvailableRefund(paymentId);

            $("#amount").attr("placeholder", availableRefund);
            $("#currency-symbol").text(currencySymbol);
            $("#refund-btn").attr("data-id", paymentId);

            $("#detailModal").modal('hide');
            $("#refundModal").modal('show');
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            hideFullScreenSpinner();
        }
    });

    $("#refund-btn").on("click", async function () {
        const paymentId = $(this).attr("data-id");

        showFullScreenSpinner();
        try {
            const response = await requestRefund({
                paymentIntent: paymentId,
                amount: $("#amount").val(),
                reason: $("#reason").val()
            });

            const { status, refunded, refundedAmount } = response;

            if (status !== 'failed') {
                const $refundIcon = $(`#refund-request${paymentId}`);
                const $status = $(`#status${paymentId}`);
                const $refunded = $(`#refunded${paymentId}`);

                $refunded.text(refundedAmount);
                $status.text(status);

                if (refunded) {
                    $refundIcon.remove();
                }

                $("#refundModal").modal('hide');
                $("#amount").val("");
            } else {
                showErrorMessage();
            }
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            hideFullScreenSpinner();
        }
    });

    $("#amount").on("change", async function () {
        showFullScreenSpinner();
        try {
            const $this = $(this);
            const requestedAmount = $this.val();

            const response = await isRefundApproved($("#refund-btn").attr("data-id"), requestedAmount);
            const { approve, availableRefund } = response;

            if (approve) {
                $this.removeClass("is-invalid").addClass("is-valid");
                hideErrorMessage();
            } else {
                $this.removeClass("is-valid").addClass("is-invalid");
                showErrorMessage();
            }
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            hideFullScreenSpinner();
        }
    });
}

function showErrorMessage() {
    $("#error").removeClass("d-none");
}

function hideErrorMessage() {
    $("#error").addClass("d-none");
}

async function requestRefund(request) {
    showFullScreenSpinner();
    try {
        return await ajaxUtil.post(`${MODULE_URL}transactions/refund`, request);
    } finally {
        hideFullScreenSpinner();
    }
}

async function isRefundApproved(id, amount) {
    showFullScreenSpinner();
    try {
        return await ajaxUtil.get(`${MODULE_URL}transactions/refunds/eligibility?id=${id}&amount=${amount}`);
    } finally {
        hideFullScreenSpinner();
    }
}

async function getAvailableRefund(id) {
    showFullScreenSpinner();
    try {
        return await ajaxUtil.get(`${MODULE_URL}transactions/refunds/available?id=${id}`);
    } finally {
        hideFullScreenSpinner();
    }
}
