# StatsUp

Android app for tracking and analyzing training activities imported from Strava.

Built with Kotlin, Jetpack Compose, Room, and Google Maps.

**[https://fracassi-marco.github.io/StatsUp](https://fracassi-marco.github.io/StatsUp)**

---

## Requirements

- Android Studio Ladybug or newer
- JDK 21
- Android SDK 34+
- Strava API credentials
- Google Maps API key

---

## Local setup

### 1. Clone the repo

```bash
git clone https://github.com/<your-org>/StatsUp.git
cd StatsUp
```

### 2. Create `local.properties`

Create a `local.properties` file in the project root (already git-ignored):

```properties
sdk.dir=/path/to/your/Android/sdk

maps.apiKey=YOUR_GOOGLE_MAPS_API_KEY
strava.clientId=YOUR_STRAVA_CLIENT_ID
strava.clientSecret=YOUR_STRAVA_CLIENT_SECRET
```

**Where to get the keys:**

| Key | Source |
|-----|--------|
| `maps.apiKey` | [Google Cloud Console](https://console.cloud.google.com) → APIs & Services → Maps SDK for Android |
| `strava.clientId` / `strava.clientSecret` | [Strava API Settings](https://www.strava.com/settings/api) |

### 3. Build and run

Open the project in Android Studio and run the `app` configuration, or from the command line:

```bash
./gradlew assembleDebug
```

---

## CI/CD (GitHub Actions)

Two workflows are included in `.github/workflows/`:

| Workflow | Trigger | What it does |
|----------|---------|--------------|
| `ci.yml` | Every push / PR | Unit tests + debug build |
| `release.yml` | Tag `v*` | Signed AAB + upload to Google Play (internal track) |

### Configuring GitHub Secrets

Go to **GitHub → repository → Settings → Secrets and variables → Actions** and add:

#### App secrets (required for all workflows)

| Secret | Value |
|--------|-------|
| `MAPS_API_KEY` | Google Maps API key |
| `STRAVA_CLIENT_ID` | Strava app client ID |
| `STRAVA_CLIENT_SECRET` | Strava app client secret |

#### Signing secrets (required for release only)

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | Release keystore encoded in base64 (see below) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Key password |

To encode the keystore:
```bash
base64 -i release.keystore | pbcopy   # macOS — copies to clipboard
base64 release.keystore               # Linux
```

#### Google Play secret (required for release only)

| Secret | Value |
|--------|-------|
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Full JSON content of the service account key |

**How to create the service account:**
1. [Google Play Console](https://play.google.com/console) → Setup → API access
2. Link to a Google Cloud project
3. Create a service account with the **Release manager** role
4. Download the JSON key and paste its full content as the secret value

### Releasing a new version

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`
2. Update `distribution/whatsnew/whatsnew-en-US` with the release notes
3. Commit, then push a tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow builds a signed AAB and publishes it to the **internal testing track**. Promote to production manually from the Play Console.

---

## Project structure

```
app/src/main/java/com/statsup/
├── domain/          # Models, business logic, use cases
├── infrastructure/  # Room DB, Strava API, export services
└── ui/
    ├── components/  # Compose screens and UI components
    ├── viewmodel/   # ViewModels
    └── theme/       # Material3 theme
```

## Tech stack

- **UI** — Jetpack Compose + Material3
- **Navigation** — Navigation Compose
- **Database** — Room
- **Maps** — Google Maps Compose
- **Auth** — AppAuth (OAuth 2.0 with Strava)
- **Build** — Gradle KTS, KSP
