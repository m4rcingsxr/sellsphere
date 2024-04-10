"use strict";

$(function () {
    initEditors();
});

function initEditors() {
    const editorSettings = {
        skin: "sellsphere",
        content_css: "sellsphere",
        plugins: ['autosave', 'fullscreen', 'image', 'code'],
        toolbar: 'undo | redo | blocks | formatselect | fontselect | bold italic strikethrough forecolor backcolor formatpainter | alignleft aligncenter alignright alignjustify | numlist | fullscreen | image | code',
        fullscreen_native: true,
        images_upload_handler: uploadImageHandler,
        setup: setupEditor
    };

    tinymce.init({
        selector: "#shortDescription",
        ...editorSettings
    });

    tinymce.init({
        selector: '#fullDescription',
        ...editorSettings
    });
}

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

function generateSaltedFilename(filename) {
    const timestamp = Date.now();
    const extension = filename.substring(filename.lastIndexOf('.'));
    const name = filename.substring(0, filename.lastIndexOf('.'));
    return `${name}_${timestamp}${extension}`;
}

function setupEditor(editor) {
    let initialImages = new Set();

    editor.on('init', function () {
        gatherInitialImages(editor, initialImages);
    });

    editor.on('NodeChange', function () {
        handleImageChanges(editor, initialImages);
    });
}

function gatherInitialImages(editor, initialImages) {
    const images = editor.getDoc().querySelectorAll('img');
    images.forEach(img => {
        initialImages.add(img.src);
    });
}

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
