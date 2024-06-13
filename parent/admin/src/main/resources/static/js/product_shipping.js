function initHsCodeSelect2() {
    $('#hsCode').select2({
        width: '100%',
        ajax: {
            url: `${MODULE_URL}products/hs-codes`,
            dataType: 'json',
            delay: 250,
            data: function(params) {
                let query = {
                    page: params.page || 1,
                };

                if (params.term) {
                    if (/^\d+$/.test(params.term)) {
                        query.code = params.term;
                    } else {
                        query.description = params.term;
                    }
                }

                return query;
            },
            processResults: function(data, params) {
                params.page = params.page || 1;

                if (!data.hsCodes) {
                    console.error('Invalid response format:', data);
                    return {
                        results: [],
                        pagination: {
                            more: false
                        }
                    };
                }

                return {
                    results: data.hsCodes.map(function(item) {
                        return {
                            id: item.code,
                            text: item.description
                        };
                    }),
                    pagination: {
                        more: (params.page * 20) < data.meta.pagination.count
                    }
                };
            },
            cache: true
        },
        placeholder: 'Search for an HS code',
        minimumInputLength: 0,
        templateResult: formatHsCode,
        templateSelection: formatHsCodeSelection
    });
}

function formatHsCode(hsCode) {
    if (hsCode.loading) {
        return hsCode.text;
    }

    const $container = $(
        "<div class='select2-result-hscode clearfix'>" +
        "<div class='select2-result-hscode__description'></div>" +
        "</div>"
    );

    $container.find(".select2-result-hscode__description").text(hsCode.text);

    return $container;
}

function formatHsCodeSelection(hsCode) {
    return hsCode.text || hsCode.id;
}

$(document).ready(function() {
    initHsCodeSelect2();
});
