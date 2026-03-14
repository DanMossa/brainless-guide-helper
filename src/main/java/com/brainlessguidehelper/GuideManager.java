package com.brainlessguidehelper;

import com.brainlessguidehelper.models.Guide;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.brainlessguidehelper.models.Chapter;
import com.brainlessguidehelper.models.Section;

@Slf4j
@Singleton
public class GuideManager {
    private final Gson gson;

    @Getter
    private Guide guide;

    public GuideManager() {
        this.gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    }

    public void loadGuide() {
        guide = new Guide();
        List<Chapter> chapters = new ArrayList<>();
        guide.setChapters(chapters);

        try {
            int chapterIndex = 1;
            while (true) {
                String chapterDir = "/com/brainlessguidehelper/chapter" + chapterIndex;
                InputStream chapterIs = getClass().getResourceAsStream(chapterDir + "/metadata.json");
                if (chapterIs == null) {
                    break; // No more chapters
                }
                
                Chapter chapter = gson.fromJson(new InputStreamReader(chapterIs), Chapter.class);
                List<Section> sections = new ArrayList<>();
                chapter.setSections(sections);
                
                int sectionIndex = 1;
                while (true) {
                    InputStream sectionIs = getClass().getResourceAsStream(chapterDir + "/section" + sectionIndex + ".json");
                    if (sectionIs == null) {
                        break; // No more sections for this chapter
                    }
                    
                    Section section = gson.fromJson(new InputStreamReader(sectionIs), Section.class);
                    sections.add(section);
                    sectionIndex++;
                }
                
                chapters.add(chapter);
                chapterIndex++;
            }
            
            if (!chapters.isEmpty()) {
                log.info("Guide loaded: {} chapters", guide.getChapters().size());
            } else {
                log.warn("No chapters found for the guide.");
            }
        } catch (Exception e) {
            log.error("Error loading guide", e);
        }
    }
}
