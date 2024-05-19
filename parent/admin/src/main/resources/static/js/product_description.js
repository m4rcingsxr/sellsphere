$(function() {
    initEditor('fullDescription');
    initEditor('shortDescription');

    initTooltip();
})

function initTooltip() {
    const tooltip = new bootstrap.Tooltip('#tooltip', {
        boundary: document.body
    })
}