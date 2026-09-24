# CLAUDE.md

Guidance for any AI assistant or contributor working in this repository. These
rules come from the project brief and are not optional. When a change would
break one of them, stop and raise it rather than working around it.

## What this app is

Flare Enough is a free, open source Android app for tracking medication,
logging symptoms, and supporting simple meditation. It is built for people with
chronic illness, including people with painful or stiff hands, fatigue, anxiety,
poor memory, and difficulty reading long text.
Every design choice should make life easier on a bad day.

The app name is a pun on flares and "fair enough". Keep the name in a single
string resource (`R.string.app_name`) and a single Kotlin constant
(`AppInfo.NAME_RES`) so it can be changed in one place.

## Firm rules

### Privacy and no network
- No `INTERNET` permission in any manifest, ever. The app works fully offline.
  This is a core promise, not a preference.
- No analytics, no trackers, no crash reporting that phones home, no accounts.
- No Google Play Services dependency, so the same code ships on F-Droid.
- All data stays on the device. The only ways data leaves are user driven:
  backup export, CSV export, and the PDF report, all through the system share
  sheet or file picker, with the user in control.

### No monetisation
- No ads, subscriptions, in-app purchases, or paid tiers. Never.

### Reminder reliability
- Reminders must fire on time, including in Doze, after reboot, after app
  updates, and across UK clock changes (BST and GMT).
- Medication reminders use `SCHEDULE_EXACT_ALARM` (user granted at runtime),
  not `USE_EXACT_ALARM`, because Play policy restricts the latter to alarm,
  timer, and calendar apps. Check `canScheduleExactAlarms()` and guide the
  user to grant "Alarms & reminders" if needed.
- If exact alarms are not granted, fall back to the best inexact method and
  show a clear, persistent banner that reminders may be late.
- Reschedule all alarms on: boot completed, app update, time change, time zone
  change, and exact alarm permission state change.
- Store daily recurring times as local wall clock (minutes past midnight), not
  as fixed instants, so clock changes behave correctly. Times that record when
  something actually happened are stored as epoch millis.
- All scheduling logic lives in the pure Kotlin `:schedule` module with no
  Android dependencies, so it can be unit tested on a plain JVM. Every schedule
  type and the DST edge cases (spring forward, autumn back, doses at 01:30)
  must have tests.

### Accessibility and bad-day design
- Minimum touch target 56dp. Primary actions larger.
- Support system font scaling to 200% without breaking layouts.
- Full TalkBack support with meaningful content descriptions. The body map must
  also work as a plain list for TalkBack users.
- Never rely on colour alone. Always pair with a word, number, or icon.
- Short labels, plain words, icons alongside text. No long paragraphs in the UI.
  Explanations are one or two short sentences with an optional "More" expander.
- A "Big and simple" mode: larger buttons, fewer elements, hides charts and
  optional fields.

### Tone of language
- Kind and neutral. Never "You failed", "You missed again", or streak loss
  messages. Missed doses are recorded neutrally with no red alarm styling and
  no guilt language.
- No streaks, badges, targets, tips, or articles anywhere.

### Look and feel
- Calm, muted pastel palette: sage, dusty lavender, powder blue, pale peach,
  warm cream. No bright red at all. Use muted terracotta or amber for anything
  needing attention.
- Text meets WCAG AA contrast on pastel surfaces. Use deep slate or charcoal
  text, never grey on pastel.
- Light and dark themes, both muted. Material You dynamic colour is an option,
  off by default.
- Respect the system "remove animations" setting.

### Medical and store compliance
- The app records and reminds only. It must not diagnose, recommend treatments,
  adjust doses, check drug interactions, or interpret symptoms. Charts show data
  only, with no interpretation, prediction, or correlation presented as fact.
- Keep the not-medical-advice notice in onboarding and in About.

### Writing style (docs, UI text, commit messages, notes to the user)
- No em dashes and no double hyphens anywhere. Use commas, colons, or "to".
- All user-facing strings live in `res/values/strings.xml`, ready for
  translation. Welsh (cy) is a planned translation.

## Technical shape

- Kotlin, Jetpack Compose, Material 3.
- Single Activity, Compose Navigation, MVVM with ViewModels and Kotlin Flows.
- Room for data, DataStore for settings.
- Manual dependency injection: one `AppContainer` owned by `FlareApp`, wired by
  hand so it can be read top to bottom. No Hilt.
- compileSdk 36, targetSdk 36, minSdk 26.
- Licence: GPL-3.0-or-later. The app and any forks stay free.

## Module layout

- `:schedule` Pure Kotlin. Daylight saving safe reminder scheduling logic and
  its tests. No Android imports.
- `:app` The Android application. UI, data, reminders, everything else.

## How to work

- Verify version specific Android behaviour, permission rules, and Play policy
  against current official Android developer documentation before relying on
  them. Do not trust memory for these. If uncertain, say so.
- Work one milestone at a time. At the end of each: build, run tests, summarise
  changes in short plain sentences, and say exactly what to test on a phone.
- Keep explanations short and concrete.
