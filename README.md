# ⭐ JustGram

<p align="center">
  <b>JustGram</b> — форк Telegram / Cherrygram 11.3.0 с мульти-системой бейджей, режимом призрака, движком плагинов и собственной системой поддержки.
</p>

<p align="center">
  <a href="https://github.com/Funtyra52/JustGram/releases"><img src="https://img.shields.io/badge/Release-v0.1.0--beta.1-blue.svg" alt="Release"></a>
  <a href="https://github.com/Funtyra52/JustGram/actions"><img src="https://img.shields.io/badge/Build-GitHub_Actions-brightgreen.svg" alt="CI"></a>
  <a href="https://github.com/Funtyra52/JustGram/blob/main/Supporters.txt"><img src="https://img.shields.io/badge/Supporters-Live_Sync-orange.svg" alt="Supporters"></a>
</p>

---

## ✨ Основные особенности JustGram

### 🌟 Мульти-система бейджей
* Одновременное отображение бейджей **в один ряд**:
  1. **JustGram Developer / Supporter** — официальные бейджи нашего клиента.
  2. **CherryGram Supporter** — бейджи сообщества Cherrygram.
  3. **exteraGram Supporter** — бейджи сообщества exteraGram.
  4. **Telegram Premium** — эмодзи-статусы и звезда премиума.
* Интерактивное окно с подробной информацией о каждом бейдже при нажатии на него в профиле или шапке диалога.

### 💰 Система спонсоров (Supporters)
* Хочешь персональный бейдж JustGram? Закинь автору 100₽, и твой Telegram ID будет добавлен в файл [`Supporters.txt`](Supporters.txt).
* Клиент на лету подгружает список с GitHub и рисует бейдж рядом с твоим ником у всех пользователей JustGram!

### 👻 Режим призрака (Ghost Mode)
* **Скрытие статуса «В сети»** (suppress `account.updateStatus`).
* **Скрытие набора текста** (suppress `messages.setTyping`).
* **Нечиталка сообщений** (просматривай сообщения без отправки статуса прочтения собеседнику).
* **Нечиталка историй** (просматривай Stories инкогнито).
* *Безопасность*: клиент гарантированно не сохраняет удалённые сообщения и не нарушает целостность переписок.

### 🧩 Движок плагинов (Extera Engine)
* Портированная модульная система плагинов из exteraGram.
* **Safe Mode**: автоматический безопасный режим при сбое любого стороннего плагина для предотвращения bootloop.
* Изоляция ошибок в try-catch и оптимизация рендеринга на главном потоке.

### ⚙️ Вкладка «Настройки JustGram»
* Отдельный раздел настроек прямо в главном меню рядом с настройками Cherrygram.
* Быстрый доступ к Призраку, Плагинам, списку спонсоров и статусу бейджей.

---

## 🛠️ Сборка APK

### Автоматическая сборка (GitHub Actions)
В репозитории настроен автоматический CI/CD workflow `.github/workflows/build.yml`.
При каждом коммите или пуше тега готовый APK собирается на облачных серверах и выкладывается во вкладку **Actions -> Artifacts**.

### Локальная сборка
Требования: JDK 17, Android SDK 34/36, Android NDK r27b, CMake 3.22.1.

```bash
# Сборка Standalone APK (рекомендуется)
./gradlew :TMessagesProj_AppStandalone:assembleAfatStandalone

# Сборка стандартного Release APK
./gradlew :TMessagesProj_App:assembleAfatRelease
```

Готовый APK появится в:
`TMessagesProj_AppStandalone/build/outputs/apk/afat/standalone/`

---

## 👑 Автор и разработка
* **Создатель & Lead Developer**: [Funtyra52](https://github.com/Funtyra52) (Telegram ID: `5142967559`)
* **База клиента**: Cherrygram 11.3.0 & Telegram v12.10.6

## ❤️ Благодарности
* [Cherrygram](https://github.com/arsLan4k1390/Cherrygram)
* [exteraGram](https://github.com/exteraSquad/exteraGram)
* [Telegram Android Open Source](https://github.com/DrKLO/Telegram)
