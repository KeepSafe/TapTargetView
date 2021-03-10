How To Release
==============

Due to Maven Central's very particular requirements, the release process is a bit
elaborate and requires a good deal of local configuration. This guide should walk
you through it. It won't do anyone outside of KeepSafe any good, but the workflow
is representative of just about any project deploying via Sonatype.

We currently deploy to Maven Central (via Sonatype's OSS Nexus instance).

### Prerequisites

1. A *published* GPG code-signing key
1. A Sonatype Nexus OSS account with permission to publish in com.getkeepsafe
1. Permission to push directly to https://github.com/KeepSafe/TapTargetView

### Setup

1. Add your GPG key to your github profile - this is required
   for github to know that your commits and tags are "verified".
1. Configure your code-signing key in ~/.gradle.properties:
   ```gradle
   signing.keyId=<key ID of your GPG signing key>
   signing.password=<your key's passphrase>
   signing.secretKeyRingFile=/path/to/your/secring.gpg
   ```
1. Configure your Sonatype credentials in ~/.gradle.properties:
   ```gradle
   SONATYPE_NEXUS_USERNAME=<nexus username>
   SONATYPE_NEXUS_PASSWORD=<nexus password>
   ```
1. Configure git with your codesigning key; make sure it's the same as the one
   you use to sign binaries (i.e. it's the same one you added to gradle.properties):
   ```bash
   # Do this for the TapTargetView repo only
   git config user.email "your@email.com"
   git config user.signingKey "your-key-id"
   ```

### Pushing a build

1. Edit gradle.properties, update the VERSION property for the new version release
1. Edit changelog, add relevant changes, note the date and new version (follow the existing pattern)
1. Add new `## [Unreleased]` header for next release
1. Verify that the everything works:
   ```bash
   ./gradlew clean check
   ```
1. Make a *signed* commit:
   ```bash
   git commit -S -m "Release version X.Y.Z"
   ```
1. Make a *signed* tag:
   ```bash
   git tag -s -a X.Y.Z
   ```
1. Upload binaries to Sonatype:
   ```bash
   ./gradlew publish
   ```
1. Go to [Sonatype](https://oss.sonatype.org/), log in with your credentials
1. Click "Staging Repositories"
1. Find the "comgetkeepsafe" repo, usually at the bottom of the list
1. "Close" the repository (select it then click the "close" button up top), the text field doesn't matter so put whatever you want in it
1. Wait until that's done
1. "Release" the repository, leave the checkbox checked.  Hooray, we're in Maven Central now!
1. Push all of our work to Github to make it official. Check previous [releases](https://github.com/KeepSafe/TapTargetView/releases) and edit tag release changes:
   ```bash
   git push --tags origin master
   ```
