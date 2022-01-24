### Branch naming convention
- **master** - Main branch.
- **development** - Development branch.
- **release/${version}** - Release branch with release version. Example: release/1.0.1
- **feature/\[${taskId}_\]${name-with-dashes}** - Feature branch with optional task id. Example: feature/XYC-42_name-of-the-feature
- **bug/\[${taskId}_\]${name-with-dashes}** - Bug branch with optional task id. Example: bug/XYC-42_bug-note
- **hotfix/\[${taskId}_\]${name-with-dashes}** - Hotfix branch with optional task id. Example: hotfix/XYC-42_hotfix-note 

### Notes
- A ***development*** branch is created from ***master***.
- A ***release*** branch is created from ***development***.
- ***Feature***/***bug*** branches are created from ***development***.
- When a feature/bug is complete it is merged into the ***development*** branch.
- When the ***release*** branch is done it is merged into ***development*** and ***master***.
- If an issue in master is detected a ***hotfix*** branch is created from master.
- Once the hotfix is complete it is merged to both ***development*** and ***master***.
