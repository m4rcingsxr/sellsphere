"use strict";

$(function () {
    initializeStateSettings();
});

/**
 * Initializes the application by loading countries and setting up event listeners.
 */
function initializeStateSettings() {
    debug("Initializing application...");
    showFullScreenSpinner();
    loadCountries('countriesStateSettings')
        .then(loadStates)
        .catch(error => {
            console.error("Error initializing application:", error);
            showErrorModal(error.message || "An error occurred during initialization.");
        })
        .finally(() => {
            hideFullScreenSpinner();
            debug("Initialization complete.");
        });
    initStateListeners();
}

/**
 * Sets up event listeners for various UI elements.
 */
function initStateListeners() {
    debug("State Setting up event listeners...");
    $("#updateCountriesStateSettings").on("click", () => {
        debug("Update countries state settings clicked.");
        showFullScreenSpinner();
        loadCountries('countriesStateSettings')
            .then(loadStates)
            .catch(error => {
                console.error("Error updating countries state settings:", error);
                showErrorModal(error.message || "An error occurred while updating countries state settings.");
            })
            .finally(() => hideFullScreenSpinner());
    });
    $("#countriesStateSettings").on("change", loadStatesOnCountryChange);
    $("#states").on("change", handleStateSelectionChange);
    $("#newState").on("click", handleNewState);
    $("#updateState").on("click", handleUpdateState);
    $("#deleteState").on("click", handleDeleteState);
    $("#hideStateForm").on("click", handleHideStateForm);
    debug("State Event listeners set up.");
}

/**
 * Handles state selection change by updating the state input field with the selected state name.
 */
function handleStateSelectionChange() {
    const selectedStateName = $("#states option:selected").text();
    debug(`State selection changed to: ${selectedStateName}`);
    $("#state").val(selectedStateName || "");
}

/**
 * Loads states based on the selected country.
 */
function loadStatesOnCountryChange() {
    debug("Country selection changed. Loading states...");
    showFullScreenSpinner();
    loadStates()
        .catch(error => {
            console.error("Error loading states on country change:", error);
            showErrorModal(error.message || "An error occurred while loading states on country change.");
        })
        .finally(() => hideFullScreenSpinner());
}

/**
 * Loads states from the server and populates the state dropdown.
 */
async function loadStates() {
    try {
        debug("Loading states...");
        const $stateSelect = $("#states");
        const states = await fetchStates();

        $stateSelect.empty();
        states.forEach((state) => {
            $stateSelect.append(generateStateOptionHtml(state));
        });

        handleStateSelectionChange();
        debug("States loaded and dropdown populated.");
    } catch (error) {
        console.error("Error loading states:", error);
        throw new Error("Failed to load states.");
    }
}

/**
 * Generates HTML option element for a state.
 * @param {Object} state - The state object.
 * @returns {string} - The HTML string for the state option.
 */
function generateStateOptionHtml(state) {
    return `<option value="${state.id}">${state.name}</option>`;
}

/**
 * Fetches states from the server based on the selected country.
 * @returns {Promise<Array>} - A promise that resolves to an array of states.
 */
async function fetchStates() {
    try {
        const countryId = $("#countriesStateSettings").val();
        const url = `${MODULE_URL}states/by_country/${countryId}`;
        debug(`Fetching states for country ID: ${countryId}`);
        const states = await ajaxUtil.get(url);
        debug(`States fetched: ${JSON.stringify(states)}`);
        return states;
    } catch (error) {
        console.error("Error fetching states:", error);
        throw new Error("Failed to fetch states.");
    }
}

/**
 * Hides the state form and clears the input field for new state name.
 */
function hideStateForm() {
    debug("Hiding state form...");
    $("#newStateName").val("");
    $("#stateFormCollapse").collapse("hide");
    debug("State form hidden.");
}

/**
 * Handles the creation of a new state.
 */
function handleNewState() {
    const state = {
        name: $("#newStateName").val(),
        countryId: $("#countriesStateSettings").val()
    };
    debug(`Creating new state: ${JSON.stringify(state)}`);

    showFullScreenSpinner();
    saveState(state)
        .then(handlePostSave)
        .then(() => {
            hideStateForm();
            debug("Successfully saved new state.");
            showSuccessAlert(`Successfully saved new state ${state.name}`, 'stateSettings')
        })
        .catch(error => {
            console.error("Error saving new state:", error);
            showErrorModal(error.message || "An error occurred while saving the new state.");
        })
        .finally(() => hideFullScreenSpinner());
}

/**
 * Handles the update of an existing state.
 */
function handleUpdateState() {
    const state = {
        id: $("#states").val(),
        name: $("#state").val(),
        countryId: $("#countriesStateSettings").val()
    };
    debug(`Updating state: ${JSON.stringify(state)}`);

    showFullScreenSpinner();
    saveState(state)
        .then(handlePostSave)
        .then(() => {
            hideStateForm();
            showSuccessAlert(`Successfully updated state ${state.name}`, 'stateSettings')
            debug("Successfully updated state.");
        })
        .catch(error => {
            console.error("Error updating state:", error);
            showErrorModal(error.message || "An error occurred while updating the state.");
        })
        .finally(() => hideFullScreenSpinner());
}

/**
 * Handles the deletion of an existing state.
 */
function handleDeleteState() {
    const stateId = $("#states").val();
    debug(`Deleting state ID: ${stateId}`);

    showFullScreenSpinner();
    deleteState(stateId)
        .then(() => {
            $("#states option:selected").remove();
            handleStateSelectionChange();
            hideStateForm();
            showSuccessAlert(`Successfully removed state with ID: ${stateId}`, 'stateSettings')
            debug("Successfully deleted state.");
        })
        .catch(error => {
            console.error("Error deleting state:", error);
            showErrorModal(error.message || "An error occurred while deleting the state.");
        })
        .finally(() => hideFullScreenSpinner());
}

/**
 * Saves a state to the server.
 * @param {Object} state - The state object to save.
 * @returns {Promise<Object>} - A promise that resolves to the saved state object.
 */
async function saveState(state) {
    try {
        const url = `${MODULE_URL}states/save`;
        debug(`Saving state: ${JSON.stringify(state)}`);
        const savedState = await ajaxUtil.post(url, state);
        debug(`State saved: ${JSON.stringify(savedState)}`);
        return savedState;
    } catch (error) {
        console.error("Error saving state:", error);
        throw new Error("Failed to save state.");
    }
}

/**
 * Deletes a state from the server.
 * @param {string} stateId - The ID of the state to delete.
 * @returns {Promise} - A promise that resolves when the state is deleted.
 */
async function deleteState(stateId) {
    try {
        const url = `${MODULE_URL}states/delete/${stateId}`;
        debug(`Deleting state with ID: ${stateId}`);
        await ajaxUtil.delete(url);
        debug(`State with ID ${stateId} deleted.`);
    } catch (error) {
        console.error("Error deleting state:", error);
        throw new Error("Failed to delete state.");
    }
}

/**
 * Handles the post-save actions by reloading states and updating the UI.
 * @param {Object} state - The saved state object.
 * @returns {Promise} - A promise that resolves when the actions are complete.
 */
async function handlePostSave(state) {
    try {
        debug(`Post-save actions for state: ${JSON.stringify(state)}`);
        await loadStates();
        $("#countriesStateSettings").val(state.countryId);
        $("#states").val(state.id);
        handleStateSelectionChange();
        debug("Post-save actions complete.");
    } catch (error) {
        console.error("Error during post-save actions:", error);
        throw new Error("Failed to complete post-save actions.");
    }
}

/**
 * Hides the state form.
 */
function handleHideStateForm() {
    $("#stateFormCollapse").collapse("hide");
    debug('State form collapsed');
}