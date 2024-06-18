package com.sellsphere.client.order;

import com.sellsphere.client.address.AddressRepository;
import com.sellsphere.client.customer.CustomerService;
import com.sellsphere.client.setting.CountryRepository;
import com.sellsphere.client.shoppingcart.CartItemRepository;
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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final EasyshipService easyshipService;
    private final CustomerService customerService;
    private final AddressRepository addressRepository;
    private final CountryRepository countryRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Order createOrder(String customerEmail, Address destination, String courierId,
                             String currencyCode, String addressId)
            throws CustomerNotFoundException, CountryNotFoundException, AddressNotFoundException {

        Customer customer = customerService.getByEmail(customerEmail);
        com.sellsphere.common.entity.Address destinationAddress;

        // selected
        if (addressId == null || addressId.isEmpty()) {
            var address = new com.sellsphere.common.entity.Address();
            address.setAddressLine1(destination.getLine1());
            address.setAddressLine2(destination.getLine2());
            String[] fullName = destination.getContactName().split(" ");
            address.setFirstName(fullName[0]);
            address.setLastName(fullName[1]);

            address.setState(destination.getState());
            address.setPhoneNumber(destination.getContactPhone());
            address.setPostalCode(destination.getPostalCode());

            Country country = countryRepository.findByCode(
                    destination.getCountryAlpha2()).orElseThrow(CountryNotFoundException::new);

            address.setCountry(country);
            address.setCity(destination.getCity());
            address.setCustomer(customer);

            if (customer.getAddresses() == null || customer.getAddresses().isEmpty()) {
                address.setPrimary(true);
            }

            destinationAddress = addressRepository.save(address);
        } else {
            destinationAddress = addressRepository.findById(Integer.valueOf(addressId)).orElseThrow(AddressNotFoundException::new);
        }

        List<CartItem> cart = cartItemRepository.findByCustomer(customer);


        BigDecimal totalWeight = cart.stream()
                .map(item -> item.getProduct().getWeight().multiply(
                        BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RequestParcel parcel = new RequestParcel();
        parcel.setTotalActualWeight(totalWeight);

        for (CartItem cartItem : cart) {
            parcel.addItem(ParcelProductItemCreate.builder()
                                   .declaredCustomsValue(cartItem.getProduct().getDiscountPrice())
                                   .declaredCurrency(currencyCode.toUpperCase())
                                   .quantity(cartItem.getQuantity())
                                   .product(ParcelProductItemCreate.Product.builder()
                                                    .id(String.valueOf(
                                                            cartItem.getProduct().getEasyshipId()))
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

        BigDecimal subtotal = cart.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productCost = cart.stream()
                .map(item -> item.getProduct().getCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = subtotal.add(BigDecimal.valueOf(selectedRate.getTotalCharge()));

        Order order = Order.builder()
                .orderTime(LocalDateTime.now())
                .customer(customer)
                .total(total)
                .productCost(productCost)
                .subtotal(subtotal)
//            .tax(cart.getTax())
                // naive implementation:
                .deliverDate(LocalDate.from(
                        LocalDateTime.now().plusDays(selectedRate.getMaxDeliveryTime())))
                .destinationAddress(destinationAddress)
                .deliverDays(selectedRate.getMaxDeliveryTime())
                .shippingCost(BigDecimal.valueOf(selectedRate.getTotalCharge()))
                .shipment(
                        Shipment.builder()
                                .courierId(courierId)
                                .shipmentId(shipment.getShipment().getEasyshipShipmentId())
                                .deliveryState(shipment.getShipment().getDeliveryState())
                                .trackingPageUrl(shipment.getShipment().getTrackingPageUrl())
                                .deliveryState(shipment.getShipment().getDeliveryState())
                                .pickupState(shipment.getShipment().getPickupState())
                                .returnShipment(shipment.getShipment().isReturnShipment())
                                .labelGeneratedAt(shipment.getShipment().getLabelGeneratedAt())
                                .labelState(shipment.getShipment().getLabelState())
                                .labelPaidAt(shipment.getShipment().getLabelPaidAt())
                                .build()
                ).build();

        order = orderRepository.save(order);

        cart.forEach(order::addOrderDetail);

        orderRepository.save(order);

        return order;
    }

}
