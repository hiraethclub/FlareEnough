package club.hiraeth.flareenough.ui.symptoms

import androidx.annotation.StringRes
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.data.db.entity.BodyRegion

/** The order regions appear in the body map list, roughly head to toe. */
val bodyRegionOrder: List<BodyRegion> = listOf(
    BodyRegion.JAW,
    BodyRegion.NECK,
    BodyRegion.SHOULDER_LEFT,
    BodyRegion.SHOULDER_RIGHT,
    BodyRegion.ELBOW_LEFT,
    BodyRegion.ELBOW_RIGHT,
    BodyRegion.WRIST_LEFT,
    BodyRegion.WRIST_RIGHT,
    BodyRegion.HAND_LEFT,
    BodyRegion.HAND_RIGHT,
    BodyRegion.FINGERS_LEFT,
    BodyRegion.FINGERS_RIGHT,
    BodyRegion.BACK,
    BodyRegion.HIP_LEFT,
    BodyRegion.HIP_RIGHT,
    BodyRegion.KNEE_LEFT,
    BodyRegion.KNEE_RIGHT,
    BodyRegion.ANKLE_LEFT,
    BodyRegion.ANKLE_RIGHT,
    BodyRegion.FOOT_LEFT,
    BodyRegion.FOOT_RIGHT,
)

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
