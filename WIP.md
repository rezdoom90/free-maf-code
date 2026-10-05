# WIP.md

## Состояние
Framework B (папка `agent/`) — рабочий фреймворк, не трогаем.
Проект A (Free MAF Code) восстановлен после потери исходников из этой сессии.

## Что сделано в этой сессии
- S1: переименование "DeepSeek Agent Bridge" → "Free MAF Code" в README.md, AGENT_INSTRUCTIONS.md, run.bat.
- S2: JComboBox скорости заменён на кнопку "Speed Mode: <label>" с циклическим переключением; SpeedMode.next(); сохранение в config speed.mode.
- S3.0: AttachmentValidator, SendButtonState, Config-ключи (marker.send.*, attachments.*, timeout.attach_*, ps.output.limit_chars).
- S3.1: ClipboardService.setFiles (CF_HDROP через JNA) + WinApiService clipboard/GlobalAlloc API.
- S3.2: InputSimulator.typeFilesAndTextViaClipboard, typeTextWithoutEnter, waitForSendButtonState, WaitRetryCallback.
- S3.3: AutoCalibrator.calibrateSendDisabled/Active (маркер SEND: серый/активный цвета).
- S3.4a: Prompt(text, files, userInitiated) record; PromptQueue на Prompt; DeepSeekSession.getValidResponse(prompt, files), sendPrompt(prompt, files).
- S3.4b: MainFrame — кнопка Attach Files, кнопка SEND, drag-and-drop на inputArea/inputScroll/inputWrap, список файлов с кнопками ✕, submitPrompt с валидацией.
- S3.5: AutomationLoop — userInitiated prompt оборачивается в format-reminder всегда, в rules+context — только при свежем executor-окне; resetExecutorContext() из MainFrame.recover().
- S5: тесты SpeedModeTest (next), AttachmentValidatorTest, SendButtonStateTest, ClipboardServiceTest (DROPFILES header).
- S6: README.md обновлён под новые фичи.

## Что вне этой сессии (инфраструктура запуска)
- ensure-prerequisites.ps1 (в корне проекта A): ставит JDK 21 Temurin (winget → fallback ZIP), Git, Maven 3.9.9;
  санитизирует PATH/JAVA_HOME/MAVEN_HOME (без кавычек), пишет %TEMP%\freemaf-env.bat в ANSI.
- run.bat: сначала ensure-prerequisites, потом проверка JAR на наличие com/freemaf/agent/Main.class, при отсутствии — пересборка.
- .gitignore: защита от *.jar/*.class вне target/.

## Заметки
- Окно DeepSeek рендерит маркеры одинаково для нового и старого чата (одинаковый размер окна, одинаковые координаты) — отдельные наборы маркеров не нужны.
- script.ps1 сохраняется с BOM (ScriptSaver).
- CF_HDROP: Chrome принимает файлы из буфера как вложения по Ctrl+V.
- Детект загрузки: серая кнопка (30 мс × 3 сек таймаут), затем активная (100 мс × 2 мин, диалог-перезапуск).

## Следующий шаг
Пользователь запускает run.bat из подопытного проекта (например, calculator/agent) и сообщает о проблемах.
