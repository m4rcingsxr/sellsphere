"use strict";

/**
 * Initializes global variable to keep track of the current index for image inputs.
 */
let imageCurrentIndex;

/**
 * Initializes the script once the DOM is fully loaded. Counts existing hidden image inputs
 * to set the starting index for new images.
 */
$(document).ready(() => {
    const inputs = $("input[type='hidden']").filter((_, element) => element.name.match(/^images\[\d+\]\.id$/));
    imageCurrentIndex = inputs.length;
    initializeImageEventListeners();
});

/**
 * Sets up event listeners for managing extra images in the form.
 * - Adds new file input fields for extra images.
 * - Handles file selection for extra images to display a preview and add hidden inputs.
 * - Allows for the removal of added extra image sections.
 */
function initializeImageEventListeners() {
    $("#imageContainer").on('change', 'input[name="extraImages"]', extraImageHandler)
        .on('click', 'a', handleExtraImageRemoval);

    $("#newFileInput").click(handleNewFileInputClick);
}

/**
 * Handles the file selection event for extra images, displaying an image preview
 * and adding corresponding hidden inputs for the image's details.
 * @param {Event} event - The file selection event.
 */
async function extraImageHandler(event) {
    const file = event.target.files[0];
    const container = $(event.target).parent();
    $(event.target).hide();
    container.addClass("d-flex");

    if (file && file.size <= MAX_FILE_SIZE) {
        try {
            showSpinnerForExtraImage(container);
            const compressedFile = await compressImage(file, WIDTH, HEIGHT, QUALITY);
            const reader = new FileReader();
            reader.onload = e => {
                const html = getImageSectionHtml(compressedFile.name, e.target.result, imageCurrentIndex);
                container.find(".card").remove();
                container.append(html);
                updateFileInputWithCompressedFile(event.target, compressedFile);
                imageCurrentIndex++;
            };
            reader.readAsDataURL(compressedFile);
        } catch (error) {
            console.error("Error during image compression:", error);
        } finally {
            hideSpinnerForExtraImage(container);
        }
    } else {
        console.warn("The selected image is not valid.");
    }
}

/**
 * Generates HTML for a new extra image section.
 * @param {string} fileName - The name of rendered image.
 * @param {string} src - The src for the image.
 * @param {number} index - The current index for the new detail section.
 * @returns {string} The HTML string for the new extra image section.
 */
function getImageSectionHtml(fileName, src, index) {
    return `
            <div class="card shadow-sm position-relative mt-auto">
                    <div class="card-body">
                        <h5 class="card-title">Extra image</h5>
                    </div>
                    <div class="entity-image-container" style="width: 100%; height: 300px">
                        <img id="previewImage${index}" src="${src}" class="p-2 entity-image" style="object-fit: cover;">
                    </div>
                    <a href="#" class="link-primary position-absolute top-0 end-0 p-3"><i class="fa-solid fa-xmark"></i></a>
            </div>
    `;
}

/**
 * Updates the file input field with the compressed file.
 * @param {HTMLInputElement} input - The original file input element.
 * @param {File} compressedFile - The compressed file.
 */
function updateFileInputWithCompressedFile(input, compressedFile) {
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(compressedFile);
    input.files = dataTransfer.files;
}

/**
 * Dynamically adds a new file input field for uploading an extra image.
 */
function handleNewFileInputClick() {
    const html = `
        <div class="col-sm-4 position-relative">
            <input name="extraImages" type="file" class="form-control mb-2"/>
        </div>
    `;
    $(this).parent().before(html);
}

/**
 * Handles the removal of an extra image section, including its preview and inputs,
 * and updates the indices of remaining image inputs accordingly.
 * @param {Event} event - The click event on the remove link.
 */
function handleExtraImageRemoval(event) {
    const $target = $(event.currentTarget);
    if ($target.find('i.fa-xmark').length > 0) {
        $target.parent().parent().remove();
        imageCurrentIndex--;
        refreshImageIndexes();
    }
}


/**
 * Shows a spinner while processing for an extra image.
 * @param {Object} container - The container where the spinner should be shown.
 */
const showSpinnerForExtraImage = (container) => {
    const spinner = document.createElement("div");
    spinner.classList.add("spinner");

    // Set position style to ensure spinner displays correctly
    spinner.style.position = "absolute";
    spinner.style.top = "50%";
    spinner.style.left = "50%";
    spinner.style.transform = "translate(-50%, -50%)";

    container.append(spinner);
};

/**
 * Hides the spinner after processing for an extra image.
 * @param {Object} container - The container where the spinner should be hidden.
 */
const hideSpinnerForExtraImage = (container) => {
    const spinner = container.find(".spinner");
    if (spinner) {
        spinner.remove();
    }
};

/**
 * Refreshes the indices of all image-related input elements in the form to maintain
 * sequential order. This is called after adding or removing image inputs.
 */
function refreshImageIndexes() {
    ['id', 'name', 'product'].forEach(attribute => {
        refreshImageInputs(attribute);
    });
}

/**
 * Updates the IDs and names of image input elements based on their current index.
 * This ensures the backend receives the images in the correct order with matching indices.
 * @param {string} attributeType - The type of attribute to update (id, name, or product).
 */
function refreshImageInputs(attributeType) {
    const pattern = new RegExp(`^images\\[\\d+\\]\\.${attributeType}$`);
    $('input').filter((_, element) => pattern.test($(element).attr('name')))
        .each((index, element) => {
            const id = `images${index}.${attributeType}`;
            const name = `images[${index}].${attributeType}`;
            $(element).attr({id, name});
        });
}