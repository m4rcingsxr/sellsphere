renderPagination(productsPage) {
    const currentPage = Number(productsPage.page) + 1;
    const totalPages = productsPage.totalPages;
    const $pagination = $("#pagination");

    let html = ``;

    if (totalPages > 1) {
        html += `
                <li class="page-item ${currentPage === 1 ? 'disabled' : ''}">
                    <a class="page-link page" href="#" aria-label="Previous">
                        <span aria-hidden="true">&laquo;</span>
                    </a>
                </li>
            `;

        if (totalPages <= 5) {
            for (let i = 1; i <= totalPages; i++) {
                html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link page" href="#">${i}</a></li>`;
            }
        } else {
            if (currentPage <= 3) {
                for (let i = 1; i <= 4; i++) {
                    html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link page" href="#">${i}</a></li>`;
                }
                html += `<li class="page-item"><a class="page-link">
                            <span class="select-page">...</span>
                            <input type="number" class="form-control page-input"/>
                        </a></li>`;
                html += `<li class="page-item"><a class="page-link page" href="#">${totalPages}</a></li>`;
            } else if (currentPage >= totalPages - 2) {
                html += `<li class="page-item"><a class="page-link page" href="#">1</a></li>`;
                html += `<li class="page-item"><a class="page-link">
                            <span class="select-page">...</span>
                            <input type="number" class="form-control page-input"/>
                        </a></li>`;
                for (let i = totalPages - 3; i <= totalPages; i++) {
                    html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link page" href="#">${i}</a></li>`;
                }
            } else {
                html += `<li class="page-item"><a class="page-link page" href="#">1</a></li>`;
                if (currentPage - 1 > 2) {
                    html += `<li class="page-item"><a class="page-link">
                            <span class="select-page">...</span>
                            <input type="number" class="form-control page-input"/>
                        </a></li>`;
                }
                for (let i = currentPage - 1; i <= currentPage + 1; i++) {
                    html += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link page" href="#">${i}</a></li>`;
                }
                if (currentPage + 1 < totalPages - 1) {
                    html += `<li class="page-item"><a class="page-link">
                            <span class="select-page">...</span>
                            <input type="number" class="form-control page-input"/>
                        </a></li>`;
                }
                html += `<li class="page-item"><a class="page-link page" href="#">${totalPages}</a></li>`;
            }
        }

        html += `
        <li class="page-item ${currentPage === totalPages ? 'disabled' : ''}">
            <a class="page-link page" href="#" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
    `;
    }

    $pagination.html(html);
}