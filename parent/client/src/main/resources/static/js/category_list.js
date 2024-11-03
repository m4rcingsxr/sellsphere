$(document).ready(function () {
    let hideTimeout;
    let hoverTimeout;
    let previousCategory;

    $('.category-item').hover(
        function () {
            // Clear any existing hover timeout
            clearTimeout(hoverTimeout);

            // Set a delay before showing the category list
            hoverTimeout = setTimeout(() => {
                clearTimeout(hideTimeout);

                $('#categoryList').removeClass('d-none');

                if (previousCategory) {
                    $(`#${previousCategory}`).addClass('d-none');
                }

                const categoryId = $(this).text().trim().replace(/\s+/g, '-'); // Replace spaces with dashes if IDs have spaces
                previousCategory = categoryId;

                $(`#${categoryId}`).removeClass('d-none');
            }, 300); // Adjust the delay as needed
        },
        function () {
            // Clear any existing hover timeout
            clearTimeout(hoverTimeout);

            // Set a delay before hiding the category list
            hideTimeout = setTimeout(function () {
                $('#categoryList').addClass('d-none');

                if (previousCategory) {
                    $(`#${previousCategory}`).addClass('d-none');
                }
            }, 300); // Adjust the delay as needed
        }
    );

    $('#categoryList').hover(
        function () {
            clearTimeout(hideTimeout);
        },
        function () {
            hideTimeout = setTimeout(function () {
                $('#categoryList').addClass('d-none');
            }, 300); // Delay before hiding, adjust as needed
        }
    );

    // Ensure the #categoryList doesn't hide if the mouse moves between #categoryList and .category-item quickly
    $('#categoryList, .category-item').mouseenter(function () {
        clearTimeout(hideTimeout);
    });

});