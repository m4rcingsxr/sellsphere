"use strict";

function initEditor(editorId) {
    const editorSettings = {
        skin: "sellsphere",
        content_css: "sellsphere",
        plugins: ['template','autosave','visualblocks', 'fullscreen', 'image', 'code', 'lists'],
        toolbar: 'undo | redo | visualblocks | numlist bullist | formatselect | fontselect | bold italic strikethrough forecolor backcolor formatpainter | alignleft aligncenter alignright alignjustify | numlist | fullscreen | image | code | 2-col-container',
        fullscreen_native: true,
        image_dimensions: false,
        image_class_list: [
            {title: 'Responsive', value: 'w-100 img-fluid full-description-image lazy'}
        ],
        images_upload_handler: uploadImageHandler,
        setup: setupEditor
    };

    tinymce.init({
        selector: `#${editorId}`,
        ...editorSettings
    });
}

/**
 * Handles the image upload process for TinyMCE.
 * @param {BlobInfo} blobInfo - The blob information of the image being uploaded.
 * @param {Function} progress - The progress function.
 * @returns {Promise<string>} - A promise that resolves to the image URL.
 */
async function uploadImageHandler(blobInfo, progress) {
    try {
        const file = new File([blobInfo.blob()], blobInfo.filename(), { type: blobInfo.blob().type });

        const compressedFile = await compressImage(file, 1.0);
        const formData = new FormData();
        const saltedFilename = generateSaltedFilename(blobInfo.filename());
        formData.append('file', compressedFile, saltedFilename);

        const response = await fetch(`${MODULE_URL}editor/upload`, {
            method: 'POST',
            body: formData,
            credentials: 'same-origin',
            headers: {
                'X-CSRF-TOKEN': $("input[name='_csrf']").val(),
            }
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error('HTTP Error: ' + response.status + ' - ' + text);
        }

        const json = await response.json();
        if (!json || typeof json.location !== 'string') {
            throw new Error('Invalid JSON: ' + JSON.stringify(json));
        }

        return json.location;
    } catch (error) {
        console.error('Image upload failed:', error.message);
        throw error;
    }
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

    editor.on('NodeChange', function (e) {
        if (e.element.tagName === "IMG") {
            let img = e.element;
            if (!img.getAttribute('data-original')) {
                let src = img.getAttribute("src");
                img.setAttribute("data-src", src);

                // Wrap the image in a container with a spinner
                let wrapper = document.createElement("div");
                wrapper.classList.add("position-relative");
                img.parentNode.insertBefore(wrapper, img);
                wrapper.appendChild(img);

                // After the image has been inserted, set it back to the original source
                img.onload = () => {
                    img.setAttribute("src", originalSrc);
                };
            }
        }

        // Handle changes in images when the content changes
        handleImageChanges(editor, initialImages);
    });

    editor.ui.registry.addButton('2-col-container', {
        text: 'Insert Row',
        onAction: function () {
            const content = `
                <div class="row">
                    <div class="col-sm-6">
                        Column 1 content...
                    </div>
                    <div class="col-sm-6">
                        Column 2 content...
                    </div>
                </div>
            `;
            editor.insertContent(content);
        }
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

    fetch(`${MODULE_URL}editor/delete`, {
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
