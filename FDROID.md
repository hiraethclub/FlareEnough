# Getting Flare Enough onto F-Droid

Flare Enough is a good fit for F-Droid: it is free and open source under the GPL-3.0,
has no proprietary libraries, no Google Play Services, no trackers, and works fully
offline. There is nothing here that F-Droid would reject.

There are two homes to aim for, and it is worth doing them in this order.

## What is already set up in this repository

- **Fastlane metadata** under `fastlane/metadata/android/en-US/`: the app title, a
  short and a full description, and a changelog for version code 1. Both F-Droid and
  IzzyOnDroid read this straight from the repository, so you keep the store text here
  and never have to email anyone to change it. To update the listing later, edit
  those files and add a new `changelogs/<versionCode>.txt` for each release.
- **A ready made F-Droid build recipe** at `fdroid/club.hiraeth.flareenough.yml`,
  for the official F-Droid submission below.
- Optionally, add a listing icon and screenshots later: a 512 by 512 `icon.png` and a
  `phoneScreenshots/` folder inside the fastlane `en-US` directory. Without them, both
  stores fall back to the app's own launcher icon, so this is not required to start.

## Option 1: IzzyOnDroid (quicker, uses your GitHub releases)

IzzyOnDroid is a well known third party F-Droid repository. It takes your own signed
APK straight from your GitHub releases, so there is no build for them to run and it is
the fastest way to be installable. Once you are listed, every future tagged release is
picked up automatically within about a day.

Steps:

1. Read their inclusion policy at
   https://izzyondroid.org/docs/general/AppInclusionPolicy/ and check the app is not
   already listed.
2. Make sure a signed APK is attached to a tagged GitHub release. You have this
   already: `flare-enough-v0.1.0.apk` on the v0.1.0 release.
3. File an inclusion request as an issue at
   https://codeberg.org/IzzyOnDroid/repodata/issues (you need a free Codeberg
   account). Give the app name, the source repository
   `https://github.com/hiraethclub/FlareEnough`, and mention that fastlane metadata is
   in the repository and a signed APK is attached to the release.

That is the whole process. Their checker then follows your future releases on its own.

## Option 2: The official F-Droid repository (built from source)

The main F-Droid repository builds the app from source on their own servers and signs
it with the F-Droid key. It is more work and slower to get in, but it is the canonical
home and reaches the widest F-Droid audience.

Steps:

1. Create a free account at https://gitlab.com and fork
   https://gitlab.com/fdroid/fdroiddata. Make sure your fork is public and your
   working branch is not protected.
2. Copy `fdroid/club.hiraeth.flareenough.yml` from this repository into your fork at
   `metadata/club.hiraeth.flareenough.yml`.
3. Confirm the `License` line in that file (see the note about only versus or-later at
   the top of it).
4. Let their pipeline run on your fork and make sure it passes.
5. Open a merge request against fdroiddata titled `New app: Flare Enough`.

An F-Droid contributor then reviews it. Once merged, F-Droid builds each tagged
release automatically, because the recipe uses `UpdateCheckMode: Tags`.

## Notes worth knowing

- **Different signatures.** The F-Droid build is signed with the F-Droid key, and the
  IzzyOnDroid listing uses your own key from the GitHub release. Neither matches the
  other, so a phone cannot update across them without uninstalling first. This is
  normal for the same app in different stores, not a fault. Pick whichever you point
  people at as the main one.
- **The build recipe pins a commit.** The first build is pinned to the exact commit
  the v0.1.0 tag points to. Future versions are found by their tags, so you do not
  edit the recipe for each release, you just tag as usual.
- **If the F-Droid build objects to signing.** The app's release build falls back to
  the debug key when no release key is present, purely for local convenience. If an
  F-Droid build ever trips on that, the fix is a one line change to leave the release
  unsigned when no key is set, and it can be made then.
