package com.sellsphere.client.order;

import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.SettingService;
import com.sellsphere.client.shoppingcart.ShoppingCartController;
import com.sellsphere.client.shoppingcart.ShoppingCartRepository;
import com.sellsphere.common.entity.*;
import com.sellsphere.easyship.Constants;
import com.sellsphere.easyship.EasyshipService;
import com.sellsphere.easyship.payload.Address;
import com.sellsphere.easyship.payload.Rate;
import com.sellsphere.easyship.payload.shipment.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final EasyshipService easyshipService;
    private final CustomerService customerService;
    private final SettingService settingService;
    private final ShoppingCartController shoppingCartController;
    private final ShoppingCartRepository shoppingCartRepository;

    @Transactional
    public Order createOrder(String customerEmail, Address destination, String courierId, String currencyCode)
            throws CustomerNotFoundException {
        // Auto box selection applies when shipments are synced from API;
        // origin address - same as sender address (later dynamic)
        // destination address
        // packaging details - items - synchronized with easyship -
        // Selection algorithm

        Customer customer = customerService.getByEmail(customerEmail);
        ShoppingCart cart = customer.getCart();

        RequestParcel parcel = new RequestParcel();
        parcel.setTotalActualWeight(cart.getTotalWeight());

        for (CartItem cartItem : cart.getCartItems()) {
            parcel.addItem(ParcelProductItemCreate.builder()
                                   .declaredCustomsValue(cartItem.getProduct().getDiscountPrice())
                                   .declaredCurrency(currencyCode.toUpperCase())
                                   .quantity(cartItem.getQuantity())
                                   .product(ParcelProductItemCreate.Product.builder()
                                                    .id(String.valueOf(cartItem.getProduct().getEasyshipId()))
                                                    .build())
                                   .build());
        }

        // create shipment
        ShipmentRequest shipmentRequest = ShipmentRequest.builder()
                .originAddress(Address.builder()
                                       .city("Aartselaar")
                                       .postalCode("2060")
                                       .countryAlpha2("BE")
                                       .line1("Molenveldstraat 18")
                                       .currencyCode("EUR")
                                       .contactName("Marcin Seweryn")
                                       .contactPhone("730921452")
                                       .companyName("SellSphere")
                                       .contactEmail("marcinsewerynn@gmail.com")
                                       .build())
                .destinationAddress(destination)
                .parcels(List.of(parcel))
                .incoterms("DDU")
                .courierSelection(CourierSelection.builder()
                                          .selectedCourierId(courierId)
                                          .allowCourierFallback(false)
                                          .applyShippingRules(false)
                                          .listUnavailableCouriers(true)
                                          .build())
                .shippingSettings(ShippingSettings.builder()
                                          .additionalServices(
                                                  ShippingSettings.AdditionalServices.builder()
                                                          .qrCode("qr_code")
                                                          .build())
                                          .units(ShippingSettings.Units.builder()
                                                         .dimensions(Constants.UNIT_OF_LENGTH)
                                                         .weight(Constants.UNIT_OF_WEIGHT)
                                                         .build())
                                          .buyLabel(false)
                                          .buyLabelSynchronous(false)
                                          .printingOptions(
                                                  ShippingSettings.PrintingOptions.builder()
                                                          .format("pdf")
                                                          .label("A4")
                                                          .commercialInvoice("A4")
                                                          .build())
                                          .build())
                .build();

        ShipmentResponse shipment = easyshipService.createShipment(shipmentRequest);

        // check errors

        // validate if selected courier is available for this shipment
        Rate selectedRate = shipment.getShipment()
                .getRates()
                .stream()
                .filter(rate -> rate.getCourierId().equals(courierId))
                .findAny()
                .orElseThrow(IllegalStateException::new);

        Order order = Order.builder()
                .orderTime(LocalDateTime.now())
                .customer(customer)
                .total(cart.getTotal())
                .productCost(cart.getProductCost())
                .subtotal(cart.getSubtotal())
//                .tax(cart.getTax())
                // naive implementation:
                .deliverDate(LocalDate.from(LocalDateTime.now().plusDays(selectedRate.getMaxDeliveryTime())))
                .deliverDays(selectedRate.getMaxDeliveryTime())
                .shippingCost(BigDecimal.valueOf(selectedRate.getTotalCharge()))
                .shipment(
                    Shipment.builder()
                            .courierId(courierId)
                            .shipmentId(shipment.getShipment().getEasyshipShipmentId())
                            .deliveryState(shipment.getShipment().getDeliveryState())
                            .tackingPageUrl(shipment.getShipment().getTrackingPageUrl())
                            .deliveryState(shipment.getShipment().getDeliveryState())
                            .pickupState(shipment.getShipment().getPickupState())
                            .returnShipment(shipment.getShipment().isReturnShipment())
                            .labelGeneratedAt(shipment.getShipment().getLabelGeneratedAt())
                            .labelState(shipment.getShipment().getLabelState())
                            .labelPaidAt(shipment.getShipment().getLabelPaidAt())
                            .build()
                ).build();

        customer.getCart().getCartItems().forEach(order::addOrderDetail);

        orderRepository.save(order);

        shoppingCartRepository.delete(customer.getCart());

        return order;
    }

}
