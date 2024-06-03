package com.sellsphere.easyship;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sellsphere.easyship.payload.AddressDto;


public class MainApp {





}

// shipping total =
// additional_services_surcharge - A fee added by the courier when additional services are added (e.g. delivery confirmation)
// estiamted_impoty_duty - An estimate of import duty that will be charged to the buyer when the shipment clears customs (only applicable for DDU incoterms). Null is returned when there is an insufficient subscription tier for feature taxes and duties.
// estimated_import_tax - An estimate of import taxes that will be charged to the buyer when the shipment clears customs (only applicable for DDU incoterms). Null is returned when there is an insufficient subscription tier for feature taxes and duties.
// fuel_surcharge - A fee added by the courier when fuel costs are high
// insurance_fee = The cost of the insurance policy purchased for this shipment


//! available_handover_options
//array of strings
//A list of one or more of dropoff, free_pickup, and paid_pickup

// statistics:
// cost_rank - Where this courier service ranks among the other offered options, in total price; 1 indicates the best value for money.
// delivery_time_rank - Where this courier service ranks among the other offered options, in minimum delivery time estimate; 1 indicates the fastest option.
//"max_delivery_time":15,
//      "min_delivery_time":10,

// full description
// description = Details that the user should know when preparing to hand over the shipment to the courier (e.g. pick-up or drop-off)

// courier_logo url - might be null
// courier_name - human readable
// ddp_handling_fee : A fee added by the courier when they pay import taxes and duties on the sender's behalf. Null is returned when there is an insufficient subscription tier for feature taxes and duties.
// currency_conversion - of rate

// total charge - most important!


