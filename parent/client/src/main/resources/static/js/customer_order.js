const sidebar = document.querySelector("#sidebar");
const returnSidebar = document.querySelector("#returnSidebar");
const overlay = document.getElementById("overlay");
const content = document.querySelector(".content");

$(function () {

    $(".entity-detail").on("click", async function (event) {

        event.preventDefault();

        const orderId = $(this).data("id");
        const html = await fetchOrderDetailHtml(orderId);

        const $sidebar = $(sidebar);
        $sidebar.empty();
        $sidebar.html(html);

        sidebar.classList.add("show");
        overlay.style.display = "block";
        content.classList.add("blur");
    });

    $("#overlay").on("click", function (event) {
        sidebar.classList.remove("show");
        overlay.style.display = "none";
        content.classList.remove("blur");
        returnSidebar.classList.remove("show");
    })

    $("#sidebar").on("click", "#returnBtn", function (event) {
        event.preventDefault();

        sidebar.classList.remove("show");
        returnSidebar.classList.add("show");
    })

    $("#cancelBtn").on("click", function (event) {
        event.preventDefault();

        returnSidebar.classList.remove("show");
        sidebar.classList.add("show");
    })


})

async function fetchOrderDetailHtml(orderId) {
    try {
        const url = `${MODULE_URL}orders/detail/${orderId}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        return await response.text();
    } catch (error) {
        console.error('Failed to fetch order details:', error);
        return `<p>Error loading order details. Please try again later.</p>`;
    }
}