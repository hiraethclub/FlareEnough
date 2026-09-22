# Releasing Flare Enough

Releases are built and published automatically. When you push a version tag such as
`v0.1.0`, the `Release` workflow builds a signed APK and attaches it to a new GitHub
Release that people can download.

The signing key is never stored in the repository. It lives only on your machine and
in GitHub secrets. Keep it safe: if you lose it, you cannot ship an update that
installs over an existing copy, and everyone would have to uninstall and reinstall.

## One time setup

You only do this once.

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
