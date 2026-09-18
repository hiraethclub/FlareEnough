# Permissions

Flare Enough asks for as few permissions as possible, and only ones needed to
remind you reliably. None of them allow internet access. Each is explained inside
the app before your device's system prompt appears.

There is deliberately no `INTERNET` permission, and there never will be. The app
works fully offline.

## Permissions the app declares

| Permission | Why it is needed | When it is asked |
| --- | --- | --- |
| `POST_NOTIFICATIONS` | To show dose reminders, refill warnings, meditation bells, and the daily check in. | At runtime on Android 13 and later, after a short explanation. |
| `SCHEDULE_EXACT_ALARM` | To fire dose reminders at the exact time, including when the phone is idle in Doze. | The app checks whether it is granted and, if not, guides you to the "Alarms and reminders" setting. |
| `RECEIVE_BOOT_COMPLETED` | To rebuild your reminders after the phone restarts, so they are not lost. | Granted at install. No prompt. |

## Why not `USE_EXACT_ALARM`

There are two Android permissions for exact alarms:

- `USE_EXACT_ALARM` is granted automatically at install, but Google Play policy
  restricts it to apps whose main purpose is an alarm clock, a timer, or a
  calendar. A medication tracker does not qualify.
- `SCHEDULE_EXACT_ALARM` does the same job, but you grant it yourself. This is the
  correct choice for this app, so it is the one we use.

If exact alarms are not granted, the app falls back to the best inexact alarm it
can, and shows a clear banner warning that reminders may be late.

## Battery optimisation

Some phones aggressively stop background apps to save battery, which can make
reminders late even when everything above is granted. This is a device setting,
not an app permission. The Reminder health check screen shows the current status
and links to dontkillmyapp.com for advice specific to your phone.

## Optional app lock

If you turn on the app lock, the app uses your device's own biometric or PIN
prompt. This is handled entirely by the device. The app never sees your biometric
data or PIN. This does not require a dangerous permission.

## What the app does not request

- No `INTERNET`. The app cannot reach the network.
- No location, contacts, camera, microphone, or storage-wide access.
- Backups, CSV exports, and PDF reports use the system file picker (Storage Access
  Framework), so no broad storage permission is needed.
