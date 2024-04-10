/**
 * Compresses the given image file.
 * @param {File} file - The image file to compress.
 * @param {number} width - The width for the compressed image.
 * @param {number} height - The height for the compressed image.
 * @param {number} quality - The quality level for the compressed image.
 * @returns {Promise<File>} The compressed image file.
 */
async function compressImage(file, width, height, quality) {
    try {
        const data = { file, width, height, quality };
        const compressedImageBlob = await ajaxUtil.postBlob(`${MODULE_URL}upload`, data);
        return new File([compressedImageBlob], file.name, { type: "image/jpeg" });
    } catch (error) {
        console.error("Error during image compression:", error.response);
        throw error;
    }
}