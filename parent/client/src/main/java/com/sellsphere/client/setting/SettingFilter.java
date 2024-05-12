package com.sellsphere.client.setting;


import com.sellsphere.client.util.FilterUtil;
import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Setting;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter class responsible for loading general settings into the request
 * attributes.
 * It filters requests and loads general settings unless the request is for a
 * static resource.
 */
@RequiredArgsConstructor
@Component
public class SettingFilter extends GenericFilter {

    private final SettingService settingService;

    /**
     * Filters requests to load general settings unless the request is for a
     * static resource.
     *
     * @param request  The servlet request
     * @param response The servlet response
     * @param chain    The filter chain
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet-specific error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        boolean allowedUrl = FilterUtil.isRequestForResource(servletRequest);

        if (!allowedUrl) {
            loadGeneralSettings(request);
        }

        chain.doFilter(request, response);
    }

    private void loadGeneralSettings(ServletRequest request) {
        List<Setting> generalSettings = settingService.getGeneralSettings();
        generalSettings.forEach(setting -> request.setAttribute(setting.getKey(), setting.getValue()));

        request.setAttribute("S3_BASE_URI", Constants.S3_BASE_URI);
    }




}
