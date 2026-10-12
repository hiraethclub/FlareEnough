package club.hiraeth.flareenough.data.db

/**
 * The body parts the app ships with, grouped into categories, head to toe. This is the
 * single source of truth for what a fresh install starts with and what an upgrade
 * seeds. It holds only stable keys and order, never display names: the names live in
 * string resources so the defaults stay translatable (see the label lookups in the UI).
 *
 * The part keys are the original body region names, so marks recorded before body parts
 * became editable still line up with their part after upgrading.
 */
object BodyPartDefaults {

    data class DefaultCategory(val key: String, val partKeys: List<String>)

    val categories: List<DefaultCategory> = listOf(
        DefaultCategory("head", listOf("JAW", "NECK")),
        DefaultCategory(
            "arms",
            listOf(
                "SHOULDER_LEFT", "SHOULDER_RIGHT",
                "ELBOW_LEFT", "ELBOW_RIGHT",
                "WRIST_LEFT", "WRIST_RIGHT",
            ),
        ),
        DefaultCategory(
            "hands",
            listOf("HAND_LEFT", "HAND_RIGHT", "FINGERS_LEFT", "FINGERS_RIGHT"),
        ),
        DefaultCategory("back", listOf("BACK", "HIP_LEFT", "HIP_RIGHT")),
        DefaultCategory(
            "legs",
            listOf(
                "KNEE_LEFT", "KNEE_RIGHT",
                "ANKLE_LEFT", "ANKLE_RIGHT",
                "FOOT_LEFT", "FOOT_RIGHT",
            ),
        ),
    )

    /** True if this key is one the app ships with, so its name comes from resources. */
    fun isDefaultCategory(key: String): Boolean = categories.any { it.key == key }

    fun isDefaultPart(key: String): Boolean = categories.any { key in it.partKeys }
}
