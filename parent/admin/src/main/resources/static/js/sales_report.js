"use strict";

// Endpoints for fetching reports
const endpoints = {
    date: "reports/sales/",
    category: "reports/sales_by_group/CATEGORY/",
    product: "reports/sales_by_group/PRODUCT/",
};

let chartDataCache = {}; // Cache to avoid refetching during resize

$(function () {
    // Set default date in the date inputs
    $(".form-control").val(getTodayDate());

// Load Google Charts and initialize default view
    google.charts.load("current", { packages: ["corechart", "bar", "table"] });
    google.charts.setOnLoadCallback(() => loadChartsAndUpdateUI("7_days"));

    // Handle report duration dropdown selection
    $("#report-duration").on("click", "li", function () {
        const selectedPeriod = $(this).data("period");
        loadChartsAndUpdateUI(selectedPeriod);
    });

    // Handle custom "from" and "to" date input changes
    $("#from, #to").on("change", handleDateChange);

    // Add a resize event listener to redraw charts on window resize
    $(window).on("resize", throttle(redrawCharts, 500));
});

/**
 * Returns today's date in YYYY-MM-DD format.
 * @returns {string}
 */
function getTodayDate() {
    return new Date().toISOString().slice(0, 10);
}

/**
 * Handles custom date input changes and validation.
 */
function handleDateChange() {
    const fromDate = new Date($("#from").val());
    const toDate = new Date($("#to").val());

    if (isValidDateRange(fromDate, toDate)) {
        updateDropdownUI("custom");
        loadChartsForPeriod("custom");
    } else {
        alert("The 'From' date must be earlier than or the same as the 'To' date.");
    }
}

/**
 * Redraws all charts using the cached data.
 */
function redrawCharts() {
    if (chartDataCache.date) drawChart("date_chart_div", chartDataCache.date);
    if (chartDataCache.category) drawChart("category_chart_div", chartDataCache.category);
    if (chartDataCache.product) drawChart("product_chart_div", chartDataCache.product);
}

/**
 * Throttles the execution of a function to improve performance during window resizing.
 * @param {Function} func - The function to throttle.
 * @param {number} limit - The time in milliseconds to limit the function call.
 * @returns {Function} - The throttled function.
 */
function throttle(func, limit) {
    let inThrottle;
    return function () {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => (inThrottle = false), limit);
        }
    };
}

/**
 * Validates that the "from" date is not after the "to" date.
 * @param {Date} fromDate - The start date.
 * @param {Date} toDate - The end date.
 * @returns {boolean} - True if the dates are valid, false otherwise.
 */
function isValidDateRange(fromDate, toDate) {
    return fromDate <= toDate;
}

/**
 * Loads charts and updates the UI for the selected period.
 * @param {string} period - The time period selected (e.g., '7_days', 'custom').
 */
function loadChartsAndUpdateUI(period) {
    updateDateInputs(period);
    updateDropdownUI(period);
    loadChartsForPeriod(period);
}

/**
 * Updates the dropdown UI by setting the active class and updating the displayed text.
 * @param {string} period - The selected time period.
 */
function updateDropdownUI(period) {
    const selectedText = $(`[data-period="${period}"] a`).text();
    $("#period").text(selectedText);
    $("#report-duration").find("[data-period] a").removeClass("active");
    $(`[data-period="${period}"] a`).addClass("active");
}

/**
 * Loads charts based on the selected time period.
 * @param {string} period - The time period for which the charts should be displayed (e.g., '7_days', 'custom').
 */
function loadChartsForPeriod(period) {
    fetchAndDrawChart("date_chart_div", period, endpoints.date);
    fetchAndDrawChart("category_chart_div", period, endpoints.category);
    fetchAndDrawChart("product_chart_div", period, endpoints.product);
}

/**
 * Updates the "from" and "to" date inputs based on the selected period.
 * @param {string} period - The selected period ('7_days', '28_days', '6_months', '1_year', 'custom_date').
 */
function updateDateInputs(period) {
    const today = new Date();
    let fromDate, toDate;

    // Clone the today date for fromDate and toDate to avoid modifying the same object
    toDate = new Date(today); // Set the default toDate to today

    switch (period) {
        case "7_days":
            fromDate = new Date(today); // Clone the today date
            fromDate.setDate(fromDate.getDate() - 7);
            break;
        case "28_days":
            fromDate = new Date(today); // Clone the today date
            fromDate.setDate(fromDate.getDate() - 28);
            break;
        case "6_months":
            fromDate = new Date(today); // Clone the today date
            fromDate.setMonth(fromDate.getMonth() - 6);
            break;
        case "1_year":
            fromDate = new Date(today); // Clone the today date
            fromDate.setFullYear(fromDate.getFullYear() - 1);
            break;
        case "custom_date":
            return; // Leave the date unchanged for custom
        default:
            fromDate = toDate = new Date(today); // Default case, use today for both
            break;
    }

    // Set the date values in the input fields
    $("#from").val(fromDate.toISOString().slice(0, 10)); // Set 'from' date
    $("#to").val(toDate.toISOString().slice(0, 10)); // Set 'to' date
}


/**
 * Fetches data and draws the chart for the given element and period.
 * Caches data for reuse on resize.
 * @param {string} elementId - ID of the chart div.
 * @param {string} period - Time period.
 * @param {string} endpoint - API endpoint.
 */
async function fetchAndDrawChart(elementId, period, endpoint) {
    const fromDate = $("#from").val();
    const toDate = $("#to").val();
    const url = buildApiUrl(endpoint, period, fromDate, toDate);

    try {
        const data = await ajaxUtil.get(url);
        chartDataCache[elementId.replace("_chart_div", "")] = data; // Cache chart data
        drawChart(elementId, data);
    } catch (error) {
        console.error(error);
        showErrorModal(error.response);
    }
}
/**
 * Builds the API URL based on the selected period and date range.
 * @param {string} base - The base endpoint for the report.
 * @param {string} period - The selected time period (e.g., '7_days', 'custom').
 * @param {string} from - The start date for the custom range.
 * @param {string} to - The end date for the custom range.
 * @returns {string} - The full API URL.
 */
function buildApiUrl(base, period, from, to) {
    return period === "custom" ? `${MODULE_URL}${base}${from}/${to}` : `${MODULE_URL}${base}${period}`;
}

/**
 * Draws the appropriate chart based on the element ID and report data.
 * @param {string} elementId - The ID of the HTML element where the chart should be drawn.
 * @param {Object} report - The report data.
 */
function drawChart(elementId, report) {
    const chartDiv = $(`#${elementId}`);
    if (!chartDiv.length) return console.error("Chart element not found:", elementId);

    const chartType = getChartTypeFromElementId(elementId);
    switch (chartType) {
        case "CATEGORY":
            drawPieChart(chartDiv[0], report);
            break;
        case "PRODUCT":
            drawTableChart(chartDiv[0], report);
            break;
        default:
            drawColumnChart(chartDiv[0], report);
            break;
    }
}

/**
 * Determines the chart type based on the element ID.
 * @param {string} elementId - The element ID for the chart.
 * @returns {string} - The chart type (CATEGORY, PRODUCT, DATE).
 */
function getChartTypeFromElementId(elementId) {
    if (elementId.includes("category")) return "CATEGORY";
    if (elementId.includes("product")) return "PRODUCT";
    return "DATE";
}

/**
 * Draws a pie chart for category sales data.
 * @param {HTMLElement} chartDiv - The HTML element where the chart will be rendered.
 * @param {Object} report - The report data.
 */
function drawPieChart(chartDiv, report) {
    const data = [["Category", "Gross Sales"]];
    report.reports.forEach(item => data.push([item.identifier, parseFloat(item.grossSales)]));

    const options = {
        title: "Percentage sales report by category",
        is3D: true,
        height: 600,
        chartArea: { left: "30%", top: "10%", width: "80%", height: "80%" },
    };

    const chart = new google.visualization.PieChart(chartDiv);
    chart.draw(google.visualization.arrayToDataTable(data), options);
}

/**
 * Draws a table chart for product sales data.
 * @param {HTMLElement} chartDiv - The HTML element where the chart will be rendered.
 * @param {Object} report - The report data.
 */
function drawTableChart(chartDiv, report) {
    const dataTable = new google.visualization.DataTable();
    let totalQuantity = report.reports.reduce((acc, curr) => acc + curr.ordersCount + curr.productsCount, 0);
    $("#productTotal .card-title").text(totalQuantity);

    dataTable.addColumn("string", "Product");
    dataTable.addColumn("number", "Quantity");
    dataTable.addColumn("number", "Gross Sales");

    report.reports.forEach(item => {
        dataTable.addRow([
            item.identifier,
            item.productsCount,
            { v: parseFloat(item.grossSales), f: item.grossSales.toString() },
        ]);
    });

    const options = {
        showRowNumber: true,
        width: "100%",
        height: "100%",
        page: "enable",
    };

    const table = new google.visualization.Table(chartDiv);
    table.draw(dataTable, options);
}

/**
 * Draws a column chart for date-based sales data.
 * @param {HTMLElement} chartDiv - The HTML element where the chart will be rendered.
 * @param {Object} report - The report data.
 */
function drawColumnChart(chartDiv, report) {
    displaySummaries(report);

    const data = [["Date", "Gross Sales", "Net Sales", "Orders"]];
    report.reports.forEach(item => {
        data.push([
            item.identifier,
            parseFloat(item.grossSales),
            parseFloat(item.netSales),
            item.ordersCount,
        ]);
    });

    const options = {
        height: 500,
        title: "Sales Data",
        vAxes: {
            0: { title: "Sales Amount", format: "currency" },
            1: { title: "Number of Orders" },
        },
        series: { 0: { targetAxisIndex: 0 }, 1: { targetAxisIndex: 0 }, 2: { targetAxisIndex: 1 } },
        animation: { startup: true, duration: 1000, easing: "out" },
    };

    const chart = new google.visualization.ColumnChart(chartDiv);
    chart.draw(google.visualization.arrayToDataTable(data), options);
}

/**
 * Displays summary values such as total gross, net sales, and other statistics.
 * @param {Object} report - The report data.
 */
function displaySummaries(report) {
    let totalGross = report.reports.reduce((acc, curr) => acc + curr.grossSales, 0);
    let totalNet = report.reports.reduce((acc, curr) => acc + curr.netSales, 0) - report.shippingCost;
    let totalQuantity = report.reports.reduce((acc, curr) => acc + curr.ordersCount + curr.productsCount, 0);

    // Update UI elements
    $("#total_gross_sales .card-title").text(totalGross.toFixed(2));
    $("#total_net_sales .card-title").text(totalNet.toFixed(2));
    $("#avg_gross_sales .card-title").text((totalGross / totalQuantity).toFixed(2));
    $("#avg_net_sales .card-title").text((totalNet / totalQuantity).toFixed(2));
    $("#orderTotal .card-title").text(totalQuantity);
    $("#shipping .card-title").text(report.shippingCost.toFixed(2));
    $("#fee .card-title").text(report.fee.toFixed(2));
}
