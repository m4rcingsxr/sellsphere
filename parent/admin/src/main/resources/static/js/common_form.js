/**
 * Script responsible for compressing images and loading the preview.
 * This script requires the following elements and variables:
 *
 * - An input element with the name attribute set to "newImage".
 *   Example: <input type="file" name="newImage" />
 * - An image element with the id attribute set to "previewImage" where the selected image will be previewed.
 *   Example: <img id="previewImage" src="" alt="Image preview" class="entity-image" />
 * - A constant MAX_FILE_SIZE that defines the maximum allowed file size.
 *   Example: const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
 * - Constants for WIDTH, HEIGHT, and QUALITY to specify the dimensions and quality of the compressed image.
 *   Example: const WIDTH = 800; const HEIGHT = 800; const QUALITY = 0.8;
 */
$(document).ready(() => {
    const form = document.getElementById("mainForm");
    if (!form) {
        console.error("mainForm not found.");
        return;
    }

    const fileInput = form.querySelector('input[name="newImage"]');
    const previewImage = document.getElementById("previewImage");
    if (!fileInput || !previewImage) {
        console.error("Required elements (fileInput or previewImage) not found.");
        return;
    }

    fileInput.addEventListener("change", handleFileChange);
});

/**
 * Handles the file input change event.
 */
const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (file && file.size <= MAX_FILE_SIZE) {
        try {
            showSpinner();

            const data = {
                file: file,
                width: WIDTH,
                height: HEIGHT,
                quality: QUALITY
            };
            const compressedImageBlob = await ajaxUtil.postBlob(`${MODULE_URL}upload`, data);
            const compressedFile = new File([compressedImageBlob], file.name, {type: "image/jpeg"});

            updateFileInput(compressedFile);
            previewCompressedImage(compressedFile);
        } catch (error) {
            console.error("Error during image compression:", error.response);
            showErrorModal(error.response);
        } finally {
            hideSpinner();
        }
    }

    // else - handled by validation
};

/**
 * Shows a spinner while processing.
 */
const showSpinner = () => {
    const spinner = document.createElement("div");
    spinner.classList.add("spinner");
    document.getElementById("previewImage").parentNode.appendChild(spinner);
};

/**
 * Hides the spinner after processing.
 */
const hideSpinner = () => {
    const spinner = document.querySelector(".spinner");
    if (spinner) {
        spinner.remove();
    }
};

/**
 * Updates the file input with the compressed image.
 *
 * @param {File} compressedFile - The compressed file.
 */
const updateFileInput = (compressedFile) => {
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(compressedFile);
    document.querySelector('input[name="newImage"]').files = dataTransfer.files;
};

/**
 * Previews the compressed image.
 *
 * @param {File} compressedFile - The compressed file.
 */
const previewCompressedImage = (compressedFile) => {
    const reader = new FileReader();
    reader.onload = (e) => {
        const previewImage = document.getElementById("previewImage");
        previewImage.src = e.target.result;
        previewImage.classList.add("loaded");
    };
    reader.readAsDataURL(compressedFile);
};