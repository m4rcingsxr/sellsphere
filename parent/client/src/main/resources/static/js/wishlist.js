$(function () {
    $("body").on("click", ".wishlist", handleWishlistClick)
})

async function handleWishlistClick(event) {
    event.preventDefault();
    if(!LOGGED_IN) {
        window.location.href= `${config.baseUrl}/realms/SellSphere/protocol/openid-connect/auth?response_type=code&client_id=login-app&scope=openid&state=DYhvxgGZQXoQz3RT9GZPf6ZdDuUEWx4nHSVbnqTfakY%3D&redirect_uri=${config.baseUrl}/login/oauth2/code/keycloak`;
    }
    const $wishlistBtn = $(event.currentTarget);
    const $icon = $wishlistBtn.children();
    const isRemove = $icon.hasClass("bi-heart-fill");

    const productId = $wishlistBtn.data("product-id");

    showFullScreenSpinner();

    if (isRemove) {
        try {
            await removeProduct(productId);
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            if(window.location.href.endsWith("wishlist")) {
                const $wishlist = $wishlistBtn.closest(".col-lg-9");
                $wishlistBtn.parent().parent().parent().remove();
                if ($wishlist.find(".col-sm-4").length === 0) {
                    $wishlist.remove();
                    $("#empty-wishlist").removeClass("d-none");
                }
            }

            $icon.removeClass("bi-heart-fill").removeClass("text-danger").addClass("bi-heart");
            hideFullScreenSpinner();
        }
    } else {
        try {
            await addProduct(productId);
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        } finally {
            $icon.addClass("bi-heart-fill").addClass("text-danger").removeClass("bi-heart");
            hideFullScreenSpinner();
        }

    }

}

async function addProduct(productId) {
    const url = `${MODULE_URL}wishlist/add/${productId}`;
    await ajaxUtil.postText(url);
}

async function removeProduct(productId) {
    const url = `${MODULE_URL}wishlist/delete/${productId}`;
    await ajaxUtil.postText(url);
}
