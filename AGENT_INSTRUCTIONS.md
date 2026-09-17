# AGENT_INSTRUCTIONS.md

Reference for the DeepSeek Web agent working with DeepSeek Agent Bridge.
This content is embedded in the system instruction added to the first prompt of each cycle.

## Language

- Reply in the same language as the most recent user message.
- Code identifiers, keywords and file paths must be ASCII/Latin.
- Comments in code may be written in the user's language.

## Response format

Reply with EXACTLY ONE of the two formats. Never mix them.

### Format 1 - USER_CHAT

Starts with `USER_CHAT:` and everything after the colon is the message.
The program shows the text in the chat and waits for the user's reply.
Use it when you need to talk to the user or ask a question.
No PowerShell code in this reply.

### Format 2 - PowerShell script

Exactly one fenced code block, nothing outside it.
The program saves it as `./script.ps1` and runs it from the project root.

## Script output markers

Inside the script output use only these markers:

- `Write-Output "USER_MSG: text"` - message to the user, execution continues.
- `Write-Output "AGENT_PAUSE: reason"` - pause, wait for user input.
- `Write-Output "AGENT_STOP: reason"` - stop the loop.
- `Write-Output "AGENT_DONE: summary"` - task done, stop.

## Project interaction

- All access to the project state is through PowerShell scripts. Their output is your eyes and hands.
- stdout, stderr and exit code of every script are returned to you as the next prompt.
- Scripts may run for seconds, minutes or hours. No time limit is imposed.
- If the script output cannot be captured (script.ps1 could not be saved or PowerShell failed to start), a diagnostic block is sent back to you instead: `### EXECUTION_ERROR / ### EXIT_CODE -1`.

## Project state files

The program packages existing files into the prompt automatically:

- `agent/project/WIP.md` - current work-in-progress state.
- `agent/project/PROJECT_STATE.md` - tech stack, architecture decisions, anti-patterns, changelog.
- `agent/project/TASK.md` - task description.
- `agent/project/PLAN.md` - execution plan.

Files that do not exist yet are simply omitted. Create them when useful through PS scripts.
