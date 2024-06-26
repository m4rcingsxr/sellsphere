"use strict"

$(function() {

    $("#returnForm").on("submit", function(event) {
        event.preventDefault();

        const returnRequest = {
            reason : $('input[name="options"]:checked').next('label').text(),
            note : $("#note").val(),
            orderId : $("#orderId").val()
        }

        sendReturnRequest(returnRequest)
            .then(returnResponse => {
                const toast =  document.getElementById('successToast');
                $("#returnBtn").remove();

                const html = `
                <span class="status-return-requested status-circle d-block"></span> <span class="d-block">Return requested</span>
                `;
                const $orderStatus = $("#sidebar .status-circle").parent();
                $orderStatus.empty();
                $orderStatus.html(html);

                const $tableOrderStatus = $(`#orderStatus${returnRequest.orderId}`).find('.d-flex');
                $tableOrderStatus.empty();
                $tableOrderStatus.html(html);


                $("#returnSidebar").removeClass("show");
                $("#sidebar").addClass("show");
                const successToast = bootstrap.Toast.getOrCreateInstance(toast)
                successToast.show();
            })
            .catch(error => {
                alert(error);
            });
    })
});

async function sendReturnRequest(returnRequest) {
    try {
        const response = await fetch(`${MODULE_URL}orders/return`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                "X-CSRF-Token": $('input[name="_csrf"]').val(),
            },
            body: JSON.stringify(returnRequest)
        });

        if (!response.ok) {
            throw new Error('Network response was not ok ' + response.statusText);
        }

        return await response.json();
    } catch (error) {
        throw error;
    }
}