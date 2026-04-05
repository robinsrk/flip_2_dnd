package dev.robin.flip_2_dnd.presentation.changelog

data class ChangelogEntry(
  val version: String,
  val emoji: String,
  val changes: List<String>,
)

// Static changelog data presented as a fancy accordion in UI
val changelogEntries =
  listOf(
    ChangelogEntry(
      version = "v12.0.9",
      emoji = "🎨",
      changes =
        listOf(
          "✨ Updated UI with Material You colors",
          "🔄 Improved mode selection animations",
          "👤 Added author and contributors credits",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.8",
      emoji = "⚡",
      changes =
        listOf(
          "📱 Added ability to turn off screen",
          "🌐 Updated translations",
          "🛒 Removed discount promotion",
          "👥 Added contributors section",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.7",
      emoji = "📊",
      changes =
        listOf(
          "📝 Redesigned history page to show DND sessions",
          "⏱️ Display activation duration for each session",
          "🕐 Show start and end times with dates",
          "🔔 Indicate ongoing sessions with live duration",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.5",
      emoji = "💄",
      changes =
        listOf(
          "🎨 Updated changelog ui",
          "📝 Updated command button shapes",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.4",
      emoji = "✨",
      changes =
        listOf(
          "🚀 Pro feature high sensitivity made free",
          "🔧 Fix flashlight patterns",
          "🪄 Flip detection optimization",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.3",
      emoji = "🧭",
      changes =
        listOf(
          "🐛 Fix update checker for pro variant",
          "📝 Upgrade strings",
          "🎚️ Fix audio playing attributes (by robert)",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.2",
      emoji = "⚡",
      changes =
        listOf(
          "⚙️ Fix high sensitivity mode",
          "🌐 Fix translations for spanish (by robert)",
          "🏅 Fix pro badge in settings for premium users",
        ),
    ),
    ChangelogEntry(
      version = "v12.0.1",
      emoji = "🔆",
      changes =
        listOf(
          "💳 Added credits section for patreon members",
          "✨ UI Optimizations",
          "🔋 Battery optimizations",
        ),
    ),
    ChangelogEntry(
      version = "v11.1.2",
      emoji = "🧰",
      changes =
        listOf(
          "🧭 Fix home screen cards width",
          "🎨 Optimized UI",
          "🌍 Added missing translations",
        ),
    ),
    ChangelogEntry(
      version = "v11.1.1",
      emoji = "🎞",
      changes =
        listOf(
          "🪄 Improve home screen animations",
          "📐 Fix home screen icon layout",
          "🔋 Fix battery saver command dialog popup",
        ),
    ),
    ChangelogEntry(
      version = "v11.1.0",
      emoji = "🧭",
      changes =
        listOf(
          "🎬 Add sliding animations in home page",
          "🧭 UI optimizations",
        ),
    ),
    ChangelogEntry(
      version = "v11.0.3",
      emoji = "🔦",
      changes =
        listOf(
          "🔆 Add flashlight intensity controller in settings",
          "💡 Add flashlight feedback for slider",
          "🔧 Fix flashlight not blinking respecting the settings",
        ),
    ),
    ChangelogEntry(
      version = "v11.0.2",
      emoji = "🔦",
      changes =
        listOf(
          "⚡ Changed flashlight intensity to lowest for flashlight feedback",
        ),
    ),
    ChangelogEntry(
      version = "v11.0.1",
      emoji = "🛡",
      changes =
        listOf(
          "🧭 Optimized background battery usage and sensor polling",
          "🎯 Smart sensor management to save battery when screen is ON",
          "🎉 Ramadan celebration popup for free users",
          "🐞 Fixed various minor bugs and stability issues",
        ),
    ),
    ChangelogEntry(
      version = "v11.0.0",
      emoji = "🚀",
      changes =
        listOf(
          "🏠 Redesigned Home Screen with a modern, single-page layout",
          "⚙️ Integrated Settings into a sleek Bottom Sheet",
          "🛒 Added a \"Pro\" upgrade button to the top bar for easy access to premium features",
          "🧭 Simplified navigation system for a faster and more intuitive experience",
          "🎨 Refined UI with improved spacing, icons, and contrast",
          "🧭 Resolved various layout and scrolling issues",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.8",
      emoji = "🎯",
      changes =
        listOf(
          "💡 Added \"Feedback even if Flashlight is ON\" setting in Flashlight Feedback",
          "🛠 Fixed flashlight state not restoring correctly after feedback",
          "🔎 Improved flashlight detection logic",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.7",
      emoji = "🧰",
      changes =
        listOf(
          "🧭 Refactored settings UI headers with explicit toggles",
          "🎗 Added \"Support Developer\" popup with Patreon and Crypto options",
          "🧭 Reconfigure gradlew",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.6",
      emoji = "🎞",
      changes =
        listOf(
          "⏱ Added schedule support for High Sensitivity Mode (Start Time, End Time, and Days)",
          "🎞 Added smooth animations when toggling settings sections",
          "🎯 Improved UI layout consistency and resolved tile overlapping issues",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.5",
      emoji = "⚡",
      changes =
        listOf(
          "🔎 Improved flip detection precision using a low-pass filter and higher sampling rate",
          "🔒 Mandatory permissions (DND, Battery Optimization) are now checked on every app startup",
          "⚡ Notification permission is no longer mandatory to use the app",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.4",
      emoji = "🧰",
      changes =
        listOf(
          "🧱 Restored fully reproducible builds for Free flavor",
          "🔧 Removed all environment-dependent build constants",
          "🚀 Optimized build process for F-Droid and IzzyOnDroid",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.3",
      emoji = "🗓",
      changes =
        listOf(
          "🗓 Added schedule support for flashlight feedback (Start Time, End Time, and Days)",
          "🔁 Improved feedback logic consistency across sound, vibration, and flashlight",
          "🎨 UI improvements in the flashlight settings section",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.2",
      emoji = "📝",
      changes =
        listOf(
          "📝 Added DND history to track activations and deactivations",
          "🧭 Added proximity sensor detection for more reliable activation",
          "⚡ Migrated to KSP for better performance and Kotlin compatibility",
          "🗨 Added \"What's New\" changelog dialog",
          "🎨 UI improvements and theme consistency updates",
          "🐞 Fixed various minor bugs",
        ),
    ),
    ChangelogEntry(
      version = "v10.1.1",
      emoji = "💬",
      changes =
        listOf(
          "💬 Added about app section",
          "🔒 Fixed battery permission command dialog",
          "🧭 Improved donation screen layout and navigation UI",
          "🗣 Added dynamic changelog support",
          "🪶 Refined top bar animations and font weight handling",
          "🔧 Enhanced custom sound URI validation",
          "🎵 Improved MediaPlayer error handling",
          "🎶 Added new vibration patterns (Heartbeat, Tick Tock)",
        ),
    ),
  )
