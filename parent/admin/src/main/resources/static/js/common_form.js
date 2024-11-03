$(document).ready(() => {
    // Get the form element by its ID. If not found, log an error and exit.
    const form = $("#mainForm");
    if (!form.length) {
        console.error("mainForm not found.");
        return;
    }

    const fileInput = form.find('input[name="newImage"]');
    const previewImage = $("#previewImage");
    if (!fileInput.length || !previewImage.length) {
        console.error("Required elements (fileInput or previewImage) not found.");
        return;
    }

    // Attach a 'change' event listener to the file input to handle file selection.
    fileInput.on("change", handleFileChange);
});

/**
 * Handles the file input change event by compressing the selected image file,
 * updating the file input with the compressed image, and previewing the image.
 *
 * @param {Event} event - The file input change event.
 */
const handleFileChange = async (event) => {
    const file = event.target.files[0]; // Get the first selected file.

    // Ensure the file exists and meets the maximum file size constraint.
    if (file && file.size <= MAX_FILE_SIZE) {
        try {
            showImageSpinner();

            // Compress the selected image with given quality, width, and height.
            const compressedFile = await compressImage(file, QUALITY, WIDTH, HEIGHT);

            // Update the file input with the newly compressed image.
            updateFileInput(compressedFile);

            // Show a preview of the compressed image to the user.
            previewCompressedImage(compressedFile);
        } catch (error) {
            // Log the error if something goes wrong during the compression process.
            console.error("Error during image compression:", error.response);
            showErrorModal(error.response);
        } finally {
            hideImageSpinner();
        }
    }
};

/**
 * Displays a spinner to indicate the image is being processed.
 * The spinner is appended to the same parent element as the preview image.
 */
const showImageSpinner = () => {
    // Create a spinner element using jQuery.
    const spinner = $("<div>").addClass("spinner");

    // Append the spinner to the parent of the preview image.
    $("#previewImage").parent().append(spinner);
};

/**
 * Hides the spinner by removing it from the DOM once processing is complete.
 */
const hideImageSpinner = () => {
    $(".spinner").remove();
};

/**
 * Updates the file input with the newly compressed image.
 * This replaces the selected file with the compressed one in the input field.
 *
 * @param {File} compressedFile - The compressed image file.
 */
const updateFileInput = (compressedFile) => {
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(compressedFile);

    $('input[name="newImage"]')[0].files = dataTransfer.files;
};

/**
 * Displays a preview of the compressed image by updating the source of the preview image element.
 *
 * @param {File} compressedFile - The compressed image file to preview.
 */
const previewCompressedImage = (compressedFile) => {
    const reader = new FileReader();

    // Define the 'onload' event to display the image preview once it's loaded.
    reader.onload = (e) => {
        $("#previewImage")
            .attr("src", e.target.result)
            .addClass("loaded"); // Add a class to indicate that the image is loaded.
    };

    // Read the compressed file as a Data URL to display in the preview.
    reader.readAsDataURL(compressedFile);
};
