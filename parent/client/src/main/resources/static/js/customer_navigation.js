document.addEventListener("DOMContentLoaded", function () {
    const toggleSidebarButton = document.getElementById("toggleSidebar");
    const sidebar = document.querySelector("#sidebarNav");
    const overlay = document.getElementById("overlay");

    toggleSidebarButton.addEventListener("click", function () {
        const isSidebarVisible = sidebar.classList.toggle("show");
        $(".sidebar").removeClass("show");
        $("#sidebarHeader").removeClass("show");
        overlay.style.display = isSidebarVisible ? "block" : "none"; // Show or hide overlay based on sidebar visibility
    });

    overlay.addEventListener("click", function () {
        $(".sidebar-nav").removeClass("show");
        $(".sidebar").removeClass("show");
        sidebar.classList.remove("show");
        overlay.style.display = "none"; // Hide the overlay
    });
});
