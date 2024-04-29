/**
 * Utility object for performing AJAX requests.
 */
const ajaxUtil = {

    /**
     * Retrieves the CSRF token from an input element with the name '_csrf'.
     *
     * @returns {string|null} The CSRF token if found, otherwise null.
     */
    getCSRFToken: function () {
        const csrfInput = document.querySelector('input[name="_csrf"]');
        return csrfInput ? csrfInput.value : null;
    },

    /**
     * Performs a GET request to the specified URL.
     *
     * @param {string} url - The URL to send the GET request to.
     * @returns {Promise<Object>} A promise that resolves to the response data as a JSON object.
     * @throws {Error} If the GET request fails.
     */
    async get(url) {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            const error = new Error(`GET request failed: ${response.statusText}`);
            error.response = errorResponse;
            throw error;
        }

        return await response.json();

    },

    async getText(url) {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'text/html'
            }
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            const error = new Error(`GET request failed: ${response.statusText}`);
            error.response = errorResponse;
            throw error;
        }

        return await response.text();
    },

    /**
     * Performs a GET request to the specified URL and does not expect a response body.
     *
     * @param {string} url - The URL to send the GET request to.
     * @returns {Promise<void>} A promise that resolves if the request is successful, or rejects if it fails.
     * @throws {Error} If the GET request fails.
     */
    async getVoid(url) {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            const error = new Error(`GET request failed: ${response.statusText}`);
            error.response = errorResponse;
            throw error;
        }

        // No need to return anything as we expect no response body
    },

    /**
     * Performs a POST request to the specified URL with the provided data.
     *
     * @param {string} url - The URL to send the POST request to.
     * @param {Object} data - The data to include in the POST request body.
     * @returns {Promise<Object>} A promise that resolves to the response data as a JSON object.
     * @throws {Error} If the POST request fails.
     */
    async post(url, data) {
        const csrfToken = this.getCSRFToken();
        const headers = {
            'Content-Type': 'application/json'
        };

        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            const error = new Error(`POST request failed: ${response.statusText}`);
            error.response = errorResponse;
            throw error;
        }

        return await response.json();

    },

    /**
     * Performs a POST request to the specified URL with the provided data.
     *
     * @param {string} url - The URL to send the POST request to.
     * @param {Object} data - An object containing the data to include in the POST request.
     *                         The object should include the file and any other required parameters.
     * @returns {Promise<Blob>} A promise that resolves to the response data as a Blob (assuming the response is an image).
     * @throws {Error} If the POST request fails.
     */
    async postBlob(url, data) {
        const csrfToken = this.getCSRFToken();
        const headers = {
            'X-CSRF-TOKEN': csrfToken,
        };

        // Construct FormData to handle file upload and other parameters
        const formData = new FormData();
        for (const key in data) {
            if (data.hasOwnProperty(key)) {
                formData.append(key, data[key]);
            }
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: headers,
            body: formData
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            const error = new Error(`POST request failed: ${response.statusText}`);
            error.response = errorResponse;
            throw error;
        }

        return await response.blob(); // Assuming the response is an image
    },

    /**
     * Performs a DELETE request to the specified URL.
     *
     * @param {string} url - The URL to send the DELETE request to.
     * @returns {Promise<void>} A promise that resolves if the request is successful, or rejects if it fails.
     * @throws {Error} If the DELETE request fails.
     */
    async delete(url) {
        const csrfToken = this.getCSRFToken();
        const headers = {
            'Content-Type': 'application/json'
        };

        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        const response = await fetch(url, {
            method: 'DELETE',
            headers: headers
        });

        if (!response.ok) {
            const errorResponse = await response.json();
            const error = new Error(`DELETE request failed: ${response.statusText}`);
            error.response = errorResponse;
            throw error;
        }
    }
};

