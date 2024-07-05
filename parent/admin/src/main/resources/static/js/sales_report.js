"use strict";

$(function () {
    const endpoints = {
        date: `reports/sales/`,
        category: `reports/sales_by_group/CATEGORY/`,
        product: `reports/sales_by_group/PRODUCT/`,
    };

    // Set default dates
    $(".form-control").val(new Date().toISOString().slice(0, 10));

    // Google Charts Loader
    google.charts.load("current", { packages: ["corechart", "bar", "table"] });
    google.charts.setOnLoadCallback(() => {
        drawChart("date_chart_div", "7_days", endpoints["date"]);
        drawChart("category_chart_div", "7_days", endpoints["category"]);
        drawChart("product_chart_div", "7_days", endpoints["product"]);
    });

    // Tab click handlers
    $("#sales-date-tab").click(() =>
        handleTabClick("date_chart_div", "7_days", "date")
    );
    $("#sales-category-tab").click(() =>
        handleTabClick("category_chart_div", "7_days", "category")
    );
    $("#sales-product-tab").click(() =>
        handleTabClick("product_chart_div", "7_days", "product")
    );

    // Period button click handlers
    $(".btn-sales-by-date").click((event) =>
        handlePeriodClick(event, "date_chart_div", "date")
    );
    $(".btn-sales-by-category").click((event) =>
        handlePeriodClick(event, "category_chart_div", "category")
    );
    $(".btn-sales-by-product").click((event) =>
        handlePeriodClick(event, "product_chart_div", "product")
    );

    // Date change handlers
    $(".form-control").change((event) => handleDateChange(event.target));

    // Helper functions
    function handleTabClick(elementId, period, type) {
        drawChart(elementId, period, endpoints[type]);
        toggleActiveClass(type);
        toggleCustomDisplay(type, true);
    }

    function handlePeriodClick(event, elementId, type) {
        $(`#${elementId}`).empty();
        const period = $(event.target).data("period");
        toggleCustomDisplay(type, period === "custom");
        drawChart(elementId, period, endpoints[type]);
    }

    function handleDateChange(input) {
        const elementId = $(input).data("type");
        const type = typeFromElementId(elementId, true);

        drawChart(elementId, "custom", endpoints[type]);
    }

    function drawChart(elementId, period, endpoint) {
        const $from = $(`#${elementId}`).prev().find(".from");
        const $to = $(`#${elementId}`).prev().find(".to");

        const url = buildUrl(endpoint, period, $from.val(), $to.val());

        console.log(url);

        fetchData(url)
            .then((reports) => {
                console.log(reports);
                // Visualization logic based on 'elementId'
                initChart(elementId, reports, typeFromElementId(elementId, false));
                loadSummaries(reports);
            })
            .catch((error) => console.error("Failed to load data:", error));
    }

    async function fetchData(url) {
        const response = await fetch(url);
        if (!response.ok) throw new Error("Network response was not ok");
        return await response.json();
    }

    function buildUrl(base, period, from, to) {
        if (period === "custom") {
            return `${MODULE_URL}${base}${from}/${to}`;
        }
        return `${MODULE_URL}${base}${period}`;
    }

    function toggleActiveClass(type) {
        $(`.btn-sales-by-${type}`).removeClass("active").first().addClass("active");
    }

    function toggleCustomDisplay(type, hide) {
        if (hide) {
            $(`#custom_${type}_input`).removeClass("d-none");
        } else {
            $(`#custom_${type}_input`).addClass("d-none");
        }
    }

    function typeFromElementId(elementId, lowercase) {
        const type = elementId.includes("category")
            ? "CATEGORY"
            : elementId.includes("product")
                ? "PRODUCT"
                : elementId.includes("date")
                    ? "DATE"
                    : undefined;

        if (lowercase) return type.toLowerCase();

        return type;
    }
});

function initChart(elementId, data, reportType) {
    const chartDiv = document.getElementById(elementId);
    if (!chartDiv) {
        console.error("Chart element not found:", elementId);
        return;
    }

    // Decide the chart type based on the reportType
    if (reportType === "CATEGORY") {
        initPieChart(chartDiv, data);
    } else if (reportType === "PRODUCT") {
        initTableChart(chartDiv, data);
    } else {
        initColumnChart(chartDiv, data);
    }
}

function initPieChart(chartDiv, data) {
    const transformedData = [["Category", "Gross Sales"]];
    data.forEach((item) => {
        transformedData.push([item.identifier, parseFloat(item.grossSales)]);
    });

    const dataTable = google.visualization.arrayToDataTable(transformedData);

    const options = {
        title: "Percentage sales report by category",
        is3D: true,
        width: 800,
        height: 600,
        chartArea: {
            left: "30%", // Adjust as needed to center the chart
            top: "10%", // Reduce the top margin as needed
            width: "80%", // Adjust width of the actual chart area
            height: "80%", // Adjust height of the actual chart area
        },
    };

    const chart = new google.visualization.PieChart(chartDiv);
    chart.draw(dataTable, options);
}

function initTableChart(chartDiv, data) {
    const dataTable = new google.visualization.DataTable();

    dataTable.addColumn("string", "Product");
    dataTable.addColumn("number", "Quantity");
    dataTable.addColumn("number", "Gross Sales");
    dataTable.addColumn("number", "Net Sales");

    data.forEach((item) => {
        dataTable.addRow([
            item.identifier,
            item.productsCount,
            { v: parseFloat(item.grossSales), f: formatPrice(item.grossSales) },
            { v: parseFloat(item.netSales), f: formatPrice(item.netSales) },
        ]);
    });

    const table = new google.visualization.Table(chartDiv);

    const options = {
        showRowNumber: true,
        width: "100%",
        height: "100%",
        page: "enable",
    };

    table.draw(dataTable, options);
}

function initColumnChart(chartDiv, reports) {
    const data = [["Date", "Gross Sales", "Net Sales", "Orders"]];
    reports.forEach((report) => {
        data.push([
            report.identifier,
            parseFloat(report.grossSales),
            parseFloat(report.netSales),
            report.ordersCount,
        ]);
    });

    const dataTable = google.visualization.arrayToDataTable(data);

    const formatter = new google.visualization.NumberFormat({
        prefix: currencySymbolPosition === "Before price" ? currencySymbol : "",
        suffix: currencySymbolPosition === "After price" ? currencySymbol : "",
        decimalSymbol: decimalPointType === "POINT" ? "." : ",",
        groupingSymbol: thousandsPointType === "COMMA" ? "," : ".",
        fractionDigits: parseInt(decimalDigits),
    });

    formatter.format(dataTable, 1); // Apply to 'Gross Sales'
    formatter.format(dataTable, 2); // Apply to 'Net Sales'

    const options = {
        height: 500,
        series: {
            0: { targetAxisIndex: 0 },
            1: { targetAxisIndex: 0 },
            2: { targetAxisIndex: 1 },
        },
        title: "Sales Data",
        vAxes: {
            0: { title: "Sales Amount", format: "currency" },
            1: { title: "Number of Orders" },
        },
        animation: {
            startup: true,
            duration: 1000,
            easing: "out",
        },
    };

    const chart = new google.visualization.ColumnChart(chartDiv);
    chart.draw(dataTable, options);
}

function loadSummaries(reports) {
    // Aggregate data
    let totalGross = reports.reduce(
        (acc, curr) => acc + parseFloat(curr.grossSales),
        0
    );
    let totalNet = reports.reduce(
        (acc, curr) => acc + parseFloat(curr.netSales),
        0
    );

    let quantity = reports.reduce(
        (acc, curr) => acc + curr.ordersCount + curr.productsCount,
        0
    );

    console.log(totalGross, totalNet);

    // Display in UI
    $("#total_gross_sales .card-title").text(formatPrice(totalGross));
    $("#total_net_sales .card-title").text(formatPrice(totalNet));
    $("#avg_gross_sales .card-title").text(
        (totalGross / quantity && formatPrice(totalGross / quantity)) ||
        formatPrice(0)
    );
    $("#avg_net_sales .card-title").text(
        (totalNet / quantity && formatPrice(totalNet / quantity)) || formatPrice(0)
    );
    $("#total .card-title").text(quantity);
}

function formatPrice(productPrice) {
    let formattedPrice = "";

    // Add currency symbol before or after the price based on the currency symbol position
    if (currencySymbolPosition === "Before price") {
        formattedPrice += currencySymbol;
    }

    // Format the product price with the specified decimal digits and separators
    formattedPrice += productPrice.toLocaleString(undefined, {
        minimumFractionDigits: decimalDigits,
        maximumFractionDigits: decimalDigits,
        decimalSeparator: decimalPointType === "Dot" ? "." : ",",
        groupingSeparator: thousandsPointType === "Dot" ? "." : ",",
    });

    // Add currency symbol after the price if required
    if (currencySymbolPosition === "After price") {
        formattedPrice += currencySymbol;
    }

    return formattedPrice;
}
