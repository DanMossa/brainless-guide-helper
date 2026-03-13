package com.brainlessguidehelper;

import com.brainlessguidehelper.models.Chapter;
import com.brainlessguidehelper.models.Guide;
import com.brainlessguidehelper.models.Requirement;
import com.brainlessguidehelper.models.Section;
import com.brainlessguidehelper.models.Step;
import com.brainlessguidehelper.models.StepOption;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Manages guide progress by evaluating completion conditions against the current player state,
 * tracking step option selections, manual completions, and determining the current step.
 */
@Slf4j
@Singleton
public class GuideProgressManager
{
	private static final String CONFIG_GROUP = "brainlessguidehelper";
	private static final String STEP_OPTION_PREFIX = "stepOption_";
	private static final String MANUAL_COMPLETE_PREFIX = "manualComplete_";

	private final PlayerStateTracker playerStateTracker;
	private final GuideManager guideManager;
	private final ConfigManager configManager;

	@Inject
	public GuideProgressManager(PlayerStateTracker playerStateTracker, GuideManager guideManager, ConfigManager configManager)
	{
		this.playerStateTracker = playerStateTracker;
		this.guideManager = guideManager;
		this.configManager = configManager;
	}

	/**
	 * Evaluate whether a step's completion conditions are met.
	 * If the step has options and the user has selected one, evaluates that option's conditions instead.
	 * Also returns true if the step has been manually completed.
	 */
	public boolean isStepComplete(Step step)
	{
		if (step == null)
		{
			return false;
		}

		// Check manual completion first
		if (isManuallyCompleted(step.getId()))
		{
			return true;
		}

		// If the step has options and a selection was made, evaluate the selected option's conditions
		if (step.getOptions() != null && !step.getOptions().isEmpty())
		{
			String selectedOptionId = getSelectedOption(step.getId());
			if (selectedOptionId != null)
			{
				StepOption selectedOption = findOption(step, selectedOptionId);
				if (selectedOption != null && selectedOption.getCompletionConditions() != null)
				{
					return playerStateTracker.areAllRequirementsMet(selectedOption.getCompletionConditions());
				}
			}
			// No option selected yet; fall through to default completion conditions
		}

		// Evaluate the step's default completion conditions
		List<Requirement> conditions = step.getCompletionConditions();
		return playerStateTracker.areAllRequirementsMet(conditions);
	}

	/**
	 * Check whether all prerequisites for a step are met.
	 */
	public boolean arePrerequisitesMet(Step step)
	{
		if (step == null)
		{
			return false;
		}
		return playerStateTracker.areAllRequirementsMet(step.getPrerequisites());
	}

	// --- Step Option Selection ---

	/**
	 * Set the selected option for a step. Saved to plugin configuration.
	 */
	public void setSelectedOption(int stepId, String optionId)
	{
		configManager.setConfiguration(CONFIG_GROUP, STEP_OPTION_PREFIX + stepId, optionId);
		log.debug("Step {} option set to: {}", stepId, optionId);
	}

	/**
	 * Get the selected option for a step, or null if none selected.
	 */
	public String getSelectedOption(int stepId)
	{
		return configManager.getConfiguration(CONFIG_GROUP, STEP_OPTION_PREFIX + stepId);
	}

	/**
	 * Clear the selected option for a step.
	 */
	public void clearSelectedOption(int stepId)
	{
		configManager.unsetConfiguration(CONFIG_GROUP, STEP_OPTION_PREFIX + stepId);
		log.debug("Step {} option cleared", stepId);
	}

	// --- Manual Completion ---

	/**
	 * Mark a step as manually completed.
	 */
	public void setManuallyCompleted(int stepId, boolean completed)
	{
		if (completed)
		{
			configManager.setConfiguration(CONFIG_GROUP, MANUAL_COMPLETE_PREFIX + stepId, "true");
			log.debug("Step {} manually marked as complete", stepId);
		}
		else
		{
			configManager.unsetConfiguration(CONFIG_GROUP, MANUAL_COMPLETE_PREFIX + stepId);
			log.debug("Step {} manual completion removed", stepId);
		}
	}

	/**
	 * Check if a step has been manually marked as complete.
	 */
	public boolean isManuallyCompleted(int stepId)
	{
		String value = configManager.getConfiguration(CONFIG_GROUP, MANUAL_COMPLETE_PREFIX + stepId);
		return "true".equals(value);
	}

	// --- Current Step Determination ---

	/**
	 * Determine the current step by finding the first incomplete step
	 * in guide order (chapters -> sections -> steps).
	 * Returns null if all steps are complete or the guide is not loaded.
	 */
	public Step getCurrentStep()
	{
		Guide guide = guideManager.getGuide();
		if (guide == null || guide.getChapters() == null)
		{
			return null;
		}

		for (Chapter chapter : guide.getChapters())
		{
			if (chapter.getSections() == null)
			{
				continue;
			}
			for (Section section : chapter.getSections())
			{
				if (section.getSteps() == null)
				{
					continue;
				}
				for (Step step : section.getSteps())
				{
					if (!isStepComplete(step))
					{
						return step;
					}
				}
			}
		}

		// All steps complete
		return null;
	}

	/**
	 * Get the chapter containing the current step, or null if all complete.
	 */
	public Chapter getCurrentChapter()
	{
		Guide guide = guideManager.getGuide();
		if (guide == null || guide.getChapters() == null)
		{
			return null;
		}

		for (Chapter chapter : guide.getChapters())
		{
			if (chapter.getSections() == null)
			{
				continue;
			}
			for (Section section : chapter.getSections())
			{
				if (section.getSteps() == null)
				{
					continue;
				}
				for (Step step : section.getSteps())
				{
					if (!isStepComplete(step))
					{
						return chapter;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get the section containing the current step, or null if all complete.
	 */
	public Section getCurrentSection()
	{
		Guide guide = guideManager.getGuide();
		if (guide == null || guide.getChapters() == null)
		{
			return null;
		}

		for (Chapter chapter : guide.getChapters())
		{
			if (chapter.getSections() == null)
			{
				continue;
			}
			for (Section section : chapter.getSections())
			{
				if (section.getSteps() == null)
				{
					continue;
				}
				for (Step step : section.getSteps())
				{
					if (!isStepComplete(step))
					{
						return section;
					}
				}
			}
		}
		return null;
	}

	private StepOption findOption(Step step, String optionId)
	{
		if (step.getOptions() == null || optionId == null)
		{
			return null;
		}
		for (StepOption option : step.getOptions())
		{
			if (optionId.equals(option.getId()))
			{
				return option;
			}
		}
		return null;
	}
}
