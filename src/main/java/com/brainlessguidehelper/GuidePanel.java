package com.brainlessguidehelper;

import com.brainlessguidehelper.models.Chapter;
import com.brainlessguidehelper.models.Guide;
import com.brainlessguidehelper.models.Requirement;
import com.brainlessguidehelper.models.Section;
import com.brainlessguidehelper.models.Step;
import com.brainlessguidehelper.models.StepOption;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class GuidePanel extends PluginPanel
{
	private static final Color COLOR_MET = new Color(0, 190, 0);
	private static final Color COLOR_NOT_MET = new Color(220, 50, 50);
	private static final Color COLOR_COMPLETE = new Color(100, 100, 100);

	private final GuideProgressManager progressManager;
	private final GuideManager guideManager;
	private final PlayerStateTracker playerStateTracker;

	private Step viewedStep;
	private Step cachedCurrentStep;
	private boolean cachedViewedStepComplete;
	private final Map<Requirement, Boolean> cachedRequirementStatuses = new HashMap<>();

	public GuidePanel(GuideProgressManager progressManager, GuideManager guideManager, PlayerStateTracker playerStateTracker)
	{
		super(false);
		this.progressManager = progressManager;
		this.guideManager = guideManager;
		this.playerStateTracker = playerStateTracker;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
	}

	public void rebuild()
	{
		SwingUtilities.invokeLater(() ->
		{
			removeAll();

			Guide guide = guideManager.getGuide();
			if (guide == null || guide.getChapters() == null)
			{
				add(createMessageLabel("Guide not loaded."), BorderLayout.CENTER);
				revalidate();
				repaint();
				return;
			}

			if (viewedStep == null)
			{
				viewedStep = cachedCurrentStep;
			}

			if (viewedStep == null)
			{
				add(createMessageLabel("All steps complete!"), BorderLayout.CENTER);
				revalidate();
				repaint();
				return;
			}

			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
			contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

			// Header: Chapter / Section
			Chapter chapter = findChapterForStep(guide, viewedStep);
			Section section = findSectionForStep(guide, viewedStep);
			contentPanel.add(buildHeaderPanel(chapter, section));

			// Step instructions
			contentPanel.add(buildStepPanel(viewedStep));

			// Requirements
			if (viewedStep.getPrerequisites() != null && !viewedStep.getPrerequisites().isEmpty())
			{
				contentPanel.add(buildRequirementsPanel("Prerequisites", viewedStep.getPrerequisites()));
			}

			if (viewedStep.getCompletionConditions() != null && !viewedStep.getCompletionConditions().isEmpty())
			{
				contentPanel.add(buildRequirementsPanel("Completion Conditions", viewedStep.getCompletionConditions()));
			}

			// Step Options
			if (viewedStep.getOptions() != null && !viewedStep.getOptions().isEmpty())
			{
				contentPanel.add(buildOptionsPanel(viewedStep));
			}

			// Manual completion
			contentPanel.add(buildManualCompletionPanel(viewedStep));

			// Navigation
			contentPanel.add(buildNavigationPanel(guide, viewedStep));

			JScrollPane scrollPane = new JScrollPane(contentPanel);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
			add(scrollPane, BorderLayout.CENTER);

			revalidate();
			repaint();
		});
	}

	public void goToCurrentStep()
	{
		viewedStep = cachedCurrentStep;
		rebuild();
	}

	/**
	 * Pre-compute data that requires the client thread.
	 * Must be called from the client thread before rebuild().
	 */
	public void updateDataOnClientThread()
	{
		cachedCurrentStep = progressManager.getCurrentStep();

		Step stepToView = viewedStep != null ? viewedStep : cachedCurrentStep;
		cachedRequirementStatuses.clear();

		if (stepToView != null)
		{
			cachedViewedStepComplete = progressManager.isStepComplete(stepToView);
			cacheRequirements(stepToView.getPrerequisites());
			cacheRequirements(stepToView.getCompletionConditions());
		}
		else
		{
			cachedViewedStepComplete = false;
		}
	}

	private void cacheRequirements(List<Requirement> requirements)
	{
		if (requirements == null)
		{
			return;
		}
		for (Requirement req : requirements)
		{
			cachedRequirementStatuses.put(req, playerStateTracker.isRequirementMet(req));
		}
	}

	private JPanel buildHeaderPanel(Chapter chapter, Section section)
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(8, 8, 8, 8)
		));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
		panel.setAlignmentX(LEFT_ALIGNMENT);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		String chapterText = chapter != null ? chapter.getTitle() : "Unknown Chapter";
		JLabel chapterLabel = new JLabel(chapterText);
		chapterLabel.setFont(FontManager.getRunescapeBoldFont());
		chapterLabel.setForeground(Color.WHITE);
		panel.add(chapterLabel, gbc);

		gbc.gridy = 1;
		gbc.insets = new Insets(2, 0, 0, 0);
		String sectionText = section != null ? section.getTitle() : "Unknown Section";
		JLabel sectionLabel = new JLabel(sectionText);
		sectionLabel.setFont(FontManager.getRunescapeSmallFont());
		sectionLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		panel.add(sectionLabel, gbc);

		return panel;
	}

	private JPanel buildStepPanel(Step step)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(6, 0, 0, 0),
			new EmptyBorder(8, 8, 8, 8)
		));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		panel.setAlignmentX(LEFT_ALIGNMENT);

		boolean complete = cachedViewedStepComplete;

		JLabel stepTitle = new JLabel("Step " + step.getId());
		stepTitle.setFont(FontManager.getRunescapeBoldFont());
		stepTitle.setForeground(complete ? COLOR_COMPLETE : Color.WHITE);
		panel.add(stepTitle, BorderLayout.NORTH);

		String htmlInstructions = "<html><body style='width:200px;'>" + step.getInstructions() + "</body></html>";
		JLabel instructionsLabel = new JLabel(htmlInstructions);
		instructionsLabel.setFont(FontManager.getRunescapeFont());
		instructionsLabel.setForeground(complete ? COLOR_COMPLETE : ColorScheme.LIGHT_GRAY_COLOR);
		instructionsLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
		panel.add(instructionsLabel, BorderLayout.CENTER);

		if (complete)
		{
			JLabel completeLabel = new JLabel("✓ Complete");
			completeLabel.setFont(FontManager.getRunescapeSmallFont());
			completeLabel.setForeground(COLOR_MET);
			completeLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
			panel.add(completeLabel, BorderLayout.SOUTH);
		}

		return panel;
	}

	private JPanel buildRequirementsPanel(String title, List<Requirement> requirements)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(6, 0, 0, 0),
			new EmptyBorder(8, 8, 8, 8)
		));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		panel.setAlignmentX(LEFT_ALIGNMENT);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(titleLabel);

		for (Requirement req : requirements)
		{
			boolean met = cachedRequirementStatuses.getOrDefault(req, false);
			JLabel reqLabel = new JLabel(formatRequirement(req));
			reqLabel.setFont(FontManager.getRunescapeSmallFont());
			reqLabel.setForeground(met ? COLOR_MET : COLOR_NOT_MET);
			reqLabel.setBorder(new EmptyBorder(2, 8, 0, 0));
			reqLabel.setAlignmentX(LEFT_ALIGNMENT);
			panel.add(reqLabel);
		}

		return panel;
	}

	private JPanel buildOptionsPanel(Step step)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(6, 0, 0, 0),
			new EmptyBorder(8, 8, 8, 8)
		));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		panel.setAlignmentX(LEFT_ALIGNMENT);

		JLabel titleLabel = new JLabel("Options");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		titleLabel.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(titleLabel);

		ButtonGroup group = new ButtonGroup();
		String selectedOptionId = progressManager.getSelectedOption(step.getId());

		for (StepOption option : step.getOptions())
		{
			JRadioButton radioButton = new JRadioButton(option.getLabel());
			radioButton.setFont(FontManager.getRunescapeSmallFont());
			radioButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			radioButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			radioButton.setAlignmentX(LEFT_ALIGNMENT);
			radioButton.setSelected(option.getId().equals(selectedOptionId));
			radioButton.addActionListener(e ->
			{
				progressManager.setSelectedOption(step.getId(), option.getId());
				rebuild();
			});
			group.add(radioButton);
			panel.add(radioButton);
		}

		return panel;
	}

	private JPanel buildManualCompletionPanel(Step step)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(6, 0, 0, 0),
			new EmptyBorder(8, 8, 8, 8)
		));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		panel.setAlignmentX(LEFT_ALIGNMENT);

		JCheckBox checkbox = new JCheckBox("Mark as complete");
		checkbox.setFont(FontManager.getRunescapeSmallFont());
		checkbox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		checkbox.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		checkbox.setSelected(progressManager.isManuallyCompleted(step.getId()));
		checkbox.addActionListener(e ->
		{
			progressManager.setManuallyCompleted(step.getId(), checkbox.isSelected());
			rebuild();
		});

		panel.add(checkbox, BorderLayout.WEST);
		return panel;
	}

	private JPanel buildNavigationPanel(Guide guide, Step currentStep)
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(new EmptyBorder(10, 0, 0, 0));
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		panel.setAlignmentX(LEFT_ALIGNMENT);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 2, 0, 2);

		Step prevStep = findAdjacentStep(guide, currentStep, -1);
		Step nextStep = findAdjacentStep(guide, currentStep, 1);

		gbc.gridx = 0;
		gbc.weightx = 0;
		JButton prevButton = new JButton("<");
		prevButton.setFont(FontManager.getRunescapeSmallFont());
		prevButton.setEnabled(prevStep != null);
		prevButton.addActionListener(e ->
		{
			viewedStep = prevStep;
			rebuild();
		});
		panel.add(prevButton, gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		JButton currentButton = new JButton("Current");
		currentButton.setFont(FontManager.getRunescapeSmallFont());
		currentButton.addActionListener(e -> goToCurrentStep());
		panel.add(currentButton, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		JButton nextButton = new JButton(">");
		nextButton.setFont(FontManager.getRunescapeSmallFont());
		nextButton.setEnabled(nextStep != null);
		nextButton.addActionListener(e ->
		{
			viewedStep = nextStep;
			rebuild();
		});
		panel.add(nextButton, gbc);

		return panel;
	}

	private String formatRequirement(Requirement req)
	{
		boolean met = cachedRequirementStatuses.getOrDefault(req, false);
		String icon = met ? "✅" : "x";

		switch (req.getType())
		{
			case SKILL:
				return icon + " " + req.getId() + " Level " + req.getLevel();
			case QUEST:
				return icon + " Quest: " + req.getId() + " (" + req.getStatus() + ")";
			case ITEM:
				String loc = req.getLocation() != null ? " [" + req.getLocation() + "]" : "";
				int amt = req.getAmount() != null ? req.getAmount() : 1;
				return icon + " Item: " + req.getId() + " x" + amt + loc;
			case DIARY:
				return icon + " Diary: " + req.getId() + " (" + req.getStatus() + ")";
			case VARBIT:
			case VARP:
				return icon + " " + req.getType() + ": " + req.getId();
			default:
				return icon + " " + req.getType() + ": " + req.getId();
		}
	}

	private JLabel createMessageLabel(String message)
	{
		JLabel label = new JLabel(message);
		label.setFont(FontManager.getRunescapeBoldFont());
		label.setForeground(Color.WHITE);
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}

	private Chapter findChapterForStep(Guide guide, Step target)
	{
		for (Chapter chapter : guide.getChapters())
		{
			if (chapter.getSections() == null) continue;
			for (Section section : chapter.getSections())
			{
				if (section.getSteps() == null) continue;
				for (Step step : section.getSteps())
				{
					if (step.getId() == target.getId())
					{
						return chapter;
					}
				}
			}
		}
		return null;
	}

	private Section findSectionForStep(Guide guide, Step target)
	{
		for (Chapter chapter : guide.getChapters())
		{
			if (chapter.getSections() == null) continue;
			for (Section section : chapter.getSections())
			{
				if (section.getSteps() == null) continue;
				for (Step step : section.getSteps())
				{
					if (step.getId() == target.getId())
					{
						return section;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Find the step at offset positions from the target step in guide order.
	 * offset = -1 for previous, +1 for next.
	 */
	private Step findAdjacentStep(Guide guide, Step target, int offset)
	{
		Step previous = null;
		boolean returnNext = false;

		for (Chapter chapter : guide.getChapters())
		{
			if (chapter.getSections() == null) continue;
			for (Section section : chapter.getSections())
			{
				if (section.getSteps() == null) continue;
				for (Step step : section.getSteps())
				{
					if (returnNext)
					{
						return step;
					}
					if (step.getId() == target.getId())
					{
						if (offset < 0)
						{
							return previous;
						}
						else
						{
							returnNext = true;
						}
					}
					previous = step;
				}
			}
		}
		return null;
	}
}
