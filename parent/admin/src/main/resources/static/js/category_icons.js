$(document).ready(function () {
    setItemsPerPage();
    handleIconDisplay($("#parent").val());
    listAllIcons();
    initListeners();
    $(window).resize(setItemsPerPage); // Adjust items per page on window resize
});

let currentPage = 1;
let itemsPerPage = 50;
let currentIcons = [];

function setItemsPerPage() {
    const width = $(window).width();
    if (width <= 575) {
        itemsPerPage = 20; // For x-small screens
    } else if (width <= 768) {
        itemsPerPage = 30; // For small screens
    } else if (width <= 992) {
        itemsPerPage = 40; // For medium screens
    } else {
        itemsPerPage = 50; // For large screens
    }
    displayIcons(currentIcons, currentPage);
}

function initListeners() {
    $("#results").on("click", "i", handleIconClick);
    $("#iconSize").on("change", handleIconSizeChange);
    $("#parent").on("change", event => handleIconDisplay(event.target.value));
}

function handleIconClick(event) {
    const iconHtml = $(this)[0].outerHTML;
    $('#iconSize').val('');
    $("#categoryIcon\\.iconPath").val(iconHtml);
}

function handleIconSizeChange() {
    const $currentIconEl = $("#categoryIcon\\.iconPath").val();
    $currentIconEl.removeClass("fa-2xs fa-xs fa-sm fa-lg fa-xl fa-2xl").addClass(this.value);
    $("#categoryIcon\\.iconPath").val($currentIconEl.prop('outerHTML'));
}

async function fetchIcons(query) {
    const endpoint = 'https://api.fontawesome.com';
    const graphQLQuery = { query };

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(graphQLQuery)
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const result = await response.json();
        return result.data;
    } catch (error) {
        console.error('Error fetching icons:', error);
        document.getElementById('results').textContent = 'Error fetching icons';
    }
}

function createIconElement(icon, prefix) {
    const iconElement = document.createElement('i');
    iconElement.className = `${prefix} fa-${icon.id} icon-preview`;
    return iconElement;
}

function displayIcons(icons, page = 1) {
    const resultsContainer = document.getElementById('results');
    resultsContainer.innerHTML = ''; // Clear previous results

    const start = (page - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    const paginatedIcons = icons.slice(start, end);

    paginatedIcons.forEach(icon => {
        const prefix = getIconPrefix(icon);
        if (prefix) {
            const iconElement = createIconElement(icon, prefix);
            resultsContainer.appendChild(iconElement);
        }
    });

    setupPagination(icons.length, page);
}

function getIconPrefix(icon) {
    if (icon.membership.free.includes('brands')) {
        return 'fa-brands';
    } else if (icon.membership.free.includes('solid')) {
        return 'fa-solid';
    } else if (icon.membership.free.includes('regular')) {
        return 'fa-regular';
    }
    return '';
}

function setupPagination(totalItems, currentPage) {
    const paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = ''; // Clear previous pagination

    const totalPages = Math.ceil(totalItems / itemsPerPage);

    // Always show the first page
    paginationContainer.appendChild(createPageItem(1, currentPage));

    if (totalPages <= 5) {
        for (let i = 2; i <= totalPages; i++) {
            const pageItem = createPageItem(i, currentPage);
            paginationContainer.appendChild(pageItem);
        }
    } else {
        if (currentPage > 3) {
            paginationContainer.appendChild(createDotsItem());
        }

        const start = Math.max(2, currentPage - 1);
        const end = Math.min(totalPages - 1, currentPage + 1);

        for (let i = start; i <= end; i++) {
            if (i === 1 || i === totalPages) continue; // Skip first and last page if already included
            const pageItem = createPageItem(i, currentPage);
            paginationContainer.appendChild(pageItem);
        }

        if (currentPage < totalPages - 2) {
            paginationContainer.appendChild(createDotsItem());
        }

        // Always show the last page
        paginationContainer.appendChild(createPageItem(totalPages, currentPage));
    }
}

function createPageItem(pageNumber, currentPage) {
    const pageItem = document.createElement('li');
    pageItem.className = `page-item ${pageNumber === currentPage ? 'active' : ''}`;
    const pageLink = document.createElement('a');
    pageLink.className = 'page-link';
    pageLink.textContent = pageNumber;
    pageLink.href = '#';
    pageLink.onclick = function (e) {
        e.preventDefault();
        currentPage = pageNumber;
        displayIcons(currentIcons, currentPage);
    };
    pageItem.appendChild(pageLink);
    return pageItem;
}

function createDotsItem() {
    const dots = document.createElement('li');
    dots.className = 'page-item disabled';
    const span = document.createElement('span');
    span.className = 'page-link';
    span.innerHTML = '...';
    dots.appendChild(span);
    return dots;
}

async function listAllIcons() {
    const query = `
        query {
            release(version: "6.5.2") {
                icons {
                    id
                    unicode
                    membership {
                        free
                    }
                }
            }
        }
    `;
    const data = await fetchIcons(query);
    if (data) {
        currentIcons = filterFreeIcons(data.release.icons);
        displayIcons(currentIcons, currentPage);
    }
}

async function searchIcons() {
    const searchQuery = document.getElementById('search').value;
    const query = `
        query {
            search(version: "6.5.2", query: "${searchQuery}", first: 500) {
                id
                unicode
                membership {
                    free
                }
            }
        }
    `;
    const data = await fetchIcons(query);
    if (data) {
        currentIcons = filterFreeIcons(data.search);
        displayIcons(currentIcons, currentPage);
    }
}

function filterFreeIcons(icons) {
    return icons.filter(icon => {
        return icon.membership.free.includes('brands') ||
            icon.membership.free.includes('solid') ||
            icon.membership.free.includes('regular');
    });
}

function handleIconDisplay(categoryId) {
    const iconContainers = $(".icon");
    if(Number(categoryId) === -1) {
        iconContainers.removeClass("d-none");
    } else {
        $("#categoryIcon\\.iconPath").val("");
        iconContainers.addClass("d-none");
    }

}
