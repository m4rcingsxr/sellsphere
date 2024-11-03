document.addEventListener("DOMContentLoaded", function () {
    const navDropdownButton = document.getElementById("navDropdown");
    const sidebarHeader = document.getElementById("sidebarHeader");
    const overlay = document.getElementById("overlay");

    // Toggle sidebarHeader and overlay visibility when clicking the navDropdown button
    navDropdownButton.addEventListener("click", function () {
        $(".sidebar").removeClass("show");
        const isSidebarVisible = sidebarHeader.classList.toggle("show");
        overlay.style.display = isSidebarVisible ? "block" : "none"; // Show or hide overlay based on sidebar visibility
    });

    // Hide sidebarHeader and overlay when clicking on the overlay
    overlay.addEventListener("click", function () {
        $(".sidebar-nav").removeClass("show");
        $(".sidebar").removeClass("show");
        sidebarHeader.classList.remove("show");
        overlay.style.display = "none"; // Hide the overlay
    });
});