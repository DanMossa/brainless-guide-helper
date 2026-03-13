package com.brainlessguidehelper.models;

import java.util.List;
import lombok.Data;

@Data
public class Step {
    private int id;
    private String instructions;
    private Integer expectedGp;
    private Integer timeMinutes;
    private List<Requirement> prerequisites;
    private List<Requirement> completionConditions;
    private List<StepOption> options;
}
