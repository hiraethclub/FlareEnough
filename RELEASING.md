# Releasing Flare Enough

Releases are built and published automatically. When you push a version tag such as
`v0.1.0`, the `Release` workflow builds a signed APK and attaches it to a new GitHub
Release that people can download.

The signing key is never stored in the repository. It lives only on your machine and
in GitHub secrets. Keep it safe: if you lose it, you cannot ship an update that
installs over an existing copy, and everyone would have to uninstall and reinstall.

## One time setup

You only do this once. If you are on Windows, follow the Windows walkthrough just
below. On Linux or macOS, use the numbered steps after it.

### Windows walkthrough

Use Windows PowerShell (Start button, type PowerShell, open Windows PowerShell).

1. Point PowerShell at keytool, which comes bundled with Android Studio. Adjust the
   path to where Android Studio is installed:

   ```
   $keytool = "D:\Android Studio\jbr\bin\keytool.exe"
   & $keytool -help
   ```

   If that path does not exist, try `jre` instead of `jbr`. If you do not have
   Android Studio, install a JDK from https://adoptium.net and then just use
   `keytool` on its own.

2. Make a folder and create the key:

   ```
   mkdir $HOME\flare-enough-keys
   cd $HOME\flare-enough-keys
   & $keytool -genkeypair -v -keystore flare-enough-release.jks -alias flare-enough -keyalg RSA -keysize 4096 -validity 10000
   ```

   It asks for a keystore password (type it twice, nothing shows as you type), then
   some name and address fields you can press Enter through, then `yes` to confirm,
   then a key password (press Enter to reuse the keystore password). Write the
   password down safely.

3. Back up `flare-enough-release.jks` and its password somewhere private and durable.
   This file cannot be recreated.

4. Copy the key onto the clipboard as text, ready to paste into the secret:

   ```
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\flare-enough-release.jks")) | Set-Clipboard
   ```

5. Add the four repository secrets on GitHub, as described in step 3 below. For
   `KEYSTORE_BASE64`, paste with Ctrl+V (the clipboard from step 4 above).

### 1. Create your signing key

Run this on your own machine, in a folder outside the project, and answer the
prompts. Choose a strong password and write it down somewhere safe.

```
keytool -genkeypair -v \
  -keystore flare-enough-release.jks \
  -alias flare-enough \
  -keyalg RSA -keysize 4096 -validity 10000
```

Back up `flare-enough-release.jks` and its password somewhere private and durable, for
example a password manager. This file is the one thing that cannot be recreated.

### 2. Turn the key into a secret value

GitHub secrets hold text, so encode the key as base64.

On Linux:

```
base64 -w0 flare-enough-release.jks > keystore.base64
```

On macOS:

```
base64 -i flare-enough-release.jks | tr -d '\n' > keystore.base64
```

### 3. Add the four repository secrets

In GitHub, go to the repository, then Settings, then Secrets and variables, then
Actions, then New repository secret. Add these four:

- `KEYSTORE_BASE64`: the whole contents of `keystore.base64`.
- `KEYSTORE_PASSWORD`: the keystore password you chose.
- `KEY_ALIAS`: `flare-enough` (or whatever alias you used above).
- `KEY_PASSWORD`: the key password. If keytool used the same password for both, this
  is the same value as `KEYSTORE_PASSWORD`.

After this you can delete the local `keystore.base64` file. Keep the `.jks` file and
its password.

## Cutting a release

1. Update the version in `app/build.gradle.kts`:
   - `versionCode`: increase by one every release (2, 3, 4, and so on). Android uses
     this number to tell newer from older.
   - `versionName`: the human version, for example `0.2.0`.
   Also update `about_version` in `app/src/main/res/values/strings.xml` so the About
   screen matches.
2. Commit and push those changes to `main`.
3. Tag the release and push the tag. The tag should match the version name:

   ```
   git tag v0.2.0
   git push origin v0.2.0
   ```

4. Watch the `Release` workflow in the Actions tab. When it finishes, the signed APK
   is on the Releases page, named `flare-enough-v0.2.0.apk`.

That APK is what people download and sideload, and what you can point testers to.

## Building a release APK on your own machine (optional)

You do not need this for normal releases, but it is handy for checking a signed build
locally. Create a file named `keystore.properties` in the project root (it is already
in `.gitignore`, so it will not be committed):

```
storeFile=/absolute/path/to/flare-enough-release.jks
storePassword=your-keystore-password
keyAlias=flare-enough
keyPassword=your-key-password
```

Then run:

```
./gradlew :app:assembleRelease
```

The APK appears in `app/build/outputs/apk/release/`.

## Notes

- If no signing key is present, a release build falls back to the debug key so local
  builds still work. Real releases always run through the workflow with the real key.
- F-Droid, if you publish there later, builds from source and signs with its own key,
  so an F-Droid install and a GitHub Release install have different signatures and
  cannot update over each other. That is expected, not a bug.
- The Play Store, if you go there later, uses its own Play App Signing on top of this
  key. The same versioning rules apply.
