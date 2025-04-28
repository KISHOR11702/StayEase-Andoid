plugins {
    alias(libs.plugins.android.application) apply false // ✅ Correct alias usage
    alias(libs.plugins.kotlin.android) apply false // ✅ Correct alias usage
    alias(libs.plugins.google.services) apply false
}

