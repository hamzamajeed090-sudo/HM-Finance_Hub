package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fintrack_settings", Context.MODE_PRIVATE)

    private val _displayCurrency = MutableStateFlow(prefs.getString("display_currency", "PKR") ?: "PKR")
    val displayCurrency: StateFlow<String> = _displayCurrency.asStateFlow()

    private val _sarToPkrRate = MutableStateFlow(prefs.getFloat("sar_to_pkr_rate", 74.20f).toDouble())
    val sarToPkrRate: StateFlow<Double> = _sarToPkrRate.asStateFlow()

    private val _appLanguage = MutableStateFlow(
        if (prefs.getString("app_language", "en") == "ur") AppLanguage.UR else AppLanguage.EN
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _isPinLockEnabled = MutableStateFlow(prefs.getBoolean("pin_lock_enabled", false))
    val isPinLockEnabled: StateFlow<Boolean> = _isPinLockEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _hijriOffsetDays = MutableStateFlow(prefs.getInt("hijri_offset_days", 0))
    val hijriOffsetDays: StateFlow<Int> = _hijriOffsetDays.asStateFlow()

    fun setDisplayCurrency(currency: String) {
        prefs.edit().putString("display_currency", currency).apply()
        _displayCurrency.value = currency
    }

    fun setSarToPkrRate(rate: Double) {
        prefs.edit().putFloat("sar_to_pkr_rate", rate.toFloat()).apply()
        _sarToPkrRate.value = rate
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.code).apply()
        _appLanguage.value = language
    }

    fun setPinLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pin_lock_enabled", enabled).apply()
        _isPinLockEnabled.value = enabled
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun getPin(): String {
        return prefs.getString("pin_code", "1234") ?: "1234"
    }

    fun setPin(pin: String) {
        prefs.edit().putString("pin_code", pin).apply()
    }

    fun setHijriOffset(offset: Int) {
        prefs.edit().putInt("hijri_offset_days", offset).apply()
        _hijriOffsetDays.value = offset
    }
}
