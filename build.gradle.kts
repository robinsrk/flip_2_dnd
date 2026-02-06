// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
	id("com.android.application") version "8.11.1" apply false
	id("org.jetbrains.kotlin.android") version "2.1.0" apply false
	id("com.google.dagger.hilt.android") version "2.57.2" apply false
	id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
	alias(libs.plugins.compose.compiler) apply false
}