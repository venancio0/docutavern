package com.docutavern.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chapter (page) in the documentation. (UPDATED)
 */
public class ChapterModel {
    private final String title;
    private final int order;
    private final String sourceElement; // Fully qualified name of the source class/package
    private String story; // From @TavernStory on the chapter element
    private final List<SectionModel> sections = new ArrayList<>();
    private final List<CodeSnippetModel> codeSnippets = new ArrayList<>();
    private final List<NoteModel> notes = new ArrayList<>();
    private final List<ImageModel> images = new ArrayList<>();


    public ChapterModel(String title, int order, String sourceElement) {
        this.title = Objects.requireNonNull(title, "Chapter title cannot be null");
        this.order = order;
        this.sourceElement = Objects.requireNonNull(sourceElement, "Chapter source element cannot be null");
    }

    public String getTitle() { return title; }
    public int getOrder() { return order; }
    public String getSourceElement() { return sourceElement; }
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
    public List<SectionModel> getSections() { return sections; }
    public void addSection(SectionModel section) { this.sections.add(section); }
    public List<CodeSnippetModel> getCodeSnippets() { return codeSnippets; }
    public void addCodeSnippet(CodeSnippetModel snippet) { this.codeSnippets.add(snippet); }
    public List<NoteModel> getNotes() { return notes; }
    public void addNote(NoteModel note) { this.notes.add(note); }
    public List<ImageModel> getImages() { return images; }
    public void addImage(ImageModel image) { this.images.add(image); }

    @Override
    public String toString() {
        return "ChapterModel{" + "title='" + title + '\'' + '}';
    }
}