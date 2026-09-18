# WIP.md

## Task
Free MAF Code UI update: rename project, replace speed dropdown with button, add user file transfer, add SEND button.

## Completed
- S1: renamed "DeepSeek Agent Bridge" to "Free MAF Code" in README.md, AGENT_INSTRUCTIONS.md, run.bat.
- S2: replaced JComboBox speed selector with a cycling JButton "Speed Mode: <current>" in MainFrame; added SpeedMode.next(); persisted via config speed.mode.
- S3.0: added Config keys (marker.send.*, attachments.*, timeout.attach_*); new AttachmentValidator; new SendButtonState; tests for both.
- S3.1: WinApiService clipboard API (OpenClipboard/EmptyClipboard/SetClipboardData/CloseClipboard/GlobalAlloc etc.); ClipboardService.setFiles() writes CF_HDROP payload.
- S3.2: InputSimulator.typeFilesAndTextViaClipboard(List<File>, String) with grey/active SEND polling (30 ms / 3 s and 100 ms / 2 min with retry dialog); typeTextWithoutEnter().
- S3.3: AutoCalibrator.calibrateSendDisabled / calibrateSendActive; MainFrame.runCalibrationSequence reads both colours around calibration prompt.
- S3.4a: Prompt(text, files, userInitiated) record; PromptQueue overloads; DeepSeekSession.getValidResponse(prompt, files) and sendPrompt(prompt, files); AutomationLoop takes Prompt.
- S3.4b: MainFrame UI - Attach Files button, SEND button, drag-and-drop on ChatConsole, attached files label, submitPrompt with validation and Prompt.user(text, files).
- S3.5: AutomationLoop wraps user-initiated prompts with format reminder every time, and with rules+context only when the current executor window is fresh; resetExecutorContext() called from MainFrame.recover().
- S5: tests for SpeedMode.next, AttachmentValidator limits, SendButtonState, ClipboardService.buildDropFilesData (DROPFILES header, NUL-terminated UTF-16 paths).
- S6: README.md rewritten with new UI sections (Speed Mode button, Attach Files, SEND, upload detection).

## Files modified
- README.md, AGENT_INSTRUCTIONS.md, run.bat (rename)
- src/main/java/com/freemaf/agent/SpeedMode.java (next())
- src/main/java/com/freemaf/agent/MainFrame.java (button, attach, SEND, drag-and-drop, currentLoop, submitPrompt)
- src/main/java/com/freemaf/agent/Config.java (new defaults)
- src/main/java/com/freemaf/agent/AttachmentValidator.java (new)
- src/main/java/com/freemaf/agent/SendButtonState.java (new)
- src/main/java/com/freemaf/agent/Prompt.java (new)
- src/main/java/com/freemaf/agent/PromptQueue.java (Prompt payload)
- src/main/java/com/freemaf/agent/ClipboardService.java (CF_HDROP)
- src/main/java/com/freemaf/agent/InputSimulator.java (files+text, SEND wait)
- src/main/java/com/freemaf/agent/AutoCalibrator.java (SEND disabled/active)
- src/main/java/com/freemaf/agent/DeepSeekSession.java (getValidResponse/sendPrompt overloads)
- src/main/java/com/freemaf/agent/AutomationLoop.java (Prompt, wrapping, resetExecutorContext)
- src/main/java/com/freemaf/agent/winapi/WinApiService.java (clipboard + global memory APIs)

## Remaining
- S7.1: full mvn test passed.
- S7.2: manual smoke test in the running application.

## Next step
User manual verification.
