$(function() {
    initListeners();
})

function initListeners() {
    initQuantityButtonListener();
    setInitialButtonStates();
}

function initQuantityButtonListener() {
    $(".container-md").on("click",".quantity .quantity-minus, .quantity .quantity-plus", function() {
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

/**
 * Sets the initial state of the quantity buttons based on the current input values.
 */
function setInitialButtonStates() {
    $(".quantity").each(function() {
        const $container = $(this);
        const $input = $container.find('.quantity-input');
        const value = Number($input.val());
        const $minusIcon = $container.find(".quantity-minus i");
        const $plusIcon = $container.find(".quantity-plus i");

        if (value === 1) {
            $minusIcon.addClass("opacity-25");
        } else if (value === 5) {
            $plusIcon.addClass("opacity-25");
        }
    });
}

