package com.sellsphere.client.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class FilterUtil {

    private static final List<String> RESOURCE_EXTENSIONS = Arrays.asList(
            ".css", ".js", ".jpg", ".jpeg", ".png", ".gif", ".ico", ".svg", ".woff", ".woff2",
            ".ttf", ".eot", ".otf", ".json", ".xml", ".html", ".pdf", ".mp4", ".mp3"
    );

    public static boolean isRequestForResource(HttpServletRequest request) {
        String url = request.getRequestURL().toString();

        for (String allowedExtension : RESOURCE_EXTENSIONS) {
            if (url.endsWith(allowedExtension)) {
                return true;
            }
        }

        return false;
    }

}
