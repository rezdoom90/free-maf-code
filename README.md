# DeepSeek Agent Bridge

A Windows automation bridge that drives a manually opened Chrome window with the
DeepSeek Web chat and uses it as a remote LLM agent for your project.

The bridge does not use CDP, Selenium, Playwright, browser extensions, remote
debugging, the DeepSeek API or JavaScript injection. All control is through
WinAPI and OS-level virtual input (SendInput).

## What it does

- Finds or launches a Chrome window on https://chat.deepseek.com/.
- Sends a prompt into the DeepSeek chat input.
- Waits for the response, copies the last code block through the DeepSeek copy button.
- Either executes the response as a PowerShell script, or shows it in the chat
  window as a user message (if the agent chose the USER_CHAT format).
- Feeds stdout/stderr/exit code back to the agent as the next prompt.
- Repeats until the agent emits AGENT_DONE / AGENT_STOP or the user presses Stop.

## Requirements

- Windows 10 or 11, display scale 225%.
- JDK 21 or newer (`java` in PATH).
- Maven 3.8 or newer (`mvn` in PATH) for the initial build.
- Google Chrome installed.
- The DeepSeek web chat reachable at https://chat.deepseek.com/.

## Install

1. Download the framework zip from the GitHub releases page.
2. Extract the contents into an `agent/` folder in the root of your project:

       <your-project>/agent/

3. From your project root, run:

       agent\run.bat

   On the first run the script compiles the framework with Maven and starts the
   GUI. Subsequent runs reuse the compiled jar and start immediately.
4. Chrome opens (or is focused) on the DeepSeek chat. Log in if needed.
5. Click Start and wait for the automatic calibration to finish.

## Files created at runtime

- `config.properties` — window position, marker coordinates and colors, timeouts.
- `script.ps1` — last PowerShell script received from the agent.
- `agent/app.log` — application log.
- `agent/project/WIP.md`, `PROJECT_STATE.md`, `TASK.md`, `PLAN.md` — files the agent
  may create and update to keep track of project state.
- `agent/history/` — prompts, responses, scripts and execution logs.

## Agent protocol

The agent replies in one of two formats only:

- `USER_CHAT: <text>` — a message for the user. The program shows it in the chat
  and waits for the next user input.
- One fenced PowerShell code block — executed as `script.ps1` from the project root.

Inside the script output the agent can emit the markers `USER_MSG`, `AGENT_PAUSE`,
`AGENT_STOP`, `AGENT_DONE`.

## License

MIT. See `LICENSE`.
