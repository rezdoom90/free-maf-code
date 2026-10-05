
# PROJECT_STATE.md — Free MAF Code



Документ о состоянии и особенностях проекта A (Free MAF Code). Проект A — это фреймворк автоматизации, который кладётся в `<C>/agent/` любого проекта-цели C и работает над ним.



## TECH_STACK

- languages: Java 21

- ui: Swing (JFrame / JTextPane / JTextArea / JButton)

- native: JNA 5.17.0 (user32, gdi32, kernel32, Shcore)

- build: Maven, maven-compiler-plugin 3.13.0, maven-shade-plugin 3.6.0 (finalName=app)

- logging: собственный AppLogger (пишет в agent/app.log + stdout/stderr)

- tests: JUnit 5.11.4

- os: Windows 10-11, DPI PerMonitorV2 (через Shcore.SetProcessDpiAwareness)



## ПРОЕКТ A vs PROJECT C (как это работает)

- Проект A = free-maf-code (корень). В A вложена папка agent/ — это framework B, отдельный вспомогательный фреймворк, через который мы ведём разработку A. A сам по себе является шаблоном, который кладётся в C как <C>/agent/.

- Когда A разворачивается в C как agent/, все служебные файлы (WIP.md, MEMORY.md, PLAN.md) и вся история пишутся в <C>/agent/project/ и <C>/agent/history/.

- Запуск производится из корня C: <C>/agent/run.bat.

- run.bat детектит раскладку: dev (BUILD_DIR/agent/rules) vs dist (BUILD_DIR — сам agent/).



## АРХИТЕКТУРА



### Роли и пайплайн

- ANALYST -> PLANNER -> JUDGE(plan) -> EXECUTOR -> JUDGE(result) -> CODE_REVIEWER -> EXECUTOR (acceptance + commit).

- Каждый ответ агента: первая строка [ROLE], затем ровно один fenced PS-блок с первой строкой Write-Output "ROLE: ...".

- Маркеры в stdout PS-скрипта: USER_MSG:, AGENT_PAUSE:, AGENT_STOP:, AGENT_DONE:, ROLE:.

- AGENT_DONE для роли ANALYST — маршрутизация в PLANNER (не полная остановка).

- AGENT_STOP / AGENT_DONE для других ролей — HALT.

- Вердикты JUDGE: PLAN_APPROVED, PLAN_REJECTED, RESULT_APPROVED, RESULT_REJECTED.

- Вердикты CODE_REVIEWER: CODE_APPROVED, CODE_REJECTED.

- AGENT_PAUSE уважается только у ANALYST (уточнения) и у EXECUTOR (вопрос о приёмке).



### Оконная модель

- Окно исполнителя — Chrome с chat.deepseek.com. Управление через WinAPI + SendInput (без CDP/Selenium/Playwright/JS/официального API).

- Review-роли (JUDGE, CODE_REVIEWER) при необходимости открывают отдельное окно через ChromeLauncher.launchNewWindow. Оно живёт до вердикта; после вердикта закрывается.

- WindowManager: активация через AttachThreadInput + SetForegroundWindow; позиция (50,50,673,473) хранится и валидируется перед каждым взаимодействием.



### Пиксельные маркеры

- regenerate — правая кнопка (детект нового чата и старта генерации).

- copy_last — кнопка копирования последнего ответа.

- input_field, deepthink, search — статичные точки (калибровка и позиционирование клика).

- marker.retry — кнопка "перегружено, попробовать снова" (DeepSeek overload).

- marker.send — кнопка SEND в окне Chrome (детект загрузки вложений).

- Все маркеры одинаковы для нового и старого чата: одинаковый размер окна -> одинаковые координаты и цвета.



### Файловые потоки

- script.ps1 — временный контейнер доставки изменений. Сохраняется в UTF-8 с BOM (ScriptSaver). После выполнения удаляется (AutomationLoop.deleteDeliveryScript). Никогда не коммитится.

- agent/history/prompts/ — все промпты, отправленные агенту (HistoryService.savePrompt).

- agent/history/responses/ — все ответы агента (HistoryService.saveResponse).

- agent/history/scripts/ — все PS-скрипты, полученные от агента (ScriptLogger.save).

- agent/history/executions/ — stdout/stderr/exitCode каждого PS-скрипта (ExecutionLogger.save).

- agent/project/chat_history.txt — история чата (для отображения в UI).

- agent/project/WIP.md — журнал текущей задачи: задача пользователя, требования, замечания, план действий, состояние сборки. Переживает переключение чатов.

- agent/project/PLAN.md — текущий план.

- agent/project/MEMORY.md — долговременная память. После завершения задачи агент обязан перенести в неё все важные для понимания проекта сведения из WIP.md (сжато), затем очистить WIP.md.

- TASK.md во фреймворке не используется. Исходная постановка задачи документируется в WIP.md; по завершении — важное переезжает в MEMORY.md.



### Кодировки и вывод PS

- Скрипт сохраняется в UTF-8 с BOM.

- PowerShellExecutor запускает скрипт через -Command с принудительным [Console]::OutputEncoding = UTF8, чтобы русский текст в stdout не превращался в кракозябры.

- stdout/stderr читаются как UTF-8.



### Файлы и вложения

- Пользователь прикрепляет файлы через кнопку Attach Files или drag-and-drop на поле ввода (не на область чата).

- Прикреплённые файлы показываются списком над полем ввода, у каждого — кнопка ✕.

- Лимиты: до 20 файлов, каждый до 50 МБ (AttachmentValidator).

- При отправке файлы кладутся в буфер обмена Windows как CF_HDROP (ClipboardService.setFiles), затем Ctrl+V -> Chrome принимает как вложения. Второе Ctrl+V — текст сообщения.

- Детект загрузки: серая кнопка SEND (30 мс, макс 3 сек), затем активная (100 мс, макс 2 мин с диалогом-перезапуском).

- SEND disabled, если поле пустое И нет файлов. Иначе — enabled. Клик по SEND = Enter.



### Скорость (Speed Mode)

- Кнопка-переключатель "Speed Mode: <label>" в нижнем баре. Клик циклически переключает: Instant -> Fast -> Normal -> Slow -> Very Slow -> Instant.

- Значение сохраняется в agent/config.properties (speed.mode).

- Задержки задаются в SpeedMode (min/max мс) и применяются в InputSimulator.applySpeedDelay перед каждым Enter.



### Кодировка UI

- inputArea (JTextArea) поддерживает Ctrl+C/V/X через композитный TransferHandler, который делегирует copy/paste штатному дефолтному handler.

- Drag-and-drop файлов обрабатывается тем же композитным handler (для flavour javaFileListFlavor).



### Промпты пользователя

- Первое сообщение пользователя в текущий чат исполнителя уходит с rules+context (SystemInstructionProvider.getExecutorRules + ContextPackager.buildContext).

- Каждое сообщение пользователя уходит с format reminder в конце.

- Флаг executorContextRequired сбрасывается при пересоздании окна исполнителя (MainFrame.recover -> AutomationLoop.resetExecutorContext).

- Файлы, прикреплённые к сообщению, уходят вместе с этим сообщением (тот же Prompt).



## ОСОБЕННОСТИ И ТОНКОСТИ



### Автозапуск и prerequisites

- run.bat в корне C вызывает <C>/agent/ensure-prerequisites.ps1 (не agent/ensure-prerequisites.ps1!).

- ensure-prerequisites.ps1 ставит JDK 21 Temurin (winget -> fallback ZIP), Git, Maven 3.9.9 (ZIP в %LOCALAPPDATA%\Programs\Maven).

- PATH/JAVA_HOME/MAVEN_HOME для текущей сессии cmd пишутся в %TEMP%\freemaf-env.bat (ANSI, без кавычек вокруг путей — иначе cmd ломается).

- run.bat перед запуском JAR проверяет, есть ли в нём com/freemaf/agent/Main.class. Если нет — пересобирает.



### Система защиты

- EmergencyStop: Ctrl+Shift+S (глобальный хоткей через polling GetAsyncKeyState).

- Кнопка Stop в UI: kill PS-процесса, статус STOPPED.

- Если окно Chrome потеряно — MainFrame.recover показывает диалог, при согласии ищет/открывает новое окно и сбрасывает executorContextRequired.



### Лимиты вывода

- ps.output.limit_chars (default 60000). Если stdout скрипта превышает — в лог агенту уходит EXECUTION_OUTPUT_LIMIT_EXCEEDED с рекомендациями разбить чтение.



### Кириллица

- Весь вывод PS читается как UTF-8. Ответы агента и промпты сохраняются как UTF-8 без BOM.

- .java-файлы пишутся только через скрипт (не напрямую), чтобы не появлялось BOM на .java.



### Отличия от framework B

- Framework B — это старая версия того же фреймворка, лежащая в <A>/agent/. Мы её НЕ трогаем.

- В A нет agent/project/ — этот путь появляется только в C после развёртывания.

- A не хранит состояния, специфичного для C.



## CHANGELOG



### 1.1.0 — 2026-10-05 — Восстановление исходников и перенос функционала

- Восстановлены все Java-файлы проекта A после потери исходников.

- S1: переименование "DeepSeek Agent Bridge" -> "Free MAF Code".

- S2: dropdown скорости -> кнопка Speed Mode (циклическое переключение).

- S3.0-S3.5: файлы пользователя (CF_HDROP), SEND-детект через пиксель, SendButtonState, AttachmentValidator, Prompt(userInitiated), обёртка rules+context в AutomationLoop.

- S4: кнопка SEND.

- S5: тесты (36 — все зелёные).

- S6: README, WIP.

- S7: инфраструктура (run.bat с JAR-валидацией, ensure-prerequisites.ps1, sanitized env), фикс кодировки PS, Ctrl+C/V через композитный TransferHandler.

- Директивы агента обновлены: TASK.md удалён из служебных файлов; в блок ЗАПРЕТЫ добавлено напоминание проверять Test-Path перед Get-Content.
