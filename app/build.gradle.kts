plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.dagger.hilt.android")
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.ksp)
}

android {
	namespace = "dev.robin.flip_2_dnd"
	compileSdk = 36

	defaultConfig {
		applicationId = "dev.robin.flip_2_dnd"
		minSdk = 23
		targetSdk = 35
		versionCode = 1204
		versionName = "12.0.4"
		vectorDrawables {
			useSupportLibrary = true
		}

		val isPro = findProject(":pro-impl") != null
		if (isPro) {
			applicationIdSuffix = ".pro"
			versionNameSuffix = "-pro"
			resValue("string", "app_name", "Flip 2 DND Pro")
		} else {
			versionNameSuffix = "-free"
			resValue("string", "app_name", "Flip 2 DND")
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
		buildConfig = true
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
	implementation("androidx.core:core-ktx:1.17.0")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
	implementation("androidx.activity:activity-compose:1.12.0-rc01")
	implementation(platform("androidx.compose:compose-bom:2025.11.00"))
	implementation("androidx.compose.ui:ui:1.9.4")
	implementation("androidx.compose.ui:ui-graphics:1.9.4")
	implementation("androidx.compose.material3:material3-window-size-class:1.4.0")
	implementation("androidx.core:core-splashscreen:1.2.0")
	implementation(libs.androidx.compose.material.icons.extended.android) 
	// Hilt
	implementation("com.google.dagger:hilt-android:2.57.2")
	implementation(libs.material3)
	ksp("com.google.dagger:hilt-android-compiler:2.57.2")
	implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

	// Accompanist
	implementation("com.google.accompanist:accompanist-pager:0.36.0")
	implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")

	// Room
	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler)

	implementation(project(":core"))
	runtimeOnly(project(":free-impl"))
	if (findProject(":pro-impl") != null) {
		runtimeOnly(project(":pro-impl"))
	}
}
