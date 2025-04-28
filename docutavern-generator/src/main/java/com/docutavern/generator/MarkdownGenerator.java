package com.docutavern.generator;

import com.docutavern.model.DocumentationModel;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface defining the contract for generating Markdown output.
 */
public interface MarkdownGenerator {

    /**
     * Generates the Markdown documentation files.
     *
     * @param model           The populated documentation model.
     * @param outputDirectory The base directory where files should be written.
     * @param filer           The Filer service (optional, might be needed for resource access).
     * @param messager        The Messager service for logging progress or errors.
     * @throws IOException If an error occurs during file writing.
     */
    void generate(DocumentationModel model, Path outputDirectory, Filer filer, Messager messager) throws IOException;
}