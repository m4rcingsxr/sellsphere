$(document).ready(function () {
    let hideTimeout;
    let hoverTimeout;
    let previousCategory;

    $('.category-item').hover(
        function () {
            clearTimeout(hoverTimeout);

            hoverTimeout = setTimeout(() => {
                clearTimeout(hideTimeout);

                $('#categoryList').removeClass('d-none');

                if (previousCategory) {
                    $(`#${previousCategory}`).addClass('d-none');
                }

                const categoryId = this.dataset.id;
                previousCategory = categoryId;

                $(`#${categoryId}`).removeClass('d-none');
            }, 300);
        },
        function () {
            clearTimeout(hoverTimeout);

            hideTimeout = setTimeout(function () {
                $('#categoryList').addClass('d-none');

                if (previousCategory) {
                    $(`#${previousCategory}`).addClass('d-none');
                }
            }, 300);
        }
    );

    $('#categoryList').hover(
        function () {
            clearTimeout(hideTimeout);
        },
        function () {
            hideTimeout = setTimeout(function () {
                $('#categoryList').addClass('d-none');
            }, 300);
        }
    );

    $('#categoryList, .category-item').mouseenter(function () {
        clearTimeout(hideTimeout);
    });

});