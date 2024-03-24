"use strict";

$(function() {
    initializeChangeFileInputListener();
});

/**
 * Initializes the change event listener for the file input element.
 * This listener checks the selected file, validates it, and if it's an image,
 * previews it in the specified image element.
 *
 * Requirements:
 * 1. A file input element with the name attribute set to "newImage".
 *    Example: <input type="file" name="newImage" />
 * 2. An image element with the id attribute set to "previewImage" where the selected image will be previewed.
 *    Example: <img id="previewImage" src="" alt="Image preview" />
 * 3. A constant MAX_FILE_SIZE that defines the maximum allowed file size.
 *    Example: const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
 */
function initializeChangeFileInputListener() {
    const fileInput = $('input[name="newImage"]');
    fileInput.on("change", function() {
        const file = this.files[0];
        const allowedImageTypes = ['image/jpeg', 'image/png', 'image/gif'];

        if(file) {
            if (file.size > MAX_FILE_SIZE) {
                console.warn("The selected file is too large.");
                return;
            }

            if (allowedImageTypes.includes(file.type)) {
                const reader = new FileReader();

                reader.onload = function(e) {
                    $("#previewImage").attr("src", e.target.result);
                };

                reader.readAsDataURL(file);
            } else {
                console.warn("Selected file is not an image.");
            }
        } else {
            console.warn("No file has been selected.");
        }
    });
}

