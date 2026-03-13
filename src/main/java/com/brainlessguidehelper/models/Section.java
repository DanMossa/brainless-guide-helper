package com.brainlessguidehelper.models;

import java.util.List;
import lombok.Data;

@Data
public class Section {
    private String id;
    private String title;
    private List<Step> steps;
}
