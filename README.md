# Flare Enough

A free, open source Android app for tracking medication, logging symptoms, and
supporting simple meditation. Built for people with chronic illness.

The name is a pun on flares and "fair enough". Dry, and a little defiant.

## What it promises

- No ads, no subscriptions, no in-app purchases.
- No analytics, no trackers, no accounts.
- No internet permission at all. It works fully offline. Your data stays on your
  device.
- Reminders that actually fire on time, including when the phone is idle and
  after a reboot.
- A calm, gentle interface designed for bad days: sore hands, fatigue, brain fog,
  and anxiety.

## Not medical advice

Flare Enough records and reminds only. It does not diagnose, recommend
treatments, adjust doses, check drug interactions, or interpret symptoms. For
medical advice, diagnosis, or treatment, speak to a healthcare professional.

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

## Project layout

- `:app` The Android application. UI, data, reminders.
- `:schedule` Pure Kotlin scheduling logic, including the daylight saving safe
  parts, kept separate so it can be tested on a plain JVM.

## Contributing

Please read [CLAUDE.md](CLAUDE.md) first. It lists the firm rules of the project:
privacy, no monetisation, reminder reliability, accessibility, and tone.
