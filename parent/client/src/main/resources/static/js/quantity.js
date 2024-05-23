$(function() {
    initListeners();
})

function initListeners() {
    initQuantityButtonListener();
}

function initQuantityButtonListener() {
    $(".quantity .quantity-minus, .quantity .quantity-plus").on("click", function() {
        const $container = $(this).closest('.quantity');
        const $input = $container.find('.quantity-input');
        const value = Number($input.val());
        const $minusIcon = $container.find(".quantity-minus i");
        const $plusIcon = $container.find(".quantity-plus i");

        if($(this).hasClass('quantity-minus')) {
            if(value === 1) {
                return;
            }
            $input.val(value - 1);
            $plusIcon.removeClass("opacity-25");

            if(Number($input.val()) === 1) {
                $minusIcon.addClass("opacity-25");
            }
        } else {
            if(value === 5) {
                return;
            }
            $input.val(value + 1);
            $minusIcon.removeClass("opacity-25");

            if(Number($input.val()) === 5) {
                $plusIcon.addClass("opacity-25");
            }
        }
    });
}
