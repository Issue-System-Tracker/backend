package com.nsu.issue_tracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Каталог для загруженных изображений описания задач (локальный диск). */
@ConfigurationProperties(prefix = "app.upload")
@Getter
@Setter
public class FileUploadProperties {

    /**
     * Относительно рабочей директории процесса или абсолютный путь.
     * Docker: смонтируй том на этот путь при необходимости персистентности.
     */
    private String dir = "data/uploads";
}
