const formatPriceUtil = {

    formatPrice(productPrice) {
        if(!currencySettings) {
            throw new Error("Currency settings must be defined!")
        }

        let formattedPrice = '';

        // Add currency_conversion symbol before or after the price based on the currency_conversion symbol position
        if (currencySettings.currencySymbolPosition === 'BEFORE_PRICE') {
            formattedPrice += currencySettings.currencySymbol;
        }

        // Format the product price with the specified decimal digits
        let priceString = productPrice.toFixed(currencySettings.decimalDigits);

        // Add thousands separators and replace decimal point if necessary
        if (currencySettings.thousandsPointType === 'COMMA') {
            priceString = priceString.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        } else if (currencySettings.thousandsPointType === 'DOT') {
            priceString = priceString.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
        }

        if (currencySettings.decimalPointType === 'COMMA') {
            priceString = priceString.replace('.', ',');
        }

        formattedPrice += priceString;

        // Add currency_conversion symbol after the price if required
        if (currencySettings.currencySymbolPosition === 'AFTER_PRICE') {
            formattedPrice += currencySettings.currencySymbol;
        }

        return formattedPrice;
    }

}

