$(function () {
   init();
});

async function init() {
    const articleType = $("#articleType").val();
    showArticleForm(articleType);
    await handleChangePromotionArticleType($("#save-promotion-article-type").val());
    await handleChangePromotionProductType($("#promotion-type").val());
    await handleChangeOfExistingPromotions($("#existing-promotions-input").val());

    initListeners();
}

function initListeners() {
    $("#articleType").on("change", function () {
        showArticleForm(this.value);
    })
    $("#navigation").on("click", ".nav-link", handleSelectNavigationItemNumber);
    $("#footer").on("click", ".nav-link", handleSelectFooterItemNumber);

    $("#promotion-type").on("change", async function() {
        await handleChangePromotionProductType(this.value);
    });

    $("#save-promotion-article-type").on("change", async function() {
        await handleChangePromotionArticleType(this.value);
        if(this.value === 'EXISTING') {
            $("#existing-promotions").removeClass("d-none");
            $("#new-product").addClass("d-none");
        } else {
            $("#existing-promotions").addClass("d-none");
            $("#new-product").removeClass("d-none");
        }
    })

    $("#categories").on("change", async function() {
        await loadProductsForSelectedCategory(this.value);
    });
    $("#brands").on("change", async function () {
        await loadProductsForSelectedBrand(this.value);
    });
    $("#keyword").on("change", loadProductsForKeyword);
    $("#select-products").on("change", loadSelectedProducts)
    $("#selected-products").on("change", removeUnselectedOptions)

    $("#existing-promotions-input").on("change", async function() {
        await handleChangeOfExistingPromotions(this.value);
    })
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
    $("#save-promotion-type").addClass("d-none");
    $("#footer").addClass("d-none");
    $("#existing-promotions").addClass("d-none");
    $("#new-product").addClass("d-none");
    $("#selected-products").addClass("d-none");
}

function showArticleNavigationForm() {
    $("#navigation").removeClass("d-none");
}

function showArticlePromotionForm() {
    $("#save-promotion-type").removeClass("d-none");
    $("#selected-products").removeClass("d-none");
}

function showArticleFooterForm() {
    $("#footer").removeClass("d-none");
}

function handleSelectNavigationItemNumber(event) {
    event.preventDefault();
    const isDisabled = $(this).attr("disabled");

    if (isDisabled) return;

    if (this.dataset.index === $("#navigation-order").val()) {
        return;
    }

    // consider if current article exist - and already take nav slot
    if ($("#id").val()) {
        const selectedOrder = $("#navigation-order").val();
        if (selectedOrder) {
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

    if (isDisabled) return;

    if (this.dataset.itemNumber === $("#navigation-order").val() && this.dataset.sectionNumber === $("#section-order")) {
        return;
    }

    // consider if current article exist - and already take nav slot
    if ($("#id").val()) {
        const selectedOrder = $("#navigation-order").val();
        const selectedSection = $("#section-order").val();
        if (selectedOrder && selectedSection) {
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

async function handleChangePromotionProductType(type) {


    switch (type) {
        case "BRAND" : {
            try {
                const brands = await fetchAllBrands();
                const brandsContainer = $("#brands");
                brandsContainer.empty();
                brands.forEach(brand => {
                    brandsContainer.append(
                        `<option value="${brand.id}">${brand.name}</option>`
                    );
                })

                await loadProductsForSelectedBrand(brandsContainer.val());

                $("#keyword").addClass("d-none");
                $("#brands").removeClass("d-none");
                $("#categories").addClass("d-none");
            } catch (error) {
                console.error(error);
                showErrorModal(error.response);
            }
            break;
        }
        case "CATEGORY" : {
            try {
                const categories = await fetchAllCategories();

                const categoriesContainer = $("#categories");
                categoriesContainer.empty();
                categories.forEach(category => {
                    categoriesContainer.append(
                        `<option value="${category.id}">${category.name}</option>`
                    );
                })

                await loadProductsForSelectedCategory(categoriesContainer.val());

                $("#keyword").addClass("d-none");
                $("#brands").addClass("d-none");
                $("#categories").removeClass("d-none");
            } catch (error) {
                console.error(error);
                showErrorModal(error.response);
            }
            break;
        }
        case "KEYWORD" : {
            $("#keyword").removeClass("d-none");
            $("#brands").addClass("d-none");
            $("#categories").addClass("d-none");
            $("#select-products").empty();
            break;
        }
    }

}


async function fetchAllBrands() {
    return ajaxUtil.get(`${MODULE_URL}brands/fetch-all`);
}

async function fetchAllCategories() {
    return ajaxUtil.get(`${MODULE_URL}categories/fetch-all`);
}

async function loadProductsForSelectedBrand(brandId) {
    const products = await ajaxUtil.get(`${MODULE_URL}products/brand/${brandId}`);
    const productContainer = $("#select-products");
    productContainer.empty();
    products.forEach(product => {
        productContainer.append(
            `
                <option value="${product.id}">${product.name}</option>
            `
        );
    })

}

async function loadProductsForSelectedCategory(categoryId) {
    const products = await ajaxUtil.get(`${MODULE_URL}products/category/${categoryId}`);
    const productContainer = $("#select-products");
    productContainer.empty();
    products.forEach(product => {
        productContainer.append(
            `
                <option value="${product.id}">${product.name}</option>
            `
        );
    })
}

async function loadProductsForKeyword() {
    const productContainer = $("#select-products");
    productContainer.empty();

    if(!this.value) {

        return;
    }
    const products = await ajaxUtil.get(`${MODULE_URL}products/search/${this.value}`);

    products.forEach(product => {
        productContainer.append(
            `
                <option value="${product.id}">${product.name}</option>
            `
        );
    })
}

function loadSelectedProducts() {
    const selectElement = document.getElementById('select-products');
    const selectedOptions = [];
    for (let i = 0; i < selectElement.options.length; i++) {
        if (selectElement.options[i].selected) {
            selectedOptions.push({
                value: selectElement.options[i].value,
                text: selectElement.options[i].text
            });
        }
    }

    const selectedProducts = $("#selected-products");
    selectedOptions.forEach(option => {
        if (selectedProducts.find(`option[value='${option.value}']`).length === 0) {
            selectedProducts.append(
                `<option value="${option.value}" selected>${option.text}</option>`
            );
        }
    });
}


function removeUnselectedOptions() {
    const selectElement = document.getElementById('selected-products');
    const selectedProducts = $("#selected-products");

    selectedProducts.find('option').each(function() {
        const value = $(this).val();
        const isSelectedInSource = [...selectElement.options].some(option => option.value === value && option.selected);

        if (!isSelectedInSource) {
            $(this).remove();
        }
    });
}

async function handleChangePromotionArticleType(type) {
    $("#selected-products").empty();
    if(type === 'EXISTING') {
        const name = $("#existing-promotions-input").val();
        if(!name) return;

        $("#promotionName").val(name);
        await handleChangeOfExistingPromotions(name);
    } else {
        $("#promotionName").val("");
    }
}

async function handleChangeOfExistingPromotions(name) {
    try {
        if(!name) return;

        const promotion = await ajaxUtil.get(`${MODULE_URL}promotions/${name}`);
        const products = $("#selected-products");

        products.empty();
        promotion.products.forEach(product => {
            products.append(
                `
            <option value="${product.id}" selected>${product.name}</option>
           `
            )
        });

        $('input[name="promotionName"]').val(name);
        console.log("Promotion name set to:", name);
    } catch(error) {
        console.error(error);
        showErrorModal(error.response);
    }
}