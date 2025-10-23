plugins {
    // Mantienes las declaraciones con alias
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Y añades el plugin de Google Services que no está en el catalog
    id("com.google.gms.google-services") version "4.4.1" apply false
}