

/**
 * Formats bytes into a human-readable string.
 *
 * @param {number} bytes - The number of bytes.
 * @param {number} [decimals=2] - The number of decimals to include in the formatted string.
 * @returns {string} The formatted byte size string.
 */
const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${sizes[i]}`;
};

/**
 * Displays a modal dialog with the specified content.
 *
 * @param {string} modalId - The ID of the modal element.
 * @param {string} title - The title of the modal.
 * @param {string} content - The content to display in the modal.
 * @param {string} [contentType='text'] - The type of content ('text' or 'html').
 * @param {string} [btnURL] - The URL for the modal's primary button (optional).
 */
const showModalDialog = (modalId, title, content, contentType = 'text', btnURL) => {
    const $modal = $(`#${modalId}`);
    const $modalTitle = $modal.find('.modal-title');
    const $modalBody = $modal.find('.modal-body');

    $modalTitle.text(title);
    $modalBody.empty();

    if (contentType === 'html') {
        $modalBody.html(content);
    } else {
        $modalBody.text(content);
    }

    if (btnURL) {
        $modal.find('.btn-primary').attr('href', btnURL);
    }

    $modal.modal('show');
};

/**
 * Shows an error modal with the provided error response.
 *
 * @param {Object} errorResponse - The error response object.
 */
const showErrorModal = (errorResponse) => {
    let timestamp;
    if(errorResponse.timestamp) {
        timestamp = errorResponse.timestamp;
    } else {
        timestamp = Date.now();
    }

    const date = parseTimestampToDate(timestamp);
    $("#errorModal .modal-footer").text(date);

    const userFriendlyMessage = handleErrorResponse(errorResponse.status, errorResponse.message);
    showModalDialog('errorModal', '⚠ Error occurred', userFriendlyMessage, 'text');
};


/**
 * Returns a user-friendly error message based on the status code.
 *
 * @param {number} status - The HTTP status code.
 * @returns {string} The user-friendly error message.
 */
const handleErrorResponse = (status, message) => {
    let userFriendlyMessage;

    switch (Number(status)) {
        case 400:
            userFriendlyMessage = `Bad Request: ${message || 'The server could not understand the request.'}`;
            return userFriendlyMessage;
        case 401:
            userFriendlyMessage = "Unauthorized: You are not authorized to access this resource.";
            break;
        case 403:
            userFriendlyMessage = "Forbidden: Access to this resource is forbidden.";
            break;
        case 404:
            userFriendlyMessage = "Not Found: The requested resource could not be found.";
            break;
        case 500:
            userFriendlyMessage = "Internal Server Error: An error occurred on the server.";
            break;
        case 502:
            userFriendlyMessage = "Bad Gateway: The server received an invalid response from the upstream server.";
            break;
        case 503:
            userFriendlyMessage = "Service Unavailable: The server is currently unable to handle the request.";
            break;
        default:
            userFriendlyMessage = "An unexpected error occurred: Please try again later.";
            break;
    }

    return `${userFriendlyMessage} Please contact administration or try again later.`;
};

/**
 * Parses a timestamp into a formatted date string.
 *
 * @param {number} timestamp - The timestamp to parse.
 * @returns {string} The formatted date string in 'YYYY-MM-DD HH:MM:SS' format.
 */
const parseTimestampToDate = (timestamp) => {
    const date = new Date(timestamp);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-indexed
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};


const showFullScreenSpinner = () => {
    const spinnerOverlay = document.createElement('div');
    spinnerOverlay.id = 'spinner-overlay';
    spinnerOverlay.innerHTML = `
        <div class="spinner-container">
            <div class="spinner"></div>
        </div>
    `;

    if($(".modal").hasClass("show")) {
        $(".modal").append(spinnerOverlay);
    } else {
        document.body.appendChild(spinnerOverlay);

    }
}

const hideFullScreenSpinner = () => {
    const spinnerOverlay = document.getElementById('spinner-overlay');
    if (spinnerOverlay) {
        $(spinnerOverlay).remove();
    }
}

/**
 * Logs debugging messages to the console.
 * @param {string} message - The message to log.
 */
function debug(message) {
    console.debug(message);
}