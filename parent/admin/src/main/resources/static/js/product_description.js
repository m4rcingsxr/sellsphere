"use strict";

$(function () {
    // Initialize TinyMCE editors when the document is ready
    initEditors();
});

/**
 * Initializes the TinyMCE editors with specified settings.
 */
function initEditors() {
    // Common settings for both editors
    const editorSettings = {
        skin: "sellsphere", // Custom skin
        content_css: "sellsphere", // Custom CSS
        plugins: ['autosave', 'fullscreen', 'image', 'code', 'lists'], // Plugins to include
        toolbar: 'undo | redo | blocks | numlist bullist | formatselect | fontselect | bold italic strikethrough forecolor backcolor formatpainter | alignleft aligncenter alignright alignjustify | numlist | fullscreen | image | code', // Toolbar configuration
        fullscreen_native: true, // Enable native fullscreen
        images_upload_handler: uploadImageHandler, // Custom image upload handler
        setup: setupEditor // Setup function to run when the editor is initialized
    };

    // Initialize the short description editor
    tinymce.init({
        selector: "#shortDescription", // Selector for the short description field
        ...editorSettings
    });

    // Initialize the full description editor
    tinymce.init({
        selector: '#fullDescription', // Selector for the full description field
        ...editorSettings
    });
}

/**
 * Handles the image upload process for TinyMCE.
 * @param {BlobInfo} blobInfo - The blob information of the image being uploaded.
 * @param {Function} progress - The progress function.
 * @returns {Promise<string>} - A promise that resolves to the image URL.
 */
function uploadImageHandler(blobInfo, progress) {
    return new Promise((resolve, reject) => {
        const formData = new FormData();
        const saltedFilename = generateSaltedFilename(blobInfo.filename());
        formData.append('file', blobInfo.blob(), saltedFilename);

        fetch(`${MODULE_URL}descriptions/upload`, {
            method: 'POST',
            body: formData,
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': $("input[name='_csrf']").val(),
            }
        }).then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    reject('HTTP Error: ' + response.status + ' - ' + text);
                });
            }
            return response.json();
        }).then(json => {
            if (!json || typeof json.location !== 'string') {
                reject('Invalid JSON: ' + JSON.stringify(json));
            } else {
                resolve(json.location);
            }
        }).catch(error => {
            reject('Image upload failed due to a fetch error: ' + error.message);
        });
    });
}

/**
 * Generates a salted filename by appending a timestamp to the original filename.
 * @param {string} filename - The original filename.
 * @returns {string} - The salted filename.
 */
function generateSaltedFilename(filename) {
    const timestamp = Date.now();
    const extension = filename.substring(filename.lastIndexOf('.'));
    const name = filename.substring(0, filename.lastIndexOf('.'));
    return `${name}_${timestamp}${extension}`;
}

/**
 * Sets up the TinyMCE editor with initial image handling logic.
 * @param {Editor} editor - The TinyMCE editor instance.
 */
function setupEditor(editor) {
    let initialImages = new Set();

    editor.on('init', function () {
        // Gather initial images when the editor is initialized
        gatherInitialImages(editor, initialImages);
    });

    editor.on('NodeChange', function () {
        // Handle changes in images when the content changes
        handleImageChanges(editor, initialImages);
    });
}

/**
 * Gathers the initial set of images in the editor content.
 * @param {Editor} editor - The TinyMCE editor instance.
 * @param {Set} initialImages - The set to store initial images.
 */
function gatherInitialImages(editor, initialImages) {
    const images = editor.getDoc().querySelectorAll('img');
    images.forEach(img => {
        initialImages.add(img.src);
    });
}

/**
 * Handles changes in images, deleting any removed images from the server.
 * @param {Editor} editor - The TinyMCE editor instance.
 * @param {Set} initialImages - The set of initial images.
 */
function handleImageChanges(editor, initialImages) {
    const currentImages = new Set();
    const images = editor.getDoc().querySelectorAll('img');

    images.forEach(img => {
        currentImages.add(img.src);
    });

    initialImages.forEach(src => {
        if (!currentImages.has(src)) {
            deleteImage(src);
        }
    });

    initialImages.clear();
    currentImages.forEach(img => initialImages.add(img));
}

/**
 * Deletes an image from the server.
 * @param {string} src - The source URL of the image to delete.
 */
function deleteImage(src) {
    const fileName = src.substring(src.lastIndexOf('/') + 1);

    fetch(`${MODULE_URL}descriptions/delete`, {
        method: 'POST',
        body: JSON.stringify({ "fileName": fileName }),
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': $("input[name='_csrf']").val(),
        }
    }).then(response => {
        if (!response.ok) {
            console.error('HTTP Error: ' + response.status);
        }
    }).catch(error => {
        console.error('Delete image failed due to a fetch error: ' + error.message);
    });
}
