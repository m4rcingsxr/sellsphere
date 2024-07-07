$(function () {
    $("#rateYo").rateYo({
        ratedFill: "#0d6efd",
        readonly: true,
        fullStar: true,
        numStars: 5,
        maxValue: 5,
        spacing: "3px",
        starWidth: "30px",
        precision: 2,
        onChange: function (rating) {
            $("#rate").val(rating);
        }
    })

    $("#cancelModalBtn").on("click", function() {
        $("#exampleModalCenter").modal("hide");
    })

    $("#reviews").on("click", ".post-review", function (event) {
        event.preventDefault();
        const src = this.dataset.src;
        const name = this.dataset.name;
        const discountPrice = this.dataset.discountPrice;
        const productId = this.dataset.productId;
        const alias = this.dataset.alias;

        const productLink = `<a href="${MODULE_URL}p/${alias}" class="link-dark link-underline link-underline-opacity-0 link-underline-opacity-50-hover">${name}</a>`;


        document.querySelector("#productImage").src = src;
        $("#productName").empty().html(productLink);
        $("#productPrice").text(discountPrice);
        $("#product").val(productId);


        $("#exampleModalCenter").modal("show");
    })


    $(".entity-detail").on("click", async function (event) {
        event.preventDefault();

        const reviewId = $(this).data("id");
        const html = await fetchOrderDetailHtml(reviewId);

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

})

async function fetchOrderDetailHtml(entityId) {
    try {
        const url = `${MODULE_URL}reviews/detail/${entityId}`;
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