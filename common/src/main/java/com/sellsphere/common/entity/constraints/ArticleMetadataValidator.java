package com.sellsphere.common.entity.constraints;

import com.sellsphere.common.entity.ArticleType;
import com.sellsphere.common.entity.payload.ArticleMetadata;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ArticleMetadataValidator implements ConstraintValidator<ValidArticleMetadata, ArticleMetadata> {

    @Override
    public boolean isValid(ArticleMetadata metadata, ConstraintValidatorContext context) {
        ArticleType articleType = metadata.getArticle() != null ? metadata.getArticle().getArticleType() : null;
        boolean isValid = true;


        // Validate article type NAVIGATION
        if (articleType == ArticleType.NAVIGATION) {
            if (metadata.getItemNumber() == null) {
                isValid = false;
                context.buildConstraintViolationWithTemplate("Article type 'NAVIGATION' requires an item number.")
                        .addPropertyNode("itemNumber")
                        .addConstraintViolation();
            }
        }

        // Validate article type FOOTER
        if (articleType == ArticleType.FOOTER) {
            if (metadata.getSectionNumber() == null || metadata.getItemNumber() == null) {
                isValid = false;
                context.buildConstraintViolationWithTemplate(
                                "Footer article requires both section number and item number.")
                        .addPropertyNode("sectionNumber")
                        .addConstraintViolation();
            }

            // Ensure section header is present and not empty
            if (metadata.getSectionHeader() == null || metadata.getSectionHeader().length == 0) {
                isValid = false;
                context.buildConstraintViolationWithTemplate("Footer article requires valid section headers.")
                        .addPropertyNode("sectionHeader")
                        .addConstraintViolation();
            } else {
                // Validate each section header
                for (int i = 0; i < metadata.getSectionHeader().length; i++) {
                    String sectionHeader = metadata.getSectionHeader()[i];
                    if (sectionHeader == null || sectionHeader.isBlank()) {
                        isValid = false;
                        context.buildConstraintViolationWithTemplate(
                                        "Footer article requires a valid section header for section " + (i + 1) + ".")
                                .addPropertyNode("sectionHeader")
                                .addConstraintViolation();
                    }
                }
            }
        }

        // Validate article type PROMOTION
        if (articleType == ArticleType.PROMOTION) {
            if (metadata.getPromotionName() == null || metadata.getPromotionName().isBlank()) {
                isValid = false;
                context.buildConstraintViolationWithTemplate("Promotion article requires a promotion name.")
                        .addPropertyNode("promotionName")
                        .addConstraintViolation();
            }

            if (metadata.getSelectedProducts() == null || metadata.getSelectedProducts().isEmpty()) {
                isValid = false;
                context.buildConstraintViolationWithTemplate(
                                "Promotion article requires at least one associated product.")
                        .addPropertyNode("selectedProducts")
                        .addConstraintViolation();
            }
        }

        return isValid;
    }
}

