"use strict"

$(function () {

    $(".vote-container").on("click", ".vote-up, .vote-down", async function (event) {
        event.preventDefault();
        console.log("ABC");

        const url = event.currentTarget.href;
        const $currentThumb = $(event.currentTarget).find("i");

        try {
            const response = await ajaxUtil.post(url);


            if (response.successful) {
                if (url.endsWith("up")) {
                    if ($currentThumb.hasClass("fa-solid")) {
                        $currentThumb.addClass("fa-regular");
                        $currentThumb.removeClass("fa-solid");
                    } else {
                        $currentThumb.addClass("fa-solid");
                        $currentThumb.removeClass("fa-regular");

                        const $downThumb = $(event.currentTarget).parent().find(".vote-down i");
                        if ($downThumb.hasClass("fa-solid")) {
                            $downThumb.addClass("fa-regular");
                            $downThumb.removeClass("fa-solid");
                        }
                    }
                } else if (url.endsWith("down")) {
                    if ($currentThumb.hasClass("fa-solid")) {
                        $currentThumb.addClass("fa-regular");
                        $currentThumb.removeClass("fa-solid");
                    } else {
                        $currentThumb.addClass("fa-solid");
                        $currentThumb.removeClass("fa-regular");

                        const $upThumb = $(event.currentTarget).parent().find(".vote-up i");
                        if ($upThumb.hasClass("fa-solid")) {
                            $upThumb.addClass("fa-regular");
                            $upThumb.removeClass("fa-solid");
                        }
                    }
                }

                $(event.currentTarget).parent().find(".vote-count").text(`${response.voteCount} votes`);
            } else {
                showModalDialog('info_modal', 'Warning', response.message, 'text');
            }
        } catch (error) {
            console.error(error);
            showErrorModal(error.response);
        }

    })
});

