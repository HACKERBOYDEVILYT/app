package com.example.domain.engine

enum class PayoutMethod(
    val methodKey: String,
    val displayName: String,
    val minAmount: Double,
    val maxAmount: Double,
    val flatFee: Double,
    val percentageFee: Double,
    val estimatedArrival: String
) {
    PAYPAL(
        methodKey = "PAYPAL",
        displayName = "PayPal Payout",
        minAmount = 5.0,
        maxAmount = 500.0,
        flatFee = 0.25,
        percentageFee = 1.5,
        estimatedArrival = "12 - 24 Hours"
    ),
    CRYPTO_USDC(
        methodKey = "CRYPTO_USDC",
        displayName = "Crypto USDC (Polygon / Base)",
        minAmount = 5.0,
        maxAmount = 1000.0,
        flatFee = 0.50,
        percentageFee = 0.0,
        estimatedArrival = "Instant (< 5 mins)"
    ),
    BANK_TRANSFER(
        methodKey = "BANK_TRANSFER",
        displayName = "Direct Bank Wire / ACH",
        minAmount = 25.0,
        maxAmount = 2500.0,
        flatFee = 1.50,
        percentageFee = 0.5,
        estimatedArrival = "1 - 3 Business Days"
    ),
    GIFT_CARD(
        methodKey = "GIFT_CARD",
        displayName = "Amazon / Digital Gift Card",
        minAmount = 5.0,
        maxAmount = 100.0,
        flatFee = 0.0,
        percentageFee = 0.0,
        estimatedArrival = "Instant Code Delivery"
    );

    companion object {
        fun fromKey(key: String): PayoutMethod {
            return entries.firstOrNull { it.methodKey.equals(key, ignoreCase = true) } ?: PAYPAL
        }
    }
}

data class PayoutCalculationResult(
    val isValid: Boolean,
    val method: PayoutMethod,
    val grossAmount: Double,
    val flatFee: Double,
    val percentageFeeAmount: Double,
    val totalFee: Double,
    val netAmount: Double,
    val errorMessage: String? = null
)

class PayoutCalculationEngine {

    fun calculatePayout(method: PayoutMethod, grossAmount: Double, availableBalance: Double): PayoutCalculationResult {
        if (grossAmount < method.minAmount) {
            return PayoutCalculationResult(
                isValid = false,
                method = method,
                grossAmount = grossAmount,
                flatFee = 0.0,
                percentageFeeAmount = 0.0,
                totalFee = 0.0,
                netAmount = 0.0,
                errorMessage = "Minimum withdrawal for ${method.displayName} is $${"%.2f".format(method.minAmount)}"
            )
        }

        if (grossAmount > method.maxAmount) {
            return PayoutCalculationResult(
                isValid = false,
                method = method,
                grossAmount = grossAmount,
                flatFee = 0.0,
                percentageFeeAmount = 0.0,
                totalFee = 0.0,
                netAmount = 0.0,
                errorMessage = "Maximum single withdrawal for ${method.displayName} is $${"%.2f".format(method.maxAmount)}"
            )
        }

        if (grossAmount > availableBalance) {
            return PayoutCalculationResult(
                isValid = false,
                method = method,
                grossAmount = grossAmount,
                flatFee = 0.0,
                percentageFeeAmount = 0.0,
                totalFee = 0.0,
                netAmount = 0.0,
                errorMessage = "Requested amount ($${"%.2f".format(grossAmount)}) exceeds available balance ($${"%.2f".format(availableBalance)})"
            )
        }

        val percentageFee = grossAmount * (method.percentageFee / 100.0)
        val rawTotalFee = method.flatFee + percentageFee
        val totalFee = Math.round(rawTotalFee * 100.0) / 100.0
        val netAmount = (grossAmount - totalFee).coerceAtLeast(0.0)
        val roundedNetAmount = Math.round(netAmount * 100.0) / 100.0

        return PayoutCalculationResult(
            isValid = true,
            method = method,
            grossAmount = grossAmount,
            flatFee = method.flatFee,
            percentageFeeAmount = Math.round(percentageFee * 100.0) / 100.0,
            totalFee = totalFee,
            netAmount = roundedNetAmount,
            errorMessage = null
        )
    }
}
