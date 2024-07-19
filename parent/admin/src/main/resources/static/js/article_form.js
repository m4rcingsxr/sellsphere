$(function() {
    const articleType = $("#articleType").val();
    showArticleForm(articleType);

    initListeners();
});

function initListeners() {
    $("#articleType").on("change", function() {
        showArticleForm(this.value);
    })
    $("#navigation").on("click", ".nav-link", handleSelectNavigationItemNumber)
    $("#footer").on("click", ".nav-link", handleSelectFooterItemNumber)
}

function showArticleForm(articleType) {
    hideAllArticleForms();

    switch (articleType) {
        case "NAVIGATION" : {
            showArticleNavigationForm();
            break;
        }
        case "PROMOTION" : {
            showArticlePromotionForm();
            break;
        }
        case "FOOTER" : {
            showArticleFooterForm();
            break;
        }
        default: {
            break;
        }
    }

}

function hideAllArticleForms() {
    $("#navigation").addClass("d-none");
    $("#promotion").addClass("d-none");
    $("#footer").addClass("d-none");

}

function showArticleNavigationForm() {
    $("#navigation").removeClass("d-none");
}

function showArticlePromotionForm() {

}

function showArticleFooterForm() {
    $("#footer").removeClass("d-none");
}

function handleSelectNavigationItemNumber(event) {
    event.preventDefault();
    const isDisabled = $(this).attr("disabled");

    if(isDisabled) return;

    if(this.dataset.index === $("#navigation-order").val()) {
        return;
    }

    // consider if current article exist - and already take nav slot
    if($("#id").val()) {
        const selectedOrder = $("#navigation-order").val();
        if(selectedOrder) {
            $(`a[data-index="${selectedOrder}"`).removeClass("selected").addClass("fw-lighter").text("empty");
        }
    } else {
        // reset all which are not disabled
        $(".selected").addClass("fw-lighter").text("empty");
    }

    $("#navigation-order").val(this.dataset.index);
    $(this).removeClass("fw-lighter").addClass("selected").text("Current Article");
}

function handleSelectFooterItemNumber(event) {
    event.preventDefault();
    const isDisabled = $(this).attr("disabled");

    if(isDisabled) return;

    if(this.dataset.itemNumber === $("#navigation-order").val() && this.dataset.sectionNumber === $("#section-order")) {
        return;
    }

    // consider if current article exist - and already take nav slot
    if($("#id").val()) {
        const selectedOrder = $("#navigation-order").val();
        const selectedSection = $("#section-order").val();
        if(selectedOrder && selectedSection) {
            $(`a[data-item-number="${selectedOrder}"][data-section-number="${selectedSection}"]`).removeClass("selected").addClass("fw-lighter").text("empty");
        }
    } else {
        // reset all which are not disabled
        $(".selected").addClass("fw-lighter").text("empty");
    }

    $("#navigation-order").val(this.dataset.itemNumber);
    $("#section-order").val(this.dataset.sectionNumber);
    $(this).removeClass("fw-lighter").addClass("selected").text("Current Article");
}