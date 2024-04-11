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
    applyDynamicValidationRules();
}

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
}

