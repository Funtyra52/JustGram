#!/usr/bin/env python3
import time
import os
import sys

LOG_FILE = "/storage/emulated/0/test_agent.txt"

def get_timestamp():
    return time.strftime("%H:%M:%S UTC", time.gmtime())

def append_log(text):
    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write("\n" + text.strip() + "\n")
    print(f"[{get_timestamp()}] [UserWatcher] Logged: {text[:60]}...", flush=True)

def get_file_content():
    if not os.path.exists(LOG_FILE):
        return ""
    with open(LOG_FILE, "r", encoding="utf-8") as f:
        return f.read()

last_len = len(get_file_content())
print("[UserWatcher] Started watching test_agent.txt every 40 seconds...", flush=True)

while True:
    time.sleep(40)
    try:
        content = get_file_content()
        if len(content) > last_len:
            diff = content[last_len:].strip()
            # Filter out bot logs and dividers
            ignored_prefixes = (
                "[", "-", "=", "•", "*", "🎉", "💥", "Время:", "Коммит:", "Скачиваю",
                "ГОТОВО", "Ответ агента", "СБОРКА", "Шаг:", "Статус:", "Ссылка:",
                "1. /storage", "2. /storage", "Все требования", "вылеты"
            )
            user_lines = [
                line.strip() for line in diff.splitlines()
                if line.strip()
                and not any(line.strip().startswith(p) for p in ignored_prefixes)
            ]
            if user_lines:
                msg = " ".join(user_lines)
                print(f"[UserWatcher] Detected user message: {msg}", flush=True)
                reply = (
                    f"--------------------------------------------------\n"
                    f"[{get_timestamp()}] Ответ агента на «{msg}»:\n"
                    f"Сообщение принято! Работа сборки продолжается.\n"
                    f"Задачи в фоне активны: мониторинг файла (каждые 40 сек)\n"
                    f"и мониторинг сборки (каждые 4 мин).\n"
                    f"--------------------------------------------------"
                )
                append_log(reply)
            last_len = len(get_file_content())
        else:
            last_len = len(content)
    except Exception as e:
        print(f"[UserWatcher] Error: {e}", file=sys.stderr, flush=True)
