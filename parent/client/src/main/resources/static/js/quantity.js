"use strict";

$(function() {
    initListeners();
})

function initListeners() {
    initQuantityButtonListener();
}

function initQuantityButtonListener() {
    $("#button-minus, #button-plus").on("click", function() {
        const $input = $("#quantity-input");
        const value = Number($input.val());
        const $minusIcon = $("#button-minus i");
        const $plusIcon = $("#button-plus i");

        if(this.id === 'button-minus') {
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

    })
}