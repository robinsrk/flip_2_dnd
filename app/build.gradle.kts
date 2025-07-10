plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.dagger.hilt.android")
	id("kotlin-kapt")
	alias(libs.plugins.compose.compiler)
}

android {
	namespace = "dev.robin.flip_2_dnd"
	compileSdk = 36

	defaultConfig {
		applicationId = "dev.robin.flip_2_dnd"
		minSdk = 23
		targetSdk = 35
		versionCode = 601
		versionName = "6.0.1"
		vectorDrawables {
			useSupportLibrary = true
		}
	}

	lint {
		baseline = file("lint-baseline.xml")
		abortOnError = false
	}

	buildTypes {
		release {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions {
		jvmTarget = "17"
	}

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.21"
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	implementation("androidx.core:core-ktx:1.17.0-beta01")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
	implementation("androidx.activity:activity-compose:1.12.0-alpha04")
	implementation(platform("androidx.compose:compose-bom:2025.06.01"))
	implementation("androidx.compose.ui:ui:1.9.0-beta02")
	implementation("androidx.compose.ui:ui-graphics:1.9.0-beta02")
	implementation("androidx.compose.material3:material3:1.4.0-alpha17")
	implementation("androidx.compose.material3:material3-window-size-class:1.4.0-alpha17")
	implementation("androidx.core:core-splashscreen:1.2.0-rc01")
	implementation("androidx.compose.material:material-icons-extended-android:") // Use the same version as your other Compose libraries
	// Hilt
	implementation("com.google.dagger:hilt-android:2.56.2")
	implementation(libs.material3)
	kapt("com.google.dagger:hilt-android-compiler:2.56.2")
	implementation("androidx.hilt:hilt-navigation-compose:1.3.0-alpha01")

	// Accompanist
	implementation("com.google.accompanist:accompanist-pager:0.36.0")
	implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")
}

kapt {
	correctErrorTypes = true
}