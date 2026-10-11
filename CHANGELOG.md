# Changelog

All notable changes to the JustGram project will be documented in this file.

## [0.1.0-beta.1] - 2026-10-11

### Added
- **Base Fork**: Rebased and updated to CherryGram 11.3.0 codebase.
- **Multi-Badge System**:
  - Full support for multi-badge rendering combining JustGram, CherryGram, exteraGram, and Telegram Premium badges.
  - Sequential chain rendering (`JustGram` $\rightarrow$ `CherryGram` $\rightarrow$ `exteraGram` $\rightarrow$ `Telegram Premium`) preventing icon overlap in `DialogCell`, `ProfileActivity`, and chat header titles (`ChatAvatarContainer`).
  - Remote badge fetching and caching with offline fallback and strict NPE protection against malformed API responses.
  - Multi-badge detail dialog in `ProfileActivity` explaining all badges held by the user.
- **Ghost Mode (Режим призрака)**:
  - Granular privacy toggles:
    - Hide online presence (`account.updateStatus` suppressed).
    - Hide typing indicator (`sendTyping` suppressed).
    - Don't mark incoming messages as read (`completeReadTask` / `readHistory` suppressed).
    - Don't mark stories as read (`readStories` suppressed).
  - Master "Ghost Mode" switch activating all hide mechanisms simultaneously.
  - Dedicated preferences screen in JustGram Settings.
  - **Strict Security Guarantee**: Anti-deleted messages saving hooks are completely excluded from the codebase, preserving chat integrity and user privacy.
- **JustGram Settings Tab (Настройки JustGram)**:
  - Added dedicated top-level entry point in Settings right next to Cherrygram settings.
  - Houses all JustGram features: Ghost Mode, Plugin Engine, Multi-Badge system, Supporters sync, and project information.
- **Supporters Remote List & Custom Badge**:
  - Direct integration with `https://raw.githubusercontent.com/Funtyra52/JustGram/main/Supporters.txt`.
  - Automatic parsing and caching of supporters IDs to display the official JustGram Supporter badge.
  - Dedicated developer badge with personalized description and particle effect for creator/developer ID `5142967559`.
  - Multi-badge horizontal row display supporting JustGram, CherryGram, exteraGram, and Telegram Premium simultaneously with interactive info dialog on tap.
- **New App Branding**:
  - Integrated custom app icon asset across all mipmap densities (mdpi to xxxhdpi) and adaptive launcher icons.
  - Integrated full-color high-resolution `justgram_badge` asset into drawable resources.
- **Plugin Engine (exteraGram Engine)**:
  - Plugin architecture ported from exteraGram (`IPlugin`, `PluginManager`, `PluginMetadata`).
  - **Safe Mode**: Startup crash detection displaying an automatic recovery dialog (*"Обнаружен сбой при запуске. Запустить JustGram в безопасном режиме (без плагинов)?"*) to eliminate bootloop risks.
  - Individual plugin try-catch fault isolation ensuring a crashing plugin is quarantined without taking down the entire client.
  - **UI Optimization**: Reflection caching (`ConcurrentHashMap`), dirty-checking on views to avoid unnecessary relayouts on the main thread, and fast-scroll throttling.

### Known Issues
- **UI Plugin Microfreezes**: Third-party plugins performing complex view injections or heavy layout measures may cause occasional UI microfreezes or frame drops under intense list scrolling.
