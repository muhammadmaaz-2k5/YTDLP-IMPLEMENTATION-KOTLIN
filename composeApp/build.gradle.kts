import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

compose.resources {
    packageOfResClass = "com.mediasaver.app.generated.resources"
}

kotlin {
    // ── Targets ────────────────────────────────────────────────────────────
    android {
        namespace = "com.mediasaver.app"
        compileSdk = 36
        minSdk = 28
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // ── Source sets ────────────────────────────────────────────────────────
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // Coroutines (common)
                implementation(libs.kotlinx.coroutines.core)

                // JSON serialization — used by HistoryStore for disk persistence
                implementation(libs.kotlinx.serialization.json)

                // Thumbnail image loading (video/image previews from yt-dlp metadata)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)

                // Real brand marks for the platform quick-open row (Simple Icons, CC0-licensed)
                implementation(libs.compose.icons.simple)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                // activity-compose lives in androidApp module, not the library
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.lifecycle.process)

                // Ads (AdMob) — all ad-SDK-touching code lives in this module's androidMain
                // (see data/ads/AdsController.kt) so androidApp only needs thin wiring calls,
                // same pattern as the download engine and notifications.
                implementation(libs.play.services.ads)
                implementation(libs.user.messaging.platform)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
