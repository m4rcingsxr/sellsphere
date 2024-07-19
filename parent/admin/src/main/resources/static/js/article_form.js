$(function() {
    const articleType = $("#articleType").val();
    showArticleForm(articleType);
});


function showArticleForm(articleType) {

    switch (articleType) {
        case "PROMOTION" : {
            showArticlePromotionForm();
            break;
        }
        case "NAVIGATION" : {
            showArticleNavigationForm();
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

function showArticlePromotionForm() {
    
}

function showArticleNavigationForm() {

}

function showArticleFooterForm() {

}