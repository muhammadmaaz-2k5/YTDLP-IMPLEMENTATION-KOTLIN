package com.mediasaver.app.domain.model

/** The three subscription tiers offered on the Premium screen. */
enum class PremiumPlan(
    val id: String,
    val label: String,
    val price: String,
    val period: String,
    /** Shown as a per-month equivalent for the longer tiers, to make the discount legible. */
    val perMonthEquivalent: String?,
    val badge: String?
) {
    MONTHLY(
        id                 = "monthly",
        label              = "Monthly",
        price              = "$4.99",
        period             = "/ month",
        perMonthEquivalent = null,
        badge              = null
    ),
    QUARTERLY(
        id                 = "quarterly",
        label              = "3 Months",
        price              = "$11.99",
        period             = "/ 3 months",
        perMonthEquivalent = "$4.00 / mo",
        badge              = "Save 20%"
    ),
    YEARLY(
        id                 = "yearly",
        label              = "Yearly",
        price              = "$35.99",
        period             = "/ year",
        perMonthEquivalent = "$3.00 / mo",
        badge              = "Best Value"
    );

    companion object {
        fun fromId(id: String?): PremiumPlan? = entries.firstOrNull { it.id == id }
    }
}
