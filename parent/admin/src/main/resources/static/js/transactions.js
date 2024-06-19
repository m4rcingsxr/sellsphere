$(function () {
    initTransactionListeners();
})

function initTransactionListeners() {
    $("body").on("click", ".refund-request", function () {
        const paymentId = $(this).attr("data-id");
        const currencySymbol = $(this).attr("currency-symbol");

        $("#currency-symbol").text(currencySymbol);
        $("#refund-btn").attr("data-id", paymentId);

        $("#detailModal").modal('hide');
        $("#refundModal").modal('show');
    })

    $("#refund-btn").on("click", function () {
        const paymentId = $(this).attr("data-id");
        requestRefund({
            paymentIntent: paymentId,
            amount: $("#amount").val(),
            reason: $("#reason").val()
        })
            .then(response => {
                console.log(response);
                $("#refundModal").modal('hide');
            })
            .catch(error => {
                console.error(error)
                showErrorModal(error.response);
            });
    })
}

async function requestRefund(request) {
    return await ajaxUtil.post(`${MODULE_URL}transactions/refund`, request);
}