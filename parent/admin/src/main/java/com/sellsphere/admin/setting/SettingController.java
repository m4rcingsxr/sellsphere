package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Constants;
import com.sellsphere.common.entity.Country;
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

/**
 * SettingController manages the user interface for application settings.
 * It handles displaying and saving application settings, and file uploads for settings.
 *
 * <p>This controller interacts with {@link SettingService} for business logic and
 * {@link CountryRepository} to retrieve country-related data for display.</p>
 */
@RequiredArgsConstructor
@Controller
public class SettingController {

    private final SettingService settingService;
    private final CountryRepository countryRepository;

    /**
     * Displays the settings form with all current settings and country data.
     *
     * <p>This method retrieves all settings from the database and populates them into the model,
     * along with the list of available countries for selection in the settings form.</p>
     *
     * @param model the Model object to pass data to the view.
     * @return the view name "setting/settings".
     */
    @GetMapping("/settings")
    public String showSettingForm(Model model) {
        List<Setting> settingList = settingService.listAllSettings();
        List<Country> countryList = countryRepository.findAll();

        prepareModelForSettings(model, settingList);
        model.addAttribute("countryList", countryList);

        return "setting/settings";
    }

    /**
     * Handles the saving of application settings.
     *
     * <p>This method accepts form data, including an optional file upload, and passes the
     * request to {@link SettingService} to process and save the settings.
     * If successful, it redirects back to the settings page with a success message.</p>
     *
     * @param file the uploaded image file (if any).
     * @param request the HttpServletRequest containing form data.
     * @param ra RedirectAttributes for passing flash messages.
     * @return a redirect to the settings page.
     * @throws IOException if an error occurs during file handling.
     */
    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam(value = "newImage", required = false) MultipartFile file,
                               HttpServletRequest request, RedirectAttributes ra)
            throws IOException {
        settingService.save(request, file);
        ra.addFlashAttribute(Constants.SUCCESS_MESSAGE, "Settings successfully updated.");
        return "redirect:/settings";
    }

    /**
     * Prepares the model with the application settings.
     *
     * <p>This helper method adds each setting's key and value to the model so that they can be
     * displayed on the form. It also adds standard attributes such as the page title and the S3
     * base URI for file handling.</p>
     *
     * @param model the Model object to pass data to the view.
     * @param settingList the list of application settings to be added to the model.
     */
    private void prepareModelForSettings(Model model, List<Setting> settingList) {
        settingList.forEach(setting -> model.addAttribute(setting.getKey(), setting.getValue()));
        model.addAttribute("pageTitle", "Manage Settings");
        model.addAttribute("S3_BASE_URI", Constants.S3_BASE_URI);
    }

}