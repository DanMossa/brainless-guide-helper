# Bruhsailor Ironman Guide Helper - Tasks

## Phase 1: Data Modeling & Schema
- [ ] Define the JSON schema for the guide (`guide-schema.json`).
    - [ ] Structure: Chapters -> Sections -> Steps.
    - [ ] Step model: Instructions, Prerequisites, Completion Conditions, Options.
    - [ ] Completion conditions types: `QUEST`, `SKILL`, `ITEM` (Inventory/Bank/Equipped), `DIARY`, `LEVEL_VARIABLE`.
- [ ] Create a prototype JSON file for Chapter 1.1 based on the provided guide text.

## Phase 2: Core Plugin Infrastructure
- [ ] Implement Java models for the JSON data.
- [ ] Implement a `GuideManager` to load and parse the JSON guide.
- [ ] Implement `Requirement` interface and its various implementations:
    - `QuestRequirement`
    - `SkillRequirement`
    - `ItemRequirement`
    - `DiaryRequirement`
- [ ] Implement `PlayerStateTracker` to keep track of:
    - Quest progress (using `QuestState`).
    - Skill levels and XP.
    - Inventory and Bank contents (using `ItemContainer`).
    - Achievement Diary progress (Varbit/Varp checks).

## Phase 3: Guide Logic & Progress Tracking
- [ ] Implement logic to evaluate `CompletionConditions` against current `PlayerState`.
- [ ] Support for Step Options:
    - Allow users to select which option they are taking for a step (e.g. 98 vs 99 FM).
    - Save these preferences in the plugin configuration.
- [ ] Support for manual completion:
    - Allow users to "Check off" a step if auto-detection is not possible or desired.
- [ ] Determine the "current step" based on progress.

## Phase 4: User Interface
- [ ] Create a `GuidePanel` (extends `PluginPanel`) to display the guide.
    - [ ] Display current Chapter/Section.
    - [ ] Display current step instructions and progress.
    - [ ] Display requirements and their status (Met/Not Met).
    - [ ] Render step options as radio buttons or a dropdown.
    - [ ] Checkbox for manual completion.
- [ ] Add navigation to view previous/next steps or browse the full guide.
- [ ] Implement guide overlays (optional):
    - Highlight items in bank/inventory needed for the step.
    - Arrow pointers to NPCs/Objects mentioned in the step (like Quest Helper).

## Phase 5: Testing & Verification
- [ ] Write unit tests for requirement evaluation.
- [ ] Test JSON loading and parsing.
- [ ] Test with a mock `Client` to simulate different player states.
- [ ] Verify that manual overrides work as expected.
- [ ] Verify that step options correctly update completion conditions.
