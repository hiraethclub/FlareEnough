package club.hiraeth.flareenough.data.db.entity

/** The physical form of a medication. */
enum class MedicationForm {
    TABLET,
    CAPSULE,
    INJECTION,
    LIQUID,
    CREAM,
    INHALER,
    PATCH,
    OTHER,
}

/**
 * How a medication is scheduled. The parameters each type needs live on the
 * medication row (interval, cycle lengths, day of week mask) plus the child
 * schedule_times table for the times of day.
 */
enum class ScheduleType {
    /** The same times every day. */
    EVERY_DAY,

    /** On chosen days of the week. Uses the day of week mask. */
    DAYS_OF_WEEK,

    /** Every N days from an anchor date. Uses intervalDays and anchorEpochDay. */
    EVERY_N_DAYS,

    /** Once a week on a set day. Uses the day of week mask with one day set. */
    WEEKLY,

    /** X days on then Y days off, repeating. Uses cycleDaysOn, cycleDaysOff, anchor. */
    CYCLE,

    /** No schedule. Taken only when needed. Uses the as needed gap and daily max. */
    AS_NEEDED,
}

/** The outcome recorded for a dose. Recorded neutrally, no guilt language. */
enum class DoseStatus {
    TAKEN,
    SKIPPED,
    MISSED,
}

/** Where a dose was logged from. Useful for understanding the app, not shown as judgement. */
enum class LoggedVia {
    APP,
    NOTIFICATION,
    WIDGET,
    QUICK_TILE,
}

/** The input style of a symptom tracker. */
enum class TrackerType {
    /** Five large buttons, for example 1 None to 5 Severe. */
    FIVE_LEVEL,

    /** A simple yes or no. */
    YES_NO,

    /** Quick pick durations, for example morning stiffness bands. */
    DURATION,

    /** A plain number, with an optional unit. */
    NUMBER,
}

/**
 * A tappable region on the body map. Left and right are separated so a person can
 * mark one side. The plain list fallback for TalkBack uses these same regions.
 */
enum class BodyRegion {
    HAND_LEFT,
    HAND_RIGHT,
    FINGERS_LEFT,
    FINGERS_RIGHT,
    WRIST_LEFT,
    WRIST_RIGHT,
    ELBOW_LEFT,
    ELBOW_RIGHT,
    SHOULDER_LEFT,
    SHOULDER_RIGHT,
    NECK,
    JAW,
    BACK,
    HIP_LEFT,
    HIP_RIGHT,
    KNEE_LEFT,
    KNEE_RIGHT,
    ANKLE_LEFT,
    ANKLE_RIGHT,
    FOOT_LEFT,
    FOOT_RIGHT,
}

/** The state marked for a body region. Tap cycles sore, then swollen, then clear. */
enum class BodyState {
    SORE,
    SWOLLEN,
}

/**
 * How much bleeding was recorded for a day, for the optional period tracker. The app
 * only records this, in the person's own words of intensity. It never predicts a
 * cycle, estimates fertile days, or reads anything into the pattern.
 */
enum class PeriodFlow {
    SPOTTING,
    LIGHT,
    MEDIUM,
    HEAVY,
}

/** The kind of stillness session, logged to the timeline with duration only. */
enum class SessionType {
    TIMER,
    BREATHING,
    BELL,
}
