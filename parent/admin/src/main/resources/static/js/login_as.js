document.addEventListener('DOMContentLoaded', function () {
    const roleCards = document.querySelectorAll('.role-card');
    const hiddenInput = document.getElementById('selectedRole');

    roleCards.forEach(card => {
        card.addEventListener('click', function () {
            // Remove active class from all cards
            roleCards.forEach(c => c.classList.remove('active'));

            // Add active class to the clicked card
            this.classList.add('active');

            // Set the hidden input's value based on the data-role attribute
            hiddenInput.value = this.getAttribute('data-role');
        });
    });

});