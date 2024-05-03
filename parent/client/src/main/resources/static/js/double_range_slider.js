"use strict";

function initializeRangeSlider(lowerId, upperId, lowerValueId, upperValueId) {
    $(document).ready(function() {
        $(`#${lowerId}`).on('input change', function() {
            var lowerVal = parseInt($(this).val());
            var upperVal = parseInt($(`#${upperId}`).val());
            if (lowerVal > upperVal - 1) {
                $(this).val(upperVal - 1);
                lowerVal = upperVal - 1;
            }
            $(`#${lowerId}`).val(lowerVal); // Update the lower input value
            $(`#${lowerValueId}`).val(lowerVal); // Update the displayed value
        });

        $(`#${upperId}`).on('input change', function() {
            var upperVal = parseInt($(this).val());
            var lowerVal = parseInt($(`#${lowerId}`).val());
            if (upperVal < lowerVal + 1) {
                $(this).val(lowerVal + 1);
                upperVal = lowerVal + 1;
            }
            $(`#${upperId}`).val(upperVal); // Update the upper input value
            $(`#${upperValueId}`).val(upperVal); // Update the displayed value
        });
    });
}

