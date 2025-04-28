package com.docutavern.generator.impl;

import com.docutavern.generator.MarkdownGenerator;
import com.docutavern.model.*;
import com.docutavern.annotations.NoteType; // Importar enum

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A simple implementation that generates one Markdown file per chapter. (UPDATED)
 */
public class SimpleMarkdownGenerator implements MarkdownGenerator {

    // Removed commonmark fields for simplicity in this update, can be added back if needed

    @Override
    public void generate(DocumentationModel model, Path outputDirectory, Filer filer, Messager messager) throws IOException {
        if (model.getChapters().isEmpty()) {
            log(messager, Diagnostic.Kind.NOTE, "No chapters found, skipping Markdown generation.");
            return;
        }
        Files.createDirectories(outputDirectory);

        List<ChapterModel> sortedChapters = model.getChapters().stream()
                .sorted(Comparator.comparingInt(ChapterModel::getOrder)
                        .thenComparing(ChapterModel::getTitle))
                .collect(Collectors.toList());

        log(messager, Diagnostic.Kind.NOTE, "Generating " + sortedChapters.size() + " chapter file(s)...");

        for (ChapterModel chapter : sortedChapters) {
            String fileName = sanitizeFileName(chapter.getTitle()) + ".md";
            Path filePath = outputDirectory.resolve(fileName);
            log(messager, Diagnostic.Kind.NOTE, "Writing chapter: " + chapter.getTitle() + " to " + filePath);

            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))
            {
                // Chapter Title
                writer.write("# " + escapeMarkdown(chapter.getTitle()) + "\n\n");

                // NOVO: Renderizar Notes do Capítulo
                writeNotes(writer, chapter.getNotes());

                // Chapter Story
                if (hasContent(chapter.getStory())) {
                    writer.write(renderMarkdown(chapter.getStory()) + "\n\n");
                }

                // Chapter Code Snippets (existente)
                writeCodeSnippets(writer, chapter.getCodeSnippets());

                // NOVO: Renderizar Images do Capítulo
                writeImages(writer, chapter.getImages());


                // Sections...
                List<SectionModel> sortedSections = chapter.getSections().stream()
                        .collect(Collectors.toList()); // Adicionar sort se necessário

                for (SectionModel section : sortedSections) {
                    writer.write("## " + escapeMarkdown(section.getTitle()) + "\n\n");

                    // NOVO: Renderizar Notes da Seção
                    writeNotes(writer, section.getNotes());

                    // Section Story
                    if (hasContent(section.getStory())) {
                        writer.write(renderMarkdown(section.getStory()) + "\n\n");
                    }

                    // NOVO: Renderizar Params
                    writeParams(writer, section.getParams());

                    // NOVO: Renderizar Return
                    writeReturn(writer, section.getReturnInfo());

                    // Section Code Snippets (existente)
                    writeCodeSnippets(writer, section.getCodeSnippets());

                    // NOVO: Renderizar Images da Seção
                    writeImages(writer, section.getImages());
                }
            } catch (IOException e) {
                log(messager, Diagnostic.Kind.ERROR, "Failed to write file " + filePath + ": " + e.getMessage());
                throw e;
            }
        }

        generateSidebar(sortedChapters, outputDirectory, messager);
        log(messager, Diagnostic.Kind.NOTE, "Markdown generation finished.");
    }


    // --- MÉTODOS DE RENDERIZAÇÃO EXISTENTES E NOVOS ---

    private void writeCodeSnippets(Writer writer, List<CodeSnippetModel> snippets) throws IOException {
        if (snippets == null || snippets.isEmpty()) return;
        for (CodeSnippetModel snippet : snippets) {
            writer.write("```" + (snippet.getLanguage() != null ? escapeMarkdown(snippet.getLanguage()) : "") + "\n");
            writer.write(snippet.getContent());
            writer.write("\n```\n\n");
        }
    }

    // NOVO: Renderiza parâmetros
    private void writeParams(Writer writer, List<ParamModel> params) throws IOException {
        if (params == null || params.isEmpty()) return;
        writer.write("**Parameters:**\n\n");
        // Usando lista simples, tabela seria mais complexa
        for(ParamModel param : params) {
            writer.write("*   `" + escapeMarkdown(param.getName()) + "`: " + renderMarkdown(param.getDescription()) + "\n");
        }
        writer.write("\n"); // Espaçamento
    }

    // NOVO: Renderiza informação de retorno
    private void writeReturn(Writer writer, ReturnModel returnInfo) throws IOException {
        if (returnInfo == null || !hasContent(returnInfo.getDescription())) return;
        writer.write("**Returns:** " + renderMarkdown(returnInfo.getDescription()) + "\n\n");
    }

    // NOVO: Renderiza notas (admonitions) - versão simples com blockquote
    private void writeNotes(Writer writer, List<NoteModel> notes) throws IOException {
        if (notes == null || notes.isEmpty()) return;
        for(NoteModel note : notes) {
            String prefix = "> **[" + note.getType().name() + "]**"; // Ex: > [WARNING]
            if (hasContent(note.getTitle())) {
                prefix += " " + escapeMarkdown(note.getTitle()); // Ex: > [WARNING] Cuidado!
            }
            writer.write(prefix + "\n"); // Linha do tipo/título
            // Adiciona "> " para cada linha do valor para formar o blockquote
            String valueIndented = Arrays.stream(note.getValue().split("\\r?\\n"))
                    .map(line -> "> " + line)
                    .collect(Collectors.joining("\n"));
            writer.write(valueIndented + "\n\n"); // Conteúdo da nota e espaçamento
        }
    }

    // NOVO: Renderiza imagens
    private void writeImages(Writer writer, List<ImageModel> images) throws IOException {
        if (images == null || images.isEmpty()) return;
        for(ImageModel image : images) {
            String markdownImageTag = "![" + escapeMarkdown(image.getAltText()) + "]" +
                    "(" + escapeMarkdown(image.getPath()) + ")"; // TODO: Tratar path relativo?

            if (hasContent(image.getCaption())) {
                markdownImageTag += " \"" + escapeMarkdown(image.getCaption()) + "\""; // Título/caption do Markdown
            }
            // Adicionar width (requer extensões Markdown ou HTML direto, ignorando por simplicidade)
            // if (hasContent(image.getWidth())) { /* Adicionar lógica de width */ }

            writer.write(markdownImageTag + "\n\n");
        }
    }

    // ... (método generateSidebar existente) ...
    private void generateSidebar(List<ChapterModel> chapters, Path outputDirectory, Messager messager) throws IOException {
        Path sidebarPath = outputDirectory.resolve("_sidebar.md");
        log(messager, Diagnostic.Kind.NOTE, "Generating sidebar: " + sidebarPath);
        try (Writer writer = Files.newBufferedWriter(sidebarPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("*   [Home](README.md)\n");
            for (ChapterModel chapter : chapters) {
                String fileName = sanitizeFileName(chapter.getTitle()) + ".md";
                writer.write("*   [" + escapeMarkdown(chapter.getTitle()) + "](" + fileName + ")\n");
            }
        } catch (IOException e) {
            log(messager, Diagnostic.Kind.ERROR, "Failed to write sidebar file " + sidebarPath + ": " + e.getMessage());
        }
    }

    // ... (métodos utilitários existentes: renderMarkdown, escapeMarkdown, hasContent, sanitizeFileName, log) ...
    private String renderMarkdown(String markdownText) { return markdownText; } // Simplificado
    private String escapeMarkdown(String text) {
        if(text == null) return "";
        return text.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`")
                .replace("[", "\\[").replace("]", "\\]").replace("!", "\\!")
                .replace("<", "<").replace(">", ">"); // Basic escapes
    }
    private boolean hasContent(String s) { return s != null && !s.trim().isEmpty(); }
    private String sanitizeFileName(String name) {
        if (name == null) return "untitled";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\-_\\.]+", "-") // Allow dot for extensions if needed later
                .replaceAll("-+", "-").replaceAll("^-|-$", "");
    }
    private void log(Messager messager, Diagnostic.Kind kind, String message) {
        if (messager != null) { messager.printMessage(kind, "[DocutavernGenerator] " + message); }
        else { System.out.println("[" + kind + "] [DocutavernGenerator] " + message); }
    }
}