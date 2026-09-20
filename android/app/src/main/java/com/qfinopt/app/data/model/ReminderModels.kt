package com.qfinopt.app.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

enum class ReminderType(val label: String, val iconEmoji: String) {
    @SerializedName("SIP")
    SIP("SIP Due Date", "📅"),

    @SerializedName("EXIT")
    EXIT("Optimal Exit", "🎯"),

    @SerializedName("PRICE_TARGET")
    PRICE_TARGET("NAV Target / Dip", "🔔"),

    @SerializedName("TAX_SAVING")
    TAX_SAVING("Tax Season / ELSS", "🛡️"),

    @SerializedName("REVIEW")
    REVIEW("Portfolio Review", "📊")
}

enum class ReminderFrequency(val label: String) {
    @SerializedName("MONTHLY")
    MONTHLY("Monthly"),

    @SerializedName("ONCE")
    ONCE("One-time"),

    @SerializedName("WEEKLY")
    WEEKLY("Weekly")
}

data class CustomReminder(
    @SerializedName("id") val id: String = UUID.randomUUID().toString(),
    @SerializedName("title") val title: String,
    @SerializedName("fund_name") val fundName: String,
    @SerializedName("type") val type: ReminderType = ReminderType.SIP,
    @SerializedName("frequency") val frequency: ReminderFrequency = ReminderFrequency.MONTHLY,
    @SerializedName("day_of_month") val dayOfMonth: Int = 5,
    @SerializedName("date_string") val dateString: String = "",
    @SerializedName("amount") val amount: Double = 5000.0,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("is_enabled") val isEnabled: Boolean = true,
    @SerializedName("created_at") val createdAt: Long = System.currentTimeMillis()
)
