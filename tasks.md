# Bruhsailor Ironman Guide Helper - Tasks

## Phase 1: Data Modeling & Schema
- [x] Define the JSON schema for the guide (`guide-schema.json`).
    - [x] Structure: Chapters -> Sections -> Steps.
    - [x] Step model: Instructions, Prerequisites, Completion Conditions, Options.
    - [x] Completion conditions types: `QUEST`, `SKILL`, `ITEM` (Inventory/Bank/Equipped), `DIARY`, `LEVEL_VARIABLE`.
- [x] Create a prototype JSON file for Chapter 1.1 based on the provided guide text.

## Phase 2: Core Plugin Infrastructure
- [x] Implement Java models for the JSON data.
- [x] Implement a `GuideManager` to load and parse the JSON guide.
- [x] Implement `Requirement` interface and its various implementations:
    - `QuestRequirement`
    - `SkillRequirement`
    - `ItemRequirement`
    - `DiaryRequirement`
- [x] Implement `PlayerStateTracker` to keep track of:
    - Quest progress (using `QuestState`).
    - Skill levels and XP.
    - Inventory and Bank contents (using `ItemContainer`).
    - Achievement Diary progress (Varbit/Varp checks).

## Phase 3: Guide Logic & Progress Tracking
- [x] Implement logic to evaluate `CompletionConditions` against current `PlayerState`.
- [x] Support for Step Options:
    - Allow users to select which option they are taking for a step (e.g. 98 vs 99 FM).
    - Save these preferences in the plugin configuration.
- [x] Support for manual completion:
    - Allow users to "Check off" a step if auto-detection is not possible or desired.
- [x] Determine the "current step" based on progress.

## Phase 4: User Interface
- [x] Create a `GuidePanel` (extends `PluginPanel`) to display the guide.
    - [x] Display current Chapter/Section.
    - [x] Display current step instructions and progress.
    - [x] Display requirements and their status (Met/Not Met).
    - [x] Render step options as radio buttons or a dropdown.
    - [x] Checkbox for manual completion.
- [x] Add navigation to view previous/next steps or browse the full guide.
- [ ] Implement guide overlays (optional):
    - Highlight items in bank/inventory needed for the step.
    - Arrow pointers to NPCs/Objects mentioned in the step (like Quest Helper).

## Phase 5: Testing & Verification
- [ ] Write unit tests for requirement evaluation.
- [ ] Test JSON loading and parsing.
- [ ] Test with a mock `Client` to simulate different player states.
- [ ] Verify that manual overrides work as expected.
- [ ] Verify that step options correctly update completion conditions.
