// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.devtools.ksp") version "2.2.20-2.0.2" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    alias(libs.plugins.kotlin.android) apply false
}