/**
 * Formats bytes into a human-readable string with optional decimal precision.
 *
 * @param {number} bytes - The number of bytes to format.
 * @param {number} [decimals=2] - The number of decimals for formatting.
 * @returns {string} The formatted string.
 */
const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return `${(bytes / Math.pow(k, i)).toFixed(dm)} ${sizes[i]}`;
};

/**
 * Redirects to the list page by trimming the URL up to "/edit".
 */
const goToListPage = () => {
    const currentURL = window.location.href;
    window.location.href = currentURL.split("/edit")[0];
};

/**
 * Displays a modal dialog with specified content and optional primary button URL.
 *
 * @param {string} modalId - The modal element's ID.
 * @param {string} title - The modal's title.
 * @param {string} content - The content to display.
 * @param {string} [contentType='text'] - Type of content, either 'text' or 'html'.
 * @param {string} [btnURL] - Optional URL for the primary button.
 */
const showModalDialog = (modalId, title, content, contentType = 'text', btnURL) => {
    const $modal = $(`#${modalId}`);
    $modal.find('.modal-title').text(title);
    $modal.find('.modal-body')[contentType](content);  // Directly use .html() or .text() based on contentType

    if (btnURL) {
        $modal.find('.btn-primary').attr('href', btnURL);
    }

    $modal.modal('show');
};

/**
 * Displays an error modal with the provided error response.
 *
 * @param {Object} errorResponse - The error response object.
 */
const showErrorModal = (errorResponse) => {
    const date = parseTimestampToDate(errorResponse.timestamp);
    $("#errorModal .modal-footer").text(date);

    const userFriendlyMessage = handleErrorResponse(errorResponse.status, errorResponse.message);
    showModalDialog('errorModal', '⚠ Error occurred', userFriendlyMessage);
};

/**
 * Generates a user-friendly message based on the HTTP status code.
 *
 * @param {number} status - HTTP status code.
 * @param {string} [message] - Optional server-provided message.
 * @returns {string} A user-friendly error message.
 */
const handleErrorResponse = (status, message) => {
    const errorMessages = {
        400: `Bad Request: ${message || 'The server could not understand the request.'}`,
        401: "Unauthorized: You are not authorized to access this resource.",
        403: "Forbidden: Access to this resource is forbidden.",
        404: "Not Found: The requested resource could not be found.",
        500: "Internal Server Error: An error occurred on the server.",
        502: "Bad Gateway: The server received an invalid response from the upstream server.",
        503: "Service Unavailable: The server is currently unable to handle the request."
    };

    return errorMessages[status] || "An unexpected error occurred. Please try again later.";
};

/**
 * Parses a timestamp into a formatted date string (YYYY-MM-DD HH:MM:SS).
 *
 * @param {number} timestamp - The timestamp to parse.
 * @returns {string} Formatted date string.
 */
const parseTimestampToDate = (timestamp) => {
    const date = new Date(timestamp);
    const format = (num) => String(num).padStart(2, '0');
    return `${date.getFullYear()}-${format(date.getMonth() + 1)}-${format(date.getDate())} ${format(date.getHours())}:${format(date.getMinutes())}:${format(date.getSeconds())}`;
};

/**
 * Shows a full-screen spinner overlay while processing.
 */
const showFullScreenSpinner = () => {
    const spinnerOverlay = `
        <div id="spinner-overlay">
            <div class="spinner-container">
                <div class="spinner"></div>
            </div>
        </div>`;
    $('body').append(spinnerOverlay);
};

/**
 * Hides the full-screen spinner overlay.
 */
const hideFullScreenSpinner = () => {
    $('#spinner-overlay').remove();
};

/**
 * Outputs a debug message to the console.
 *
 * @param {string} message - The message to debug.
 */
const debug = (message) => {
    console.debug(message);
};


