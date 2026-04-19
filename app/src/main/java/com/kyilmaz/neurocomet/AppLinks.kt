package com.kyilmaz.neurocomet

/**
 * Single source of truth for all outbound NeuroComet URLs and the
 * hosts we expect Android App Links to resolve for.
 *
 * Every place that constructs a share link, a profile link, or a
 * legal-page URL MUST go through this object so we can rename the
 * domain in exactly one place.
 *
 * The host list below must stay in lock-step with:
 *   - AndroidManifest.xml  (<intent-filter> <data> entries on MainActivity)
 *   - MainActivity.kt      (navDeepLink { uriPattern = ... } blocks)
 *   - docs/.well-known/assetlinks.json  (served from the same domain)
 */
object AppLinks {
    /** Canonical bare domain (no scheme, no trailing slash). */
    const val CANONICAL_DOMAIN: String = "getneurocomet.com"

    /** Canonical origin used for generating share URLs. */
    const val CANONICAL_ORIGIN: String = "https://$CANONICAL_DOMAIN"

    /** Every host that MUST be wired into the manifest intent-filter. */
    val VERIFIED_HOSTS: List<String> = listOf(
        CANONICAL_DOMAIN,
        "www.$CANONICAL_DOMAIN",
    )

    /** Path prefixes that are expected to resolve via App Links. */
    val VERIFIED_PATH_PREFIXES: List<String> = listOf("/post/", "/u/")

    /** Build a canonical share URL for a post. Null ids coerce to "0". */
    fun postUrl(postId: Any?): String = "$CANONICAL_ORIGIN/post/${postId ?: 0}"

    /** Build a canonical profile URL. Null ids coerce to "user". */
    fun profileUrl(userId: Any?): String = "$CANONICAL_ORIGIN/u/${userId ?: "user"}"

    /** Build a legal-page URL (/privacy, /terms, etc). */
    fun legalUrl(path: String): String {
        val normalized = if (path.startsWith("/")) path else "/$path"
        return "$CANONICAL_ORIGIN$normalized"
    }

    /** Full URL to the App Links proof file. */
    const val ASSETLINKS_URL: String = "$CANONICAL_ORIGIN/.well-known/assetlinks.json"

    /** Support mailbox. */
    const val SUPPORT_EMAIL: String = "support@$CANONICAL_DOMAIN"
}

