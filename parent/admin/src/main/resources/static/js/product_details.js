// Global variable to keep track of the current index for detail inputs
let detailCurrentIndex;

$(function () {
    // Initialize the detailCurrentIndex based on existing hidden input fields
    detailCurrentIndex = $("input[type='hidden']").filter((_, element) => /^details\[\d+\]\.id$/.test(element.name)).length;
    initializeDetailInputListeners();
});

/**
 * Initializes event listeners for adding and removing detail inputs.
 */
function initializeDetailInputListeners() {
    // Event listener for adding new detail inputs
    $("#newDetailInput").on("click", addDetailInput);

    // Event listener for removing detail inputs
    $("#details").on("click", 'a', removeDetailInput);
}

/**
 * Adds a new detail input section to the table body.
 * @param {Event} event - The event object from the click event.
 */
function addDetailInput(event) {
    // Get the HTML for the new detail section
    const detailSectionHtml = getDetailSectionHtml(detailCurrentIndex);

    // Append the new detail section to the table body
    $("#details tbody").append(detailSectionHtml);

    // Increment the current index
    detailCurrentIndex++;

    // Refresh the indexes and apply dynamic validation rules
    refreshIndexes();
    applyDynamicValidationRules();
}

/**
 * Returns the HTML string for a new detail input section with a given index.
 * @param {number} index - The index for the new detail input section.
 * @returns {string} - The HTML string for the new detail input section.
 */
function getDetailSectionHtml(index) {
    return `
            <tr>
                <th>${index}</th>
                <th>
                    <input class="form-control" type="text" name="names">
                </th>
                <th>
                    <input class="form-control" type="text" name="values">
                </th>
                <th>
                     <a href="#" class="link-primary"><i class="fa-solid fa-xmark fa-xl"></i></a>
                </th>
            </tr>
        `;
}

/**
 * Removes a detail input section when the "Remove" icon is clicked.
 * @param {Event} event - The event object from the click event.
 */
function removeDetailInput(event) {
    // Check if the clicked element is the "Remove" icon
    if ($(event.currentTarget).find('i.fa-xmark').length > 0) {
        // Remove the closest table row
        $(event.currentTarget).closest("tr").remove();

        // Decrement the current index
        detailCurrentIndex--;

        // Refresh the indexes
        refreshIndexes();
    }
}

/**
 * Updates the indexes displayed for each detail input section.
 */
function refreshIndexes() {
    $("#details tbody tr").each((index, element) => {
        // Update the text of the first cell in each row with the current index
        $(element).find("th").first().text(index);
    });
}
