package com.sellsphere.easyship;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String BASE_URL = "https://api.easyship.com/2023-01";
    public static final String BEARER_TOKEN = System.getenv("EASYSHIP_API_KEY");

}
