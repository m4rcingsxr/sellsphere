const formatPriceUtil = {

    formatPrice(productPrice) {
        if(!CURRENCY_SETTINGS) {
            throw new Error("Currency settings must be defined!")
        }

        let formattedPrice = '';

        // Add currency symbol before or after the price based on the currency symbol position
        if (CURRENCY_SETTINGS.currencySymbolPosition === 'Before price') {
            formattedPrice += CURRENCY_SETTINGS.currencySymbol;
        }

        // Format the product price with the specified decimal digits
        let priceString = productPrice.toFixed(CURRENCY_SETTINGS.decimalDigits);

        // Add thousands separators and replace decimal point if necessary
        if (CURRENCY_SETTINGS.thousandsPointType === 'COMMA') {
            priceString = priceString.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
        } else if (CURRENCY_SETTINGS.thousandsPointType === 'DOT') {
            priceString = priceString.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
        }

        if (CURRENCY_SETTINGS.decimalPointType === 'COMMA') {
            priceString = priceString.replace('.', ',');
        }

        formattedPrice += priceString;

        // Add currency symbol after the price if required
        if (CURRENCY_SETTINGS.currencySymbolPosition === 'After price') {
            formattedPrice += CURRENCY_SETTINGS.symbol;
        }

        return formattedPrice;
    }

}

