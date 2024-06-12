package com.sellsphere.easyship;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String BASE_URL = "https://api.easyship.com/2023-01";
    public static final String BEARER_TOKEN = System.getenv("EASYSHIP_API_KEY");

    // product require unit of length to be cm
    public static final String UNIT_OF_LENGTH = "cm";

    // product require unit of weight to be kg
    public static final String UNIT_OF_WEIGHT = "kg";

}
