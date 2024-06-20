package com.sellsphere.client.setting;

import com.sellsphere.common.entity.CurrencyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SettingRestController {

    private final SettingService settingService;

    @GetMapping("/settings/currency")
    public ResponseEntity<Map<String, String>> getSettings() throws CurrencyNotFoundException {
        Map<String, String> settingsMap = new HashMap<>();
        settingsMap.put("currencyCode", settingService.getCurrencyCode(false));

        return ResponseEntity.ok(settingsMap);
    }

}
