# Flare Enough

A free, open source Android app for tracking medication, logging symptoms, and
supporting simple meditation. Built for people with chronic illness.

The name is a pun on flares and "fair enough". Dry, and a little defiant.

## What it promises

- No ads, no subscriptions, no in-app purchases.
- No analytics, no trackers, no accounts.
- No internet permission at all. It works fully offline. Your data stays on your
  device, and only leaves if you choose to export or back it up yourself.
- Reminders that actually fire on time, including when the phone is idle and
  after a reboot, and across the UK clock changes.
- A calm, gentle interface designed for bad days: sore hands, fatigue, brain fog,
  and anxiety.

## Features

Medication and reminders

- Add your medications and the times you take them, with flexible schedules
  (every day, chosen days of the week, every few days, weekly, cycles, or as
  needed).
- Reminders use exact alarms so they arrive on time, and reschedule themselves
  after a reboot, an app update, a time zone change, and the UK clock changes.
- A reminder health screen shows, with plain ticks, whether notifications, exact
  alarms, and battery settings will let reminders reach you, and offers a one
  minute test.
- Missed doses are recorded gently, with no red alarm styling and no guilt.

Symptoms and how you feel

- Simple trackers you can add and rename: five level scales, yes or no, quick
  duration bands, or a plain number.
- A flare flag for the day, and a "same as yesterday" shortcut for low energy
  days.
- A body map to note where it hurts, marking areas as sore, painful, or swollen,
  grouped by part of the body and readable by screen readers.
- An optional period tracker you can turn on in Settings, recording bleeding days
  and flow, and nothing more. It never predicts a cycle.
- Free text notes and tags for anything that does not fit a tracker.

History

- A calm month calendar with soft shading for how heavy a day was.
- Tap any day to see a plain timeline of that day: doses, symptoms, flares, body
  marks, notes, meditation, and period, all shown as data with no interpretation.
- Go back and edit any past day, in case you forgot to log something or logged it
  wrong: change the doses you took, the symptoms, joints, notes, and the rest.

Stillness

- A quiet corner with a simple timer, gentle guided breathing, and a soft bell.
- Space for your own prompts and readings, which you add yourself.

Your data stays yours

- Back up everything to a single file, and restore it later, all on your device
  through the system file picker. No cloud, no account.
- Export your log as a CSV spreadsheet, or a plain PDF report to keep or show a
  clinician. Data only, with no interpretation.
- An optional app lock that uses your phone's own unlock (fingerprint, face, PIN,
  or pattern), so the app opens only for you.

Calm by design

- A muted pastel palette with light and dark themes, and no bright red anywhere.
- Large touch targets, full TalkBack support, and it holds up with system text
  scaled all the way up.

## Install

Flare Enough is an Android app and runs on Android 8.0 and newer.

Download the latest signed APK from the
[Releases page](https://github.com/hiraethclub/FlareEnough/releases/latest) and
open it on your phone. The first time, Android will ask you to allow installing
from this source, which is normal for anything outside the Play Store.

A listing on F-Droid is in progress.

## Not medical advice

Flare Enough records and reminds only. It does not diagnose, recommend
treatments, adjust doses, check drug interactions, or interpret symptoms. For
medical advice, diagnosis, or treatment, speak to a healthcare professional.

## Support and contact

Flare Enough is free and always will be. If it helps you and you would like to
support it, there is a Ko-fi at
[ko-fi.com/hiraethclub](https://ko-fi.com/hiraethclub). You can also say hello on
Threads at [@hiraeth.clwb](https://www.threads.net/@hiraeth.clwb) or by email at
aisling@hiraeth.club.

## Licence

Flare Enough is free software: you can redistribute it and modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version. See [LICENSE](LICENSE). The app and any forks stay free.

SPDX identifier: GPL-3.0-or-later.

## Building

You need Android Studio (or the Android command line tools) and JDK 21.

1. Clone the repository.
2. Open it in Android Studio, or from a terminal run `./gradlew assembleDebug`.
3. The debug APK lands in `app/build/outputs/apk/debug/`.

To run the scheduling logic tests, which need no Android SDK:

```
./gradlew :schedule:test
```

Cutting a release is documented in [RELEASING.md](RELEASING.md).

## Project layout

- `:app` The Android application. UI, data, reminders.
- `:schedule` Pure Kotlin scheduling logic, including the daylight saving safe
  parts, kept separate so it can be tested on a plain JVM.

## Contributing

Please read [CLAUDE.md](CLAUDE.md) first. It lists the firm rules of the project:
privacy, no monetisation, reminder reliability, accessibility, and tone.
