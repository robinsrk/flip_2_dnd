plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.dagger.hilt.android")
	id("kotlin-kapt")
}

android {
	namespace = "dev.robin.flip_2_dnd"
	compileSdk = 34

	defaultConfig {
		applicationId = "dev.robin.flip_2_dnd"
		minSdk = 23
	 	targetSdk = 34
		versionCode = 33
		versionName = "3.0.3"
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
		kotlinCompilerExtensionVersion = "1.5.1"
	}

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
	implementation("androidx.activity:activity-compose:1.8.2")
	implementation(platform("androidx.compose:compose-bom:2024.01.00"))
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.core:core-splashscreen:1.0.1")

	// Hilt
	implementation("com.google.dagger:hilt-android:2.48")
	kapt("com.google.dagger:hilt-android-compiler:2.48")
	implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

	// Accompanist
	implementation("com.google.accompanist:accompanist-pager:0.32.0")
	implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
}

kapt {
	correctErrorTypes = true
}