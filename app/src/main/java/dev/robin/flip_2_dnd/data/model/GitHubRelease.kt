package dev.robin.flip_2_dnd.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubRelease(
    @Json(name = "tag_name")
    val tagName: String,
    @Json(name = "draft")
    val isDraft: Boolean,
    @Json(name = "prerelease")
    val isPrerelease: Boolean,
    @Json(name = "html_url")
    val htmlUrl: String,
    var message: String = "" // Custom message for UI display
)
