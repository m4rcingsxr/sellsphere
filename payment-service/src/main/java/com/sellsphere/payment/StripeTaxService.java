package com.sellsphere.payment;

import com.sellsphere.common.entity.Country;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.tax.Registration;
import com.stripe.param.tax.RegistrationCreateParams;

import java.util.List;

import static com.sellsphere.payment.Constants.API_KEY;

public class StripeTaxService {

    static {
        Stripe.apiKey = API_KEY;
    }

    public void registerTax(List<Country> countries) throws StripeException {
        for (Country country : countries) {
            RegistrationCreateParams params =
                    RegistrationCreateParams.builder()
                            .setCountry(country.getCode().toUpperCase())
                            .setCountryOptions(
                                    RegistrationCreateParams.CountryOptions.builder()
                                            .setUs(
                                                    RegistrationCreateParams.CountryOptions.Us.builder()
                                                            .setState("CA")
                                                            .setType(
                                                                    RegistrationCreateParams.CountryOptions.Us.Type.STATE_SALES_TAX)
                                                            .build()
                                            )
                                            .build()
                            )
                            .setActiveFrom(RegistrationCreateParams.ActiveFrom.NOW)
                            .build();

            Registration.create(params);
        }
    }
}
