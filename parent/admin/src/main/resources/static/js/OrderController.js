class OrderController {
    statusMap = null;

    constructor() {
        // Initialize the controller
    }

    /**
     * Initialize the controller by fetching status map and order tracks,
     * and setting up event listeners.
     */
    async init() {
        try {
            this.statusMap = await this.fetchOrderStatusMap();

            const orderTracks = await this.fetchOrderTracks($("#id").val());
            this.renderTracks(orderTracks);

            this.setupEventListeners();
        } catch (error) {
            console.error('Initialization failed:', error);
        }
    }

    /**
     * Setup event listeners for adding and deleting order tracks.
     */
    setupEventListeners() {
        $("#new-track").on("click", event => this.handleNewTrackClick(event));
        $("#order-tracks").on("click", ".delete-track", event => this.handleDeleteTrackClick(event));
        $("#order-tracks").on("change", "#status-select", event => this.handleStatusChange($(event.currentTarget).val()));
        $("#order-tracks").on("click", "#add-track", event => this.handleAddTrackClick(event));
    }

    /**
     * Handle the new track button click event.
     */
    handleNewTrackClick(event) {
        event.preventDefault();

        // Ensure that element does not exist
        if ($("#order-track-input").length > 0) return;

        $("#order-tracks").append(this.renderNewTrackInput());

        const statusSelect = $("#status-select");
        statusSelect.empty();

        Object.entries(this.statusMap).forEach(([key, value]) => {
            statusSelect.append(`<option value="${key}">${key}</option>`);
        });

        this.handleStatusChange(statusSelect.val());
    }

    /**
     * Handle the delete track button click event.
     */
    async handleDeleteTrackClick(event) {
        const id = $(event.currentTarget).attr("data-id");
        $(event.currentTarget).closest(".list-group-item").remove();

        if (id) {
            try {
                const removedTrack = await this.deleteTrack(id);
                if (removedTrack.status === 'REFUNDED') {
                    // If delete track that's refunded, show the + button
                    $("#new-track").parent().removeClass("d-none");
                }
            } catch (error) {
                console.error('Failed to delete track:', error);
            }
        }
    }

    /**
     * Handle the add track button click event.
     */
    async handleAddTrackClick(event) {
        const note = $("#note").val();
        const status = $("#status-select").val();
        const orderId = $("#id").val();


        if (!status) {
            if (!note) {
                throw new Error("Status and note must be provided")
            } else {
                throw new Error("Status must be provided")
            }
        }

        if (!note) {
            throw new Error("Note must be provided");
        }

        try {

            const savedOrderTrack = await this.addTrack(orderId, status, note);

            $("#order-tracks").append(this.renderOrderTrack(savedOrderTrack));
            $("#order-track-input").remove();

            if (savedOrderTrack.status === 'REFUNDED') {
                $("#new-track").parent().addClass("d-none");
            }
        } catch (error) {
            console.error('Failed to add track:', error);
        }
    }

    /**
     * Render the input fields for adding a new order track.
     */
    renderNewTrackInput() {
        return `
            <li class="list-group-item p-2" id="order-track-input">
                <div class="d-flex justify-content-end">
                    <a href="#"><i class="fa-solid fa-xmark fa-lg delete-track"></i></a>
                </div>

                <div class="row px-3 g-3">
                    <div class="col-sm-4">Status</div>
                    <div class="col-sm-8">
                        <select class="form-control" id="status-select"></select>
                    </div>  

                    <div class="col-sm-4">Note</div>
                    <div class="col-sm-8">
                        <textarea class="form-control" type="text" id="note" name="note"></textarea>
                    </div>
                </div>
                
                <div class="d-flex justify-content-center mt-3">
                    <button id="add-track" class="btn btn-primary">Add new order track</button>
                </div>
            </li>
        `;
    }

    /**
     * Render a single order track.
     */
    renderOrderTrack(track) {
        return `
            <li class="list-group-item p-2">
                <div class="d-flex justify-content-end">
                    <a href="#" data-id="${track.id}" class="link-primary delete-track"><i class="fa-solid fa-xmark fa-lg"></i></a>
                </div>

                <div class="row px-3">
                    <div class="col-sm-4">Status</div>
                    <div class="col-sm-8">${track.status}</div>

                    <div class="col-sm-4">Note</div>
                    <div class="col-sm-8">${track.note}</div>
                </div>

                <div class="d-flex justify-content-end">
                    <span class="text-end fw-lighter">${track.updatedTime}</span>
                </div>
            </li>
        `;
    }

    /**
     * Handle the status change event.
     */
    handleStatusChange(status) {
        const note = this.statusMap[status];
        $("#note").val(note);
    }

    /**
     * Render all order tracks.
     */
    renderTracks(orderTracks) {
        const $tracks = $("#order-tracks");
        $tracks.empty();
        orderTracks.forEach(track => {
            $tracks.append(this.renderOrderTrack(track));
        });
    }

    /**
     * Add a new order track by sending a POST request.
     */
    async addTrack(orderId, status, note) {
        try {
            return await ajaxUtil.post(`${MODULE_URL}orders/add-track`, {orderId, status, note});
        } catch (error) {
            console.error('Failed to add order track:', error);
            throw error;
        }
    }

    /**
     * Delete an order track by sending a DELETE request.
     */
    async deleteTrack(trackId) {
        try {
            return await ajaxUtil.deleteReturnBody(`${MODULE_URL}orders/remove-track?id=${trackId}`);
        } catch (error) {
            console.error('Failed to delete order track:', error);
            throw error;
        }
    }

    /**
     * Fetch the order status map.
     */
    async fetchOrderStatusMap() {
        try {
            return await ajaxUtil.get(`${MODULE_URL}orders/status-list`);
        } catch (error) {
            console.error('Failed to fetch order status map:', error);
            throw error;
        }
    }

    /**
     * Fetch the order tracks for a specific order.
     */
    async fetchOrderTracks(orderId) {
        try {
            return await ajaxUtil.get(`${MODULE_URL}orders/order-tracks?orderId=${orderId}`);
        } catch (error) {
            console.error('Failed to fetch order tracks:', error);
            throw error;
        }
    }
}

// Initialize the OrderController
new OrderController().init();