$(function () {
    renderReCaptcha();
})

function handleValidations(errors) {
    const recaptchaError = document.getElementById('recaptcha-error');
    const submitBtn = document.getElementById('submit-button');
    $(submitBtn).prop("disabled", true);
    // Clear existing error classes and messages
    document.querySelectorAll('.is-invalid').forEach(function (el) {
        el.classList.remove('is-invalid');
    });
    recaptchaError.style.display = 'none';
    document.querySelectorAll('.invalid-feedback').forEach(function (el) {
        el.style.display = 'none';
    });

    // Display validation errors, if any
    if (errors.length > 0) {
        errors.forEach(function (error) {
            const field = document.querySelector(`[name="${error.name}"]`);
            if (field) {
                field.classList.add('is-invalid');
                const feedback = field.nextElementSibling;
                if (feedback && feedback.classList.contains('invalid-feedback')) {
                    feedback.style.display = 'block';
                    feedback.innerText = error.message;
                }
            }
        });

        if (grecaptcha.getResponse().length == 0) {
            recaptchaError.style.display = 'block';
        }

        // Scroll to the first invalid field
        document.querySelector('.is-invalid').scrollIntoView({behavior: 'smooth', block: 'center'});
        $(submitBtn).prop("disabled", false);
    }
}

function renderReCaptcha() {
    showFullScreenSpinner();
    if ($("#recaptcha-container").length > 0) {
        grecaptcha.ready(function () {
            // Render reCAPTCHA widget
            grecaptcha.render('recaptcha-container', {
                'sitekey': `${config.recaptchaSiteKey}`
            });


        });
    }

    if ($("#recaptcha-review").length > 0) {
        grecaptcha.ready(function () {
            // Render reCAPTCHA widget
            grecaptcha.render('recaptcha-review', {
                'sitekey': `${config.recaptchaSiteKey}`
            });


        });
    }

    if ($("#recaptcha-question").length > 0) {
        grecaptcha.ready(function () {
            // Render reCAPTCHA widget
            grecaptcha.render('recaptcha-question', {
                'sitekey': `${config.recaptchaSiteKey}`
            });


        });
    }
    hideFullScreenSpinner();
}