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
 * @param {Event} event - The file input change event.
 */
const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (file && file.size <= MAX_FILE_SIZE) {
        try {
            showSpinner();
            const compressedFile = await compressImage(file, WIDTH, HEIGHT, QUALITY);
            updateFileInput(compressedFile);
            previewCompressedImage(compressedFile);
        } catch (error) {
            console.error("Error during image compression:", error.response);
            showErrorModal(error.response);
        } finally {
            hideSpinner();
        }
    }
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
 * @param {File} compressedFile - The compressed file.
 */
const updateFileInput = (compressedFile) => {
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(compressedFile);
    document.querySelector('input[name="newImage"]').files = dataTransfer.files;
};

/**
 * Previews the compressed image.
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