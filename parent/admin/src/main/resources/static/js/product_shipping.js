function initHsCodeSelect2(initialHsCode) {
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

                console.log("Processing results: ", data);

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
                        more: data.meta.pagination.next ? true : false
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

    if (initialHsCode) {
        console.log('Initial HS Code:', initialHsCode);
        fetchInitialHsCode(initialHsCode);
    }
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

function fetchInitialHsCode(initialHsCode) {
    $.ajax({
        url: `${MODULE_URL}products/hs-codes`,
        dataType: 'json',
        data: {
            code: initialHsCode,
            page: 1
        },
        success: function(response) {
            console.log('Fetch Initial HS Code Response:', response);
            if (response.hsCodes && response.hsCodes.length > 0) {
                const hsCode = response.hsCodes[0];
                const option = new Option(hsCode.description, hsCode.code, true, true);
                $('#hsCode').append(option).trigger('change');
            } else {
                console.error('HS code not found:', initialHsCode);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.error('Failed to fetch initial HS code:', errorThrown);
        }
    });
}

$(document).ready(function() {
    const initialHsCode = $("#initial-hs-code").val();
    console.log('Document Ready - Initial HS Code:', initialHsCode);
    initHsCodeSelect2(initialHsCode);
});
