package com.sellsphere.admin.order;

import com.sellsphere.common.entity.OrderNotFoundException;
import com.sellsphere.common.entity.OrderStatus;
import com.sellsphere.common.entity.OrderTrackNotFoundException;
import com.sellsphere.common.entity.payload.OrderTrackDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderRestController {

    private final OrderService orderService;

    @GetMapping("/status-list")
    public ResponseEntity<Map<String, String>> getOrderStatusMap() {
        Map<String, String> statusMap = OrderStatus.getOrderStatusMap();
        return ResponseEntity.ok(statusMap);
    }

    @GetMapping("/order-tracks")
    public ResponseEntity<List<OrderTrackDTO>> getOrderTracks(
            @RequestParam Integer orderId
    ) {
        List<OrderTrackDTO> orderTracks = orderService.listOrderTracks(orderId);
        return ResponseEntity.ok(orderTracks);
    }

    @DeleteMapping("/remove-track")
    public ResponseEntity<OrderTrackDTO> removeOrderTrack(
            @RequestParam Integer id
    ) throws OrderTrackNotFoundException {
        OrderTrackDTO track = orderService.deleteTrack(id);

        return ResponseEntity.ok(track);
    }

    @PostMapping("/add-track")
    public ResponseEntity<OrderTrackDTO> addOrderTracking(@RequestBody OrderTrackDTO orderDto)
            throws OrderNotFoundException {
        OrderStatus orderStatus = OrderStatus.valueOf(orderDto.getStatus());
        OrderTrackDTO track = orderService.addOrderTrack(orderDto.getOrderId(), orderStatus,
                                                                 orderDto.getNote()
        );

        return ResponseEntity.ok(track);
    }

}
