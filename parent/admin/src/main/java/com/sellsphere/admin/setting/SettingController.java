package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Currency;
import com.sellsphere.common.entity.Setting;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/settings")
    public String showSettingForm(Model model) {
        List<Setting> settingList = settingService.listAllSettings();
        List<Currency> currencyList = settingService.listAllCurrencies();

        prepareModelForSettings(model, settingList);
        model.addAttribute("currencyList", currencyList);

        return "setting/settings";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam(value = "newImage", required = false) MultipartFile file,
                               HttpServletRequest request, RedirectAttributes ra)
            throws IOException {
        settingService.save(request, file);

        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Settings successfully updated.");

        return "redirect:/settings";
    }

    private void prepareModelForSettings(Model model, List<Setting> settingList) {
        settingList.forEach(setting -> model.addAttribute(setting.getKey(), setting.getValue()));
        model.addAttribute("pageTitle", "Manage Settings");
        model.addAttribute("S3_BASE_URI", Constants.S3_BASE_URI);
    }

}
