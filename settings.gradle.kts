pluginManagement {
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

rootProject.name = "Flip_2_DND"
include(":app")
include(":core")
include(":free-impl")

val proDir = file("../flip_2_dnd_pro")
if (proDir.exists()) {
    include(":pro-impl")
    project(":pro-impl").projectDir = proDir
}
 