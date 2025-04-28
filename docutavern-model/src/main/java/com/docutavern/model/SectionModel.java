package com.docutavern.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a section within a chapter. (UPDATED)
 */
public class SectionModel {
    private final String title;
    private final String sourceElement; // Name of the method/field this section comes from
    private String story; // From @TavernStory on the section element
    private final List<CodeSnippetModel> codeSnippets = new ArrayList<>();
    private ReturnModel returnInfo; // Can be null if void or not a method section
    private final List<ParamModel> params = new ArrayList<>();
    private final List<NoteModel> notes = new ArrayList<>();
    private final List<ImageModel> images = new ArrayList<>();

    public SectionModel(String title, String sourceElement) {
        this.title = Objects.requireNonNull(title, "Section title cannot be null");
        this.sourceElement = Objects.requireNonNull(sourceElement, "Section source element cannot be null");
    }

    public String getTitle() { return title; }
    public String getSourceElement() { return sourceElement; }
    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }
    public List<CodeSnippetModel> getCodeSnippets() { return codeSnippets; }
    public void addCodeSnippet(CodeSnippetModel snippet) { this.codeSnippets.add(snippet); }
    public ReturnModel getReturnInfo() { return returnInfo; }
    public void setReturnInfo(ReturnModel returnInfo) { this.returnInfo = returnInfo; }
    public List<ParamModel> getParams() { return params; }
    public void addParam(ParamModel param) { this.params.add(param); }
    public List<NoteModel> getNotes() { return notes; }
    public void addNote(NoteModel note) { this.notes.add(note); }
    public List<ImageModel> getImages() { return images; }
    public void addImage(ImageModel image) { this.images.add(image); }

    @Override
    public String toString() {
        return "SectionModel{" + "title='" + title + '\'' + '}';
    }
}