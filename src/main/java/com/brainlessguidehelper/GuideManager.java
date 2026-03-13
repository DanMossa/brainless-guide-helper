package com.brainlessguidehelper;

import com.brainlessguidehelper.models.Guide;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class GuideManager {
    private final Gson gson;

    @Getter
    private Guide guide;

    @Inject
    public GuideManager(Gson gson) {
        this.gson = gson;
    }

    public void loadGuide() {
        try (InputStream is = getClass().getResourceAsStream("/com/brainlessguidehelper/chapter1.json")) {
            if (is == null) {
                log.error("Guide file not found");
                return;
            }
            guide = gson.fromJson(new InputStreamReader(is), Guide.class);
            if (guide != null && guide.getChapters() != null) {
                log.info("Guide loaded: {} chapters", guide.getChapters().size());
            }
        } catch (Exception e) {
            log.error("Error loading guide", e);
        }
    }
}
