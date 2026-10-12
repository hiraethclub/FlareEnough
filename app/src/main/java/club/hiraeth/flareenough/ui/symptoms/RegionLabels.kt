package club.hiraeth.flareenough.ui.symptoms

import android.content.Context
import androidx.annotation.StringRes
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyRegion
import club.hiraeth.flareenough.data.db.entity.BodyState

/**
 * Display names for body parts, categories, and states.
 *
 * Body parts and categories are editable and stored in the database by a stable key.
 * Built in ones keep a null name and are shown through these translated string
 * resources, so the defaults stay translatable. A part or category the person names or
 * renames carries its own text instead. [bodyPartDisplayName] and [categoryDisplayName]
 * put those two rules together.
 */

@StringRes
fun BodyRegion.labelRes(): Int = when (this) {
    BodyRegion.HAND_LEFT -> R.string.region_hand_left
    BodyRegion.HAND_RIGHT -> R.string.region_hand_right
    BodyRegion.FINGERS_LEFT -> R.string.region_fingers_left
    BodyRegion.FINGERS_RIGHT -> R.string.region_fingers_right
    BodyRegion.WRIST_LEFT -> R.string.region_wrist_left
    BodyRegion.WRIST_RIGHT -> R.string.region_wrist_right
    BodyRegion.ELBOW_LEFT -> R.string.region_elbow_left
    BodyRegion.ELBOW_RIGHT -> R.string.region_elbow_right
    BodyRegion.SHOULDER_LEFT -> R.string.region_shoulder_left
    BodyRegion.SHOULDER_RIGHT -> R.string.region_shoulder_right
    BodyRegion.NECK -> R.string.region_neck
    BodyRegion.JAW -> R.string.region_jaw
    BodyRegion.BACK -> R.string.region_back
    BodyRegion.HIP_LEFT -> R.string.region_hip_left
    BodyRegion.HIP_RIGHT -> R.string.region_hip_right
    BodyRegion.KNEE_LEFT -> R.string.region_knee_left
    BodyRegion.KNEE_RIGHT -> R.string.region_knee_right
    BodyRegion.ANKLE_LEFT -> R.string.region_ankle_left
    BodyRegion.ANKLE_RIGHT -> R.string.region_ankle_right
    BodyRegion.FOOT_LEFT -> R.string.region_foot_left
    BodyRegion.FOOT_RIGHT -> R.string.region_foot_right
}

/** The word for a marked body state. One place, so every screen and the exports agree. */
@StringRes
fun BodyState.labelRes(): Int = when (this) {
    BodyState.SORE -> R.string.body_state_sore
    BodyState.PAINFUL -> R.string.body_state_painful
    BodyState.SWOLLEN -> R.string.body_state_swollen
}

/** The string resource for a default category name, or null if the key is not a default. */
fun defaultCategoryNameRes(key: String): Int? = when (key) {
    "head" -> R.string.body_area_head
    "arms" -> R.string.body_area_arms
    "hands" -> R.string.body_area_hands
    "back" -> R.string.body_area_back
    "legs" -> R.string.body_area_legs
    else -> null
}

/**
 * The string resource for a default body part name, or null if the key is not a default.
 *
 * This, [defaultCategoryNameRes], and the [BodyRegion] enum are a permanent compatibility
 * layer, not dead code. Built in parts store their name as null and are shown through
 * these resources so the defaults stay translatable, and every mark recorded before body
 * parts became editable is keyed by the old region name (for example HAND_LEFT). Removing
 * the enum or these mappings would make those built in parts and old marks fall back to
 * showing a raw key. Keep them even if nothing else references the enum.
 */
fun defaultBodyPartNameRes(key: String): Int? =
    runCatching { BodyRegion.valueOf(key).labelRes() }.getOrNull()

/** The person's own category name, else the translated default, else the raw key as a fallback. */
fun categoryDisplayName(context: Context, key: String, customName: String?): String =
    customName ?: defaultCategoryNameRes(key)?.let { context.getString(it) } ?: key

/** The person's own part name, else the translated default, else the raw key as a fallback. */
fun bodyPartDisplayName(context: Context, key: String, customName: String?): String =
    customName ?: defaultBodyPartNameRes(key)?.let { context.getString(it) } ?: key
