"use strict";

function initializeRangeSlider(lowerId, upperId, lowerValueId, upperValueId) {
    $(document).ready(function () {
        $(`#${lowerId}`).on('input change', function () {
            let lowerVal = parseInt($(this).val());
            let upperVal = parseInt($(`#${upperId}`).val());
            if (lowerVal > upperVal - 1) {
                $(this).val(upperVal - 1);
                lowerVal = upperVal - 1;
            }
            $(`#${lowerId}`).val(lowerVal);
            $(`#${lowerValueId}`).val(lowerVal);
        });
        $(`#${upperId}`).on('input change', function () {
            let upperVal = parseInt($(this).val());
            let lowerVal = parseInt($(`#${lowerId}`).val());
            if (upperVal < lowerVal + 1) {
                $(this).val(lowerVal + 1);
                upperVal = lowerVal + 1;
            }
            $(`#${upperId}`).val(upperVal);
            $(`#${upperValueId}`).val(upperVal);
        });
    });

}



