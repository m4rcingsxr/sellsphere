"use strict";

/**
 * Initializes global variable to keep track of the current index for image inputs.
 */
let imageCurrentIndex;

/**
 * Initializes the script once the DOM is fully loaded. Counts existing hidden image inputs
 * to set the starting index for new images and sets up event listeners.
 */
$(document).ready(() => {
    const inputs = $("input[type='hidden']").filter((_, element) => element.name.match(/^images\[\d+\]\.id$/));
    imageCurrentIndex = inputs.length;
    initializeImageEventListeners();
    refreshDropAreaListeners(); // Apply drag-and-drop functionality
    updateImageTitles(); // Set titles for main image and extra images
});

/**
 * Sets up event listeners for managing extra images in the form.
 * - Adds new file input fields for extra images.
 * - Handles file selection for extra images to display a preview and add hidden inputs.
 * - Allows for the removal of added extra image sections.
 */
function initializeImageEventListeners() {
    // Event delegation: Target any dynamically added remove buttons within #imageContainer
    $("#imageContainer").on('change', 'input[name="extraImages"]', extraImageHandler)
        .on('click', '.remove-image', handleExtraImageRemoval); // Updated selector for remove button

    // Event listener for adding new file input
    $("#newFileInput").click(handleNewFileInputClick);
}

/**
 * Handles the file selection or drop event for extra images, displaying an image preview
 * and adding corresponding hidden inputs for the image's details.
 * @param {Event} event - The file selection or drop event.
 */
async function extraImageHandler(event) {
    const file = event.target.files[0];
    const container = $(event.target).parent();
    $(event.target).hide();
    container.addClass("d-flex");

    if (file && file.size <= MAX_FILE_SIZE) {
        try {
            showSpinnerForExtraImage(container);
            const compressedFile = await compressImage(file, QUALITY, WIDTH, HEIGHT);
            const reader = new FileReader();
            reader.onload = e => {
                const html = getImageSectionHtml(compressedFile.name, e.target.result, imageCurrentIndex);
                container.find(".card").remove();
                container.append(html);
                updateFileInputWithCompressedFile(event.target, compressedFile); // Ensure input is updated
                imageCurrentIndex++;
                updateImageTitles(); // Update titles after new images
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
 * @returns {string} The HTML string for the new image section.
 */
function getImageSectionHtml(fileName, src, index) {
    const isMainImage = index > 0; // Main image is the first one (index 0)
    return `
        <div class="card shadow-sm position-relative mt-auto">
            <div class="card-body">
                <h5 class="card-title">${isMainImage ? 'Main image' : 'Extra image'}</h5>
            </div>
            <div class="entity-image-container" style="width: 100%; max-height: 300px;">
                <img id="previewImage${index}" src="${src}" class="p-2 entity-image" style="object-fit: cover;">
            </div>
            ${!isMainImage ? `
                <a href="#" class="link-primary position-absolute top-0 end-0 p-3 remove-image">
                    <i class="fa-solid fa-xmark"></i>
                </a>` : ''}
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
    input.files = dataTransfer.files; // Ensure the input's files property is updated
}

/**
 * Dynamically adds a new file input field for uploading an extra image.
 */
function handleNewFileInputClick() {
    const html = `
        <div class="col-sm-4 position-relative drop-area">
            <input name="extraImages" type="file" class="form-control mb-2"/>
        </div>
    `;
    $(this).parent().before(html);
    refreshDropAreaListeners(); // Apply drag-and-drop functionality to new drop area
}

/**
 * Handles the removal of an extra image section, including its preview and inputs,
 * and updates the indices of remaining image inputs accordingly.
 * @param {Event} event - The click event on the remove link.
 */
function handleExtraImageRemoval(event) {
    event.preventDefault();
    const $target = $(event.currentTarget);

    // Correctly target the parent container of the image card and remove it
    $target.closest(".col-sm-4").remove(); // This should remove the entire image section container

    imageCurrentIndex--;
    refreshImageIndexes();  // Update input field indices after removal
    updateImageTitles();    // Update titles after removing images
}

/**
 * Refreshes the indices of all image-related input elements in the form to maintain
 * sequential order. This is called after adding or removing image inputs.
 */
function refreshImageIndexes() {
    ['id', 'name', 'product'].forEach(attribute => {
        refreshImageInputs(attribute);
    });
}

function refreshImageInputs(attributeType) {
    const pattern = new RegExp(`^images\\[\\d+\\]\\.${attributeType}$`);
    $('input').filter((_, element) => pattern.test($(element).attr('name')))
        .each((index, element) => {
            const id = `images${index}.${attributeType}`;
            const name = `images[${index}].${attributeType}`;
            $(element).attr({id, name});
        });
}

/**
 * Sets up drag-and-drop functionality for file inputs inside .drop-area elements.
 */
function refreshDropAreaListeners() {
    let dropAreas = document.querySelectorAll(".drop-area");

    // Prevent default behaviors for drag events
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        dropAreas.forEach(dropArea => {
            dropArea.addEventListener(eventName, preventDefaults);
        });
    });

    // Highlight drop area when dragging file over it
    ['dragenter', 'dragover'].forEach(eventName => {
        dropAreas.forEach(dropArea => {
            dropArea.addEventListener(eventName, () => dropArea.classList.add('highlight'));
        });
    });

    // Remove highlight when file leaves or is dropped
    ['dragleave', 'drop'].forEach(eventName => {
        dropAreas.forEach(dropArea => {
            dropArea.addEventListener(eventName, () => dropArea.classList.remove('highlight'));
        });
    });

    // Handle file drop inside the drop area and update the file input
    dropAreas.forEach(dropArea => {
        const fileInput = dropArea.querySelector('input[type="file"]');
        dropArea.addEventListener('drop', (e) => {
            const files = e.dataTransfer.files;
            handleFiles(files, fileInput); // Pass the file input for updating
        });
    });
}

function handleFiles(files, fileInput) {
    const dataTransfer = new DataTransfer(); // Used to update the input's files
    [...files].forEach(file => {
        compressImage(file, QUALITY, WIDTH, HEIGHT) // Compress each file
            .then(compressedFile => {
                dataTransfer.items.add(compressedFile); // Add compressed file to the DataTransfer object
                const reader = new FileReader();
                reader.onload = e => {
                    const container = $(fileInput).parent();
                    const html = getImageSectionHtml(compressedFile.name, e.target.result, imageCurrentIndex);
                    container.find(".card").remove();
                    container.append(html);
                    imageCurrentIndex++;
                    updateImageTitles(); // Update titles after new images
                };
                reader.readAsDataURL(compressedFile); // Preview the compressed image
                updateFileInputWithCompressedFile(fileInput, compressedFile); // Update file input
            })
            .catch(error => console.error("Error during image compression:", error));
    });
}

/**
 * Shows a spinner while processing for an extra image.
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
 */
const hideSpinnerForExtraImage = (container) => {
    const spinner = container.find(".spinner");
    if (spinner) {
        spinner.remove();
    }
};

/**
 * Updates the image titles, ensuring the first image is marked as "Main image"
 * and others as "Extra image."
 */
function updateImageTitles() {
    $("#imageContainer .card-title").each((index, element) => {
        if (index === 0) {
            $(element).text("Main image");
        } else {
            $(element).text("Extra image");
        }
    });
}

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}
