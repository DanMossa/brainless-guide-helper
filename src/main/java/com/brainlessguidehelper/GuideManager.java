package com.brainlessguidehelper;

import com.brainlessguidehelper.models.Guide;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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
