package com.sellsphere.admin.setting;

import com.sellsphere.common.entity.Setting;
import com.sellsphere.common.entity.SettingCategory;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class SettingDataGenerator {

    public static List<Setting> generateGeneralSettings() {
        List<Setting> settings = new ArrayList<>();

        Setting siteLogo = new Setting();
        siteLogo.setKey("SITE_LOGO");
        siteLogo.setValue("/site-logo/DummyLogo.png");
        siteLogo.setCategory(SettingCategory.GENERAL);
        settings.add(siteLogo);

        Setting siteName = new Setting();
        siteName.setKey("SITE_NAME");
        siteName.setValue("DummySite");
        siteName.setCategory(SettingCategory.GENERAL);
        settings.add(siteName);

        Setting copyright = new Setting();
        copyright.setKey("COPYRIGHT");
        copyright.setValue("Copyright (C) 2021 Dummy Ltd.");
        copyright.setCategory(SettingCategory.GENERAL);
        settings.add(copyright);

        List<Setting> currencySettings = generateCurrencySettings();
        settings.addAll(currencySettings);

        return settings;
    }

    public static List<Setting> generateCurrencySettings() {
        List<Setting> settings = new ArrayList<>();

        Setting currencyId = new Setting();
        currencyId.setKey("CURRENCY_ID");
        currencyId.setValue("1");
        currencyId.setCategory(SettingCategory.CURRENCY);
        settings.add(currencyId);

        Setting currencySymbol = new Setting();
        currencySymbol.setKey("CURRENCY_SYMBOL");
        currencySymbol.setValue("$");
        currencySymbol.setCategory(SettingCategory.CURRENCY);
        settings.add(currencySymbol);

        Setting currencySymbolPosition = new Setting();
        currencySymbolPosition.setKey("CURRENCY_SYMBOL_POSITION");
        currencySymbolPosition.setValue("AFTER PRICE");
        currencySymbolPosition.setCategory(SettingCategory.CURRENCY);
        settings.add(currencySymbolPosition);

        Setting decimalPointType = new Setting();
        decimalPointType.setKey("DECIMAL_POINT_TYPE");
        decimalPointType.setValue("POINT");
        decimalPointType.setCategory(SettingCategory.CURRENCY);
        settings.add(decimalPointType);

        Setting decimalDigits = new Setting();
        decimalDigits.setKey("DECIMAL_DIGITS");
        decimalDigits.setValue("2");
        decimalDigits.setCategory(SettingCategory.CURRENCY);
        settings.add(decimalDigits);

        Setting thousandsPointType = new Setting();
        thousandsPointType.setKey("THOUSANDS_POINT_TYPE");
        thousandsPointType.setValue("COMMA");
        thousandsPointType.setCategory(SettingCategory.CURRENCY);
        settings.add(thousandsPointType);

        return settings;
    }

    public static List<Setting> generateMailServerSettings() {
        List<Setting> settings = new ArrayList<>();

        Setting mailHost = new Setting();
        mailHost.setKey("MAIL_HOST");
        mailHost.setValue("smtp.dummy.com");
        mailHost.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(mailHost);

        Setting mailPort = new Setting();
        mailPort.setKey("MAIL_PORT");
        mailPort.setValue("587");
        mailPort.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(mailPort);

        Setting mailUsername = new Setting();
        mailUsername.setKey("MAIL_USERNAME");
        mailUsername.setValue("DummyShop@gmail.com");
        mailUsername.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(mailUsername);

        Setting mailPassword = new Setting();
        mailPassword.setKey("MAIL_PASSWORD");
        mailPassword.setValue("dummy_password");
        mailPassword.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(mailPassword);

        Setting smtpAuth = new Setting();
        smtpAuth.setKey("SMTP_AUTH");
        smtpAuth.setValue("true");
        smtpAuth.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(smtpAuth);

        Setting smtpSecured = new Setting();
        smtpSecured.setKey("SMTP_SECURED");
        smtpSecured.setValue("true");
        smtpSecured.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(smtpSecured);

        Setting mailFrom = new Setting();
        mailFrom.setKey("MAIL_FROM");
        mailFrom.setValue("DummyShop@gmail.com");
        mailFrom.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(mailFrom);

        Setting mailSenderName = new Setting();
        mailSenderName.setKey("MAIL_SENDER_NAME");
        mailSenderName.setValue("Dummy Company");
        mailSenderName.setCategory(SettingCategory.MAIL_SERVER);
        settings.add(mailSenderName);

        return settings;
    }

    public static List<Setting> generateMailTemplateSettings() {
        List<Setting> settings = new ArrayList<>();

        Setting customerEmailVerifySubject = new Setting();
        customerEmailVerifySubject.setKey("CUSTOMER_EMAIL_VERIFY_SUBJECT");
        customerEmailVerifySubject.setValue("Please verify your registration to continue shopping");
        customerEmailVerifySubject.setCategory(SettingCategory.MAIL_TEMPLATES);
        settings.add(customerEmailVerifySubject);

        Setting customerEmailVerifyContent = new Setting();
        customerEmailVerifyContent.setKey("CUSTOMER_EMAIL_VERIFY_CONTENT");
        customerEmailVerifyContent.setValue(
                "<p>Dear [[NAME]]</p>,<p>Thank you for registering on our website. </p>,<p>Please confirm your account by clicking the verification link</p>,<p>[[URL]]</p>,<p>Best regards, Dummy Team.</p>"
        );
        customerEmailVerifyContent.setCategory(SettingCategory.MAIL_TEMPLATES);
        settings.add(customerEmailVerifyContent);

        Setting orderConfirmationSubject = new Setting();
        orderConfirmationSubject.setKey("ORDER_CONFIRMATION_SUBJECT");
        orderConfirmationSubject.setValue("Order confirmation");
        orderConfirmationSubject.setCategory(SettingCategory.MAIL_TEMPLATES);
        settings.add(orderConfirmationSubject);

        Setting orderConfirmationContent = new Setting();
        orderConfirmationContent.setKey("ORDER_CONFIRMATION_CONTENT");
        orderConfirmationContent.setValue(
                "<p>Dear [[NAME]]</p><p>This email is to confirm that you have successfully placed an order through our website. Please review the following order summary:</p><p>Order ID: [[orderId]]<br>Order time: [[orderTime]]<br>Ship to: [[shippingAddress]]<br>Total: [[total]]<br>Payment Method: [[paymentMethod]]</p><p>We're currently processing your order. Click here to view full details of your order on our website. [login required]</p><p>Thank you for placing order on our website.</p><p>Best regards, Dummy Team.</p>"
        );
        orderConfirmationContent.setCategory(SettingCategory.MAIL_TEMPLATES);
        settings.add(orderConfirmationContent);

        return settings;
    }

    public static List<Setting> generatePaymentSettings() {
        List<Setting> settings = new ArrayList<>();

        Setting paypalBaseUrl = new Setting();
        paypalBaseUrl.setKey("PAYPAL_BASE_URL");
        paypalBaseUrl.setValue("https://api-m.sandbox.paypal.com");
        paypalBaseUrl.setCategory(SettingCategory.PAYMENT);
        settings.add(paypalBaseUrl);

        Setting paypalClientId = new Setting();
        paypalClientId.setKey("PAYPAL_CLIENT_ID");
        paypalClientId.setValue("dummy_client_id");
        paypalClientId.setCategory(SettingCategory.PAYMENT);
        settings.add(paypalClientId);

        Setting paypalClientSecret = new Setting();
        paypalClientSecret.setKey("PAYPAL_CLIENT_SECRET");
        paypalClientSecret.setValue("dummy_client_secret");
        paypalClientSecret.setCategory(SettingCategory.PAYMENT);
        settings.add(paypalClientSecret);

        return settings;
    }

}
