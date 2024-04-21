"use strict";

$(function () {
    initializeValidators();
    configureFormValidation();
});

/**
 * Initializes custom validators for form validation.
 *
 * Requirements:
 * 1. A file input element with the name attribute set to "newImage".
 *    Example: <input type="file" name="newImage" />
 * 2. An image element with the id attribute set to "previewImage" where the selected image will be previewed.
 *    Example: <img id="previewImage" src="" alt="Image preview" />
 * 3. A constant MAX_FILE_SIZE that defines the maximum allowed file size.
 *    Example: const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
 * 4. An input element with the name attribute "id" for checking if the user is new or existing.
 *    Example: <input type="hidden" name="id" value="" />
 */
function initializeValidators() {
    addPasswordValidators();
    addBasicValidators();
    addFileValidators();
    addCategoryIconValidator();
}

function addCategoryIconValidator() {
    $.validator.addMethod('categoryIcon', function(value, element) {
        const categoryId = Number($("#parent").val());
        const categoryIcon = $("#categoryIcon\\.iconPath").val();

        console.log(categoryIcon, categoryId);

        return categoryId !== -1 || categoryId === -1 && categoryIcon;
    }, 'Category icon must be presented for root categories.')
}

/**
 * Adds custom validators related to passwords.
 */
function addPasswordValidators() {
    $.validator.addMethod('mypassword', function (value, element) {
        const isExistingUser = $('input[name="id"]').val().trim() !== '';
        // Allow existing users to bypass the password check
        return isExistingUser || /^(?=.*[A-Z])(?=.*\d)[A-Za-z\d]{8,72}$/.test(value);
    }, 'Your password must be 8-72 characters long and include at least one uppercase letter and one digit.');

    $.validator.addMethod("passwordRequired", function (value) {
        const isNewUser = $('input[name="id"]').val().trim() === '';
        return !isNewUser || value.trim() !== '';
    }, "A password is required for creating a new user account.");
}

/**
 * Adds basic validators for alphanumeric characters and checkboxes.
 */
function addBasicValidators() {
    $.validator.addMethod('alphanumeric', function (value) {
        return /^[A-Za-z\d]+$/.test(value);
    }, 'Please use only alphanumeric characters (letters and numbers).');

    $.validator.addMethod("oneChecked", function (value, element) {
        return $(`input[name="${element.name}"]:checked`).length >= 1;
    }, 'Please select at least one option.');

    $.validator.addMethod("notZero", function(value, element) {
        return this.optional(element) || parseFloat(value) !== 0.0;
    }, "The value must not be 0.0.");
}

/**
 * Adds validators for file input fields.
 */
function addFileValidators() {
    $.validator.addMethod("imageRequired", function (value, element, fieldName) {
        const hasExistingValue = $(`input[name="${fieldName}"]`).val();
        const hasNewFile = element.files && element.files.length > 0;
        return hasExistingValue || hasNewFile;
    }, `An image file is required`);

    $.validator.addMethod("maxImageSize", function (value, element) {
        return element.files.length === 0 || element.files[0].size <= MAX_FILE_SIZE;
    }, `File must be less than ${MAX_FILE_SIZE} bytes.`);
}

function configureFormValidation() {
    validateForm("#mainForm");

    // Apply dynamic rules if they exist
    if (Array.isArray(dynamicRules) && dynamicRules.length > 0) {
        applyDynamicValidationRules();
    }
}

/**
 * Validates a form with the given selector and optional submit handler.
 * @param {string} formSelector - The selector for the form.
 * @param {function} [submitHandler] - The submit handler function.
 */
function validateForm(formSelector, submitHandler) {
    $(formSelector).validate({
        ignore: [],
        rules: validationRules,
        messages: validationMessages,
        errorElement: "em",
        errorPlacement: errorPlacementHandler,
        highlight: highlightHandler,
        unhighlight: unhighlightHandler,
        submitHandler: submitHandler
    });
}

/**
 * Handles element highlighting on validation error.
 * @param {HTMLElement} element - The form element.
 */
function highlightHandler(element) {
    toggleValidationClasses(element, true);
}

/**
 * Handles element unhighlighting on validation success.
 * @param {HTMLElement} element - The form element.
 */
function unhighlightHandler(element) {
    toggleValidationClasses(element, false);
}

/**
 * Toggles validation classes based on validity.
 * @param {HTMLElement} element - The form element.
 * @param {boolean} isInvalid - Whether the element is invalid.
 */
function toggleValidationClasses(element, isInvalid) {
    const elementName = element.name;
    const action = isInvalid ? "addClass" : "removeClass";
    const oppositeAction = isInvalid ? "removeClass" : "addClass";

    if (element.type === "checkbox") {
        $(`input[name="${elementName}"]`).each(function() {
            $(this)[action]("is-invalid")[oppositeAction]("is-valid");
        });
    } else {
        $(element)[action]("is-invalid")[oppositeAction]("is-valid");
    }
}

/**
 * Handles error placement for validation errors.
 * @param {jQuery} error - The jQuery error element.
 * @param {jQuery} element - The jQuery form element.
 */
function errorPlacementHandler(error, element) {
    let placement;

    if (element.prop("type") === "checkbox") {
        placement = element.closest(".col-sm-8").children().last();
    } else if (element.parent().hasClass("input-group")) {
        placement = element.parent();
    } else {
        placement = element;
    }

    console.log(placement);

    error.addClass("text-danger").insertAfter(placement);
}

/**
 * Dynamically applies validation rules.
 */
function applyDynamicValidationRules() {
    dynamicRules.forEach(config => {
        $(config.selector).each(function() {
            if (!$(this).rules().required) { // Avoid duplicate rule application
                $(this).rules("add", config.rules);
            }
        });
    });
}

function handleRemoteValidationError(jqXHR) {
    const errorResponse = jqXHR.responseJSON;
    console.error("Error during uniqueness check")
    showErrorModal(errorResponse);
}
