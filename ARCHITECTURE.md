# NOVA OS Mobile — Architecture

## Layers

1. **Presentation:** `MainActivity.kt` creates the shell and navigation.
2. **Command layer:** `executeCommand()` parses an allowlist and invokes Android APIs.
3. **Android integration:** `PackageManager` queries launcher activities; intents launch apps, install packages, uninstall packages, and open Settings.
4. **Java interoperability:** `DeviceInfo.java` reads Android Build metadata.
5. **Native layer:** `native-lib.cpp` exports a JNI method confirming C++ library loading.

## Security boundaries

- No root escalation, `su`, arbitrary shell, or hidden background execution.
- No silent package installation or removal.
- Android's installer and uninstall confirmation remain in control.
- No broad `QUERY_ALL_PACKAGES` permission.
- No contacts, location, microphone, camera, SMS, or storage-wide permissions.

## Terminal command reference

| Command | Behavior |
|---|---|
| `help` | List supported commands |
| `clear` | Clear terminal output |
| `date`, `time` | Show local date/time |
| `device` | Show Android and native-layer information |
| `apps` | Report launchable app count |
| `uptime` | Show NOVA session uptime |
| `version` | Show app version |
| `settings` | Open Android Settings |
| `install` | Open Android document picker for an APK |
| `uninstall <package>` | Open Android uninstall confirmation |
| `open <package>` | Launch an app with a launcher activity |

## Extension points

- Extract command parsing into a `CommandRegistry`.
- Move app discovery into a repository.
- Replace programmatic views with Jetpack Compose or fragments.
- Add Room for persistent terminal history and workspace metadata.
- Add instrumentation tests for commands and Android intent flows.
- Add default-home launcher onboarding if the intended product is a launcher rather than an OS replacement.
