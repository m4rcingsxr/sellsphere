package com.sellsphere.admin.report;

import com.sellsphere.admin.setting.SettingService;
import com.sellsphere.common.entity.Setting;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class ReportController {

    private final SettingService settingService;

    @GetMapping("/reports")
    public String viewSalesReports(HttpServletRequest request) {
        List<Setting> currencySettings = settingService.getCurrencySettings();
        currencySettings.forEach(setting -> request.setAttribute(setting.getKey(), setting.getValue()));

        return "report/reports";
    }

}
