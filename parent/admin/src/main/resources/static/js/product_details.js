let detailCurrentIndex;

$(function () {
    detailCurrentIndex = $("input[type='hidden']").filter((_, element) => /^details\[\d+\]\.id$/.test(element.name)).length;
    initializeDetailInputListeners();
});

function initializeDetailInputListeners() {
    $("#newDetailInput").on("click", addDetailInput);
    $("#details").on("click", 'a', removeDetailInput);
}

function addDetailInput(event) {
    const detailSectionHtml = getDetailSectionHtml(detailCurrentIndex);
    $("#details tbody").append(detailSectionHtml);
    detailCurrentIndex++;
    refreshIndexes();
}

function getDetailSectionHtml(index) {
    return `
            <tr>
                <th>${index}</th>
                <th>
                    <input class="form-control" type="text" id="details${index}.name" name="details[${index}].name">
                </th>
                <th>
                    <input class="form-control" type="text" id="details${index}.value" name="details[${index}].value">
                </th>
                <th>
                    <div class="d-flex justify-content-end">
                        <a href="#" class="link-primary"><i class="fa-solid fa-xmark fa-xl"></i></a>
                    </div>
                </th>
            </tr>
        `;
}

function removeDetailInput(event) {
    if ($(event.currentTarget).find('i.fa-xmark').length > 0) {
        $(event.currentTarget).closest("tr").remove();
        detailCurrentIndex--;
        refreshIndexes();
    }
}

function refreshIndexes() {
    $("#details tbody tr").each((index, element) => {
        $(element).find("th").first().text(index);
    });

    ['id', 'name', 'value', 'product'].forEach(refreshInputs);
}

function refreshInputs(attributeType) {
    $("input").filter((_, element) => new RegExp(`^details\\[\\d+\\]\\.${attributeType}$`).test(element.name))
        .each((index, element) => {
            $(element).attr({
                "id": `details${index}.${attributeType}`,
                "name": `details[${index}].${attributeType}`
            });
        });
}