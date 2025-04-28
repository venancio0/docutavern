package com.docutavern.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Root model object holding the entire documentation structure collected by the processor.
 */
public class DocumentationModel {

    private final List<ChapterModel> chapters = new ArrayList<>();

    public void addChapter(ChapterModel chapter) {
        this.chapters.add(chapter);
    }

    public List<ChapterModel> getChapters() {
        return chapters;
    }
}