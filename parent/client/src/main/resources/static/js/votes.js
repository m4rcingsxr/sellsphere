$(function() {
    $(document).on("click", "#voteUp, #voteDown", function (event) {
        event.preventDefault();
        handleVote(event);
    });
})

async function handleVote(event) {
    const target = event.currentTarget;
    const url = target.href;
    const isUpVote = target.id === "voteUp";
    const upVoteIcon = $("#voteUp i");
    const downVoteIcon = $("#voteDown i");

    try {
        // Send the request to the same URL
        const response = await ajaxUtil.post(url);

        if (response.successful) {
            $("#voteCount").text(`${response.voteCount} Votes`);

            if (isUpVote) {
                // Check if upvote is already active
                if (upVoteIcon.hasClass("fa-solid")) {
                    // Undo upvote
                    upVoteIcon.removeClass("fa-solid").addClass("fa-regular");
                } else {
                    // Set upvote as active and reset downvote
                    upVoteIcon.addClass("fa-solid").removeClass("fa-regular");
                    downVoteIcon.removeClass("fa-solid").addClass("fa-regular");
                }
            } else {
                // Check if downvote is already active
                if (downVoteIcon.hasClass("fa-solid")) {
                    // Undo downvote
                    downVoteIcon.removeClass("fa-solid").addClass("fa-regular");
                } else {
                    // Set downvote as active and reset upvote
                    downVoteIcon.addClass("fa-solid").removeClass("fa-regular");
                    upVoteIcon.removeClass("fa-solid").addClass("fa-regular");
                }
            }
        }
    } catch (error) {
        console.error(error);
        showErrorModal(error.response);
    }
}
