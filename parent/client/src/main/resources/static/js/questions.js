$(function() {

    const sidebar = document.querySelector("#sidebar");
    const overlay = document.getElementById("overlay");
    const content = document.querySelector(".content");

    $(".entity-detail").on("click", async function (event) {
        event.preventDefault();



        const questionId = $(this).data("id");
        const html = await fetchQuestionDetailHtml(questionId);

        const $sidebar = $(sidebar);
        $sidebar.empty();
        $sidebar.html(html);

        sidebar.classList.add("show");
        overlay.style.display = "block";
        content.classList.add("blur");
    });

    $(overlay).on("click", function (event) {
        sidebar.classList.remove("show");
        overlay.style.display = "none";
        content.classList.remove("blur");
    })

})

async function fetchQuestionDetailHtml(entityId) {
    try {
        const url = `${MODULE_URL}questions/detail/${entityId}`;
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        return response.text();
    } catch (error) {
        console.error('Failed to fetch order details:', error);
        return `<p>Error loading order details. Please try again later.</p>`;
    }
}