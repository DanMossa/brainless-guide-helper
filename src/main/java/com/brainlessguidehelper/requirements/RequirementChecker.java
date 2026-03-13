package com.brainlessguidehelper.requirements;

import com.brainlessguidehelper.models.Requirement;
import net.runelite.api.Client;

/**
 * Interface for checking whether a requirement is met based on the current player state.
 */
public interface RequirementChecker
{
	boolean isMet(Requirement requirement, Client client);

	Requirement.RequirementType getType();
}
