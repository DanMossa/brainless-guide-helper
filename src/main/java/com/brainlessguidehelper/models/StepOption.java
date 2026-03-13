package com.brainlessguidehelper.models;

import java.util.List;
import lombok.Data;

@Data
public class StepOption {
    private String id;
    private String label;
    private List<Requirement> completionConditions;
}
