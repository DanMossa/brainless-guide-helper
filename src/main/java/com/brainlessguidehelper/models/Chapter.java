package com.brainlessguidehelper.models;

import java.util.List;
import lombok.Data;

@Data
public class Chapter {
    private int id;
    private String title;
    private List<Section> sections;
}
