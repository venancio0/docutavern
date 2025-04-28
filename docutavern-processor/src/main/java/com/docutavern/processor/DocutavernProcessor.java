package com.docutavern.processor;

// imports... (Garantir que os novos modelos e anotações estão importados)
import com.docutavern.annotations.*;
import com.docutavern.generator.MarkdownGenerator;
import com.docutavern.generator.impl.SimpleMarkdownGenerator;
import com.docutavern.model.*;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror; // Necessário para verificar método void
import javax.lang.model.type.TypeKind;   // Necessário para verificar método void
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
// ... outros imports ...
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files; // Adicionado para createDirectories


@AutoService(Processor.class)
@SupportedAnnotationTypes({ // ALTERADO: Adicionar novas anotações
        "com.docutavern.annotations.TavernChapter",
        "com.docutavern.annotations.TavernSection",
        "com.docutavern.annotations.TavernStory",
        "com.docutavern.annotations.TavernCodeSnippet",
        "com.docutavern.annotations.TavernReturn",
        "com.docutavern.annotations.TavernParam",
        "com.docutavern.annotations.TavernParams", // Container
        "com.docutavern.annotations.TavernNote",
        "com.docutavern.annotations.TavernNotes",  // Container
        "com.docutavern.annotations.TavernImage",
        "com.docutavern.annotations.TavernImages" // Container
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions({"docutavern.outputDir"})
public class DocutavernProcessor extends AbstractProcessor {

    // ... (campos existentes: messager, filer, elementUtils, typeUtils, chaptersByQualifiedName, generationTriggered, markdownGenerator) ...
    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private final Map<String, ChapterModel> chaptersByQualifiedName = new LinkedHashMap<>();
    private boolean generationTriggered = false;
    private final MarkdownGenerator markdownGenerator = new SimpleMarkdownGenerator();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager.printMessage(Diagnostic.Kind.NOTE, "Docutavern Processor Initialized (Supports Params/Return/Note/Image).");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!generationTriggered) {
                generateMarkdownOutput();
                generationTriggered = true;
            }
            return true;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "Docutavern Processing Round...");

        // Processa elementos raiz
        for (Element rootElement : roundEnv.getRootElements()) {
            TavernChapter chapterAnnotation = rootElement.getAnnotation(TavernChapter.class);
            if (chapterAnnotation != null && (rootElement.getKind() == ElementKind.CLASS || rootElement.getKind() == ElementKind.PACKAGE)) {
                processChapterElement(rootElement, chapterAnnotation);
            }

            if (rootElement.getKind() == ElementKind.CLASS || rootElement.getKind() == ElementKind.INTERFACE) {
                String parentQualifiedName = getQualifiedName(rootElement);
                ChapterModel parentChapter = chaptersByQualifiedName.get(parentQualifiedName);
                if (parentChapter != null) {
                    for (Element enclosedElement : rootElement.getEnclosedElements()) {
                        processEnclosedElement(enclosedElement, parentChapter);
                    }
                }
            }
        }

        return false;
    }

    private void processChapterElement(Element chapterElement, TavernChapter annotation) {
        String qualifiedName = getQualifiedName(chapterElement);
        ChapterModel chapterModel = chaptersByQualifiedName.computeIfAbsent(qualifiedName, k ->
                new ChapterModel(annotation.title(), annotation.order(), qualifiedName)
        );
        messager.printMessage(Diagnostic.Kind.NOTE, "Processing Chapter: " + chapterModel.getTitle());

        // Story (existente)
        TavernStory story = chapterElement.getAnnotation(TavernStory.class);
        if (story != null && chapterModel.getStory() == null) { chapterModel.setStory(story.description()); }

        // Code Snippet (existente)
        TavernCodeSnippet snippet = chapterElement.getAnnotation(TavernCodeSnippet.class);
        if (snippet != null && chapterModel.getCodeSnippets().stream().noneMatch(s -> modelContentMatchesAnnotation(s, snippet))) {
            processCodeSnippet(snippet, chapterElement, chapterModel, null);
        }

        processNotes(chapterElement, chapterModel, null);

        processImages(chapterElement, chapterModel, null);
    }

    private void processEnclosedElement(Element enclosedElement, ChapterModel parentChapter) {
        TavernSection sectionAnnotation = enclosedElement.getAnnotation(TavernSection.class);
        if (sectionAnnotation != null && (enclosedElement.getKind().isField() || enclosedElement.getKind() == ElementKind.METHOD || enclosedElement.getKind() == ElementKind.CONSTRUCTOR)) {

            SectionModel sectionModel = findOrCreateSectionModel(enclosedElement, sectionAnnotation, parentChapter);

            // Story (existente)
            TavernStory story = enclosedElement.getAnnotation(TavernStory.class);
            if (story != null && sectionModel.getStory() == null) { sectionModel.setStory(story.description()); }

            // Code Snippet (existente)
            TavernCodeSnippet snippet = enclosedElement.getAnnotation(TavernCodeSnippet.class);
            if (snippet != null && sectionModel.getCodeSnippets().stream().noneMatch(s -> modelContentMatchesAnnotation(s, snippet))) {
                processCodeSnippet(snippet, enclosedElement, null, sectionModel);
            }

            if(enclosedElement.getKind() == ElementKind.METHOD || enclosedElement.getKind() == ElementKind.CONSTRUCTOR){
                // Processar @TavernReturn (só se for método e não for void)
                processReturn(enclosedElement, sectionModel);

                // Processar @TavernParam(s)
                processParams(enclosedElement, sectionModel);
            }

            // Processar @TavernNote(s) na seção
            processNotes(enclosedElement, null, sectionModel);

            // Processar @TavernImage(s) na seção
            processImages(enclosedElement, null, sectionModel);
        }
    }

    /** Helper to find or create a section model reliably */
    private SectionModel findOrCreateSectionModel(Element element, TavernSection annotation, ChapterModel parentChapter) {
        String sectionSourceElementName = element.getSimpleName().toString(); // Or more unique identifier if needed
        return parentChapter.getSections().stream()
                .filter(s -> s.getSourceElement().equals(sectionSourceElementName))
                .findFirst()
                .orElseGet(() -> {
                    SectionModel newSection = new SectionModel(annotation.title(), sectionSourceElementName);
                    parentChapter.addSection(newSection);
                    messager.printMessage(Diagnostic.Kind.NOTE, "Added Section: '" + newSection.getTitle() + "' to Chapter '" + parentChapter.getTitle() + "'");
                    return newSection;
                });
    }

    /** Checks if a CodeSnippetModel content matches the annotation to avoid duplicates */
    private boolean modelContentMatchesAnnotation(CodeSnippetModel model, TavernCodeSnippet annotation) {
        boolean valueMatch = model.getContent() != null && model.getContent().equals(annotation.value());
        boolean sourceMatch = model.getSourcePath() != null && model.getSourcePath().equals(annotation.source());
        // Be careful with the simplified source handling in processCodeSnippet
        boolean placeholderMatch = model.getContent() != null && annotation.source() != null && model.getContent().contains(annotation.source());
        return (valueMatch && annotation.source().isEmpty()) || (sourceMatch && annotation.value().isEmpty()) || (placeholderMatch && annotation.value().isEmpty() && !annotation.source().isEmpty());
    }

    private void processReturn(Element methodElement, SectionModel sectionModel) {
        TavernReturn returnAnnotation = methodElement.getAnnotation(TavernReturn.class);
        if (returnAnnotation != null) {
            // Verifica se o método realmente retorna algo (não é void)
            ExecutableElement execElement = (ExecutableElement) methodElement; // Cast seguro, pois é METHOD ou CONSTRUCTOR
            if (execElement.getReturnType().getKind() == TypeKind.VOID) {
                messager.printMessage(Diagnostic.Kind.WARNING, "@TavernReturn used on a void method.", methodElement);
                return; // Ignora para métodos void
            }

            if (sectionModel.getReturnInfo() == null) { // Adiciona só se não existir
                ReturnModel returnModel = new ReturnModel(returnAnnotation.description());
                sectionModel.setReturnInfo(returnModel);
            }
        }
    }

    private void processParams(Element executableElement, SectionModel sectionModel) {
        List<TavernParam> paramsAnnotations = new ArrayList<>();
        // Get repeatable annotations
        TavernParams paramsContainer = executableElement.getAnnotation(TavernParams.class);
        if (paramsContainer != null) {
            paramsAnnotations.addAll(Arrays.asList(paramsContainer.value()));
        } else {
            // Get single annotation if container not present
            TavernParam singleParam = executableElement.getAnnotation(TavernParam.class);
            if (singleParam != null) {
                paramsAnnotations.add(singleParam);
            }
        }


        if (!paramsAnnotations.isEmpty()) {
            // Validação básica: comparar com parâmetros reais do método
            ExecutableElement execElement = (ExecutableElement) executableElement;
            List<? extends VariableElement> actualParams = execElement.getParameters();
            Set<String> actualParamNames = actualParams.stream().map(p -> p.getSimpleName().toString()).collect(Collectors.toSet());


            for (TavernParam paramAnnotation : paramsAnnotations) {
                if (!actualParamNames.contains(paramAnnotation.name())) {
                    messager.printMessage(Diagnostic.Kind.WARNING, "@TavernParam references parameter '" + paramAnnotation.name() + "' which does not exist on the method.", executableElement);
                    continue; // Pula parâmetro inválido
                }

                // Evita duplicatas (baseado no nome do parâmetro)
                if (sectionModel.getParams().stream().noneMatch(p -> p.getName().equals(paramAnnotation.name()))) {
                    ParamModel paramModel = new ParamModel(paramAnnotation.name(), paramAnnotation.description());
                    sectionModel.addParam(paramModel);
                }
            }
        }
    }

    private void processNotes(Element annotatedElement, ChapterModel chapter, SectionModel section) {
        List<TavernNote> notesAnnotations = new ArrayList<>();
        TavernNotes notesContainer = annotatedElement.getAnnotation(TavernNotes.class);
        if (notesContainer != null) {
            notesAnnotations.addAll(Arrays.asList(notesContainer.value()));
        } else {
            TavernNote singleNote = annotatedElement.getAnnotation(TavernNote.class);
            if (singleNote != null) {
                notesAnnotations.add(singleNote);
            }
        }


        for (TavernNote noteAnnotation : notesAnnotations) {
            NoteModel noteModel = new NoteModel(noteAnnotation.type(), noteAnnotation.title(), noteAnnotation.value());
            // Adiciona ao capítulo OU seção, evitando duplicatas simplificadas
            if (section != null) {
                if (section.getNotes().stream().noneMatch(n -> n.getValue().equals(noteModel.getValue()) && n.getType() == noteModel.getType())) {
                    section.addNote(noteModel);
                }
            } else if (chapter != null) {
                if (chapter.getNotes().stream().noneMatch(n -> n.getValue().equals(noteModel.getValue()) && n.getType() == noteModel.getType())) {
                    chapter.addNote(noteModel);
                }
            }
        }
    }

    private void processImages(Element annotatedElement, ChapterModel chapter, SectionModel section) {
        List<TavernImage> imagesAnnotations = new ArrayList<>();
        TavernImages imagesContainer = annotatedElement.getAnnotation(TavernImages.class);
        if (imagesContainer != null) {
            imagesAnnotations.addAll(Arrays.asList(imagesContainer.value()));
        } else {
            TavernImage singleImage = annotatedElement.getAnnotation(TavernImage.class);
            if (singleImage != null) {
                imagesAnnotations.add(singleImage);
            }
        }

        for (TavernImage imageAnnotation : imagesAnnotations) {
            ImageModel imageModel = new ImageModel(
                    imageAnnotation.path(),
                    imageAnnotation.caption(),
                    imageAnnotation.altText(),
                    imageAnnotation.width());
            // Adiciona ao capítulo OU seção, evitando duplicatas baseadas no path
            if (section != null) {
                if (section.getImages().stream().noneMatch(i -> i.getPath().equals(imageModel.getPath()))) {
                    section.addImage(imageModel);
                }
            } else if (chapter != null) {
                if (chapter.getImages().stream().noneMatch(i -> i.getPath().equals(imageModel.getPath()))) {
                    chapter.addImage(imageModel);
                }
            }
        }
    }


    // ... (método processCodeSnippet existente) ...
    private void processCodeSnippet(TavernCodeSnippet annotation, Element element, ChapterModel chapter, SectionModel section) {
        String content = annotation.value();
        String source = annotation.source();
        String lang = annotation.lang();
        String sourceElementName = getQualifiedName(element);

        if (!content.isEmpty() && !source.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Use either 'value' or 'source' for @TavernCodeSnippet, not both.", element);
            return;
        }
        if (content.isEmpty() && source.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Either 'value' or 'source' must be provided for @TavernCodeSnippet.", element);
            return;
        }

        if (!source.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.WARNING, "'source' attribute processing is simplified. Using placeholder.", element);
            content = "// Code from: " + source + " (Content not loaded in this version)";
        }

        CodeSnippetModel snippetModel = new CodeSnippetModel(lang, content, source.isEmpty() ? null : source, sourceElementName);

        if (section != null) { section.addCodeSnippet(snippetModel); }
        else if (chapter != null) { chapter.addCodeSnippet(snippetModel); }
        else { messager.printMessage(Diagnostic.Kind.WARNING, "Could not associate code snippet with Chapter or Section.", element); }
    }

    private void generateMarkdownOutput() {
        if (chaptersByQualifiedName.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "No @TavernChapter processed. Skipping Markdown generation.");
            return;
        }

        DocumentationModel finalModel = new DocumentationModel();
        chaptersByQualifiedName.values().stream()
                .sorted(Comparator.comparingInt(ChapterModel::getOrder)
                        .thenComparing(ChapterModel::getTitle))
                .forEach(finalModel::addChapter);

        messager.printMessage(Diagnostic.Kind.NOTE, "Docutavern triggering Markdown generation...");
        try {
            String outputDirOption = processingEnv.getOptions().get("docutavern.outputDir");
            Path outputDir;
            if (outputDirOption != null && !outputDirOption.isEmpty()) {
                outputDir = Paths.get(outputDirOption).toAbsolutePath();
            } else {
                Path baseDir = Paths.get("").toAbsolutePath();
                Path targetDir = baseDir.resolve("target");
                Path buildDir = baseDir.resolve("build");
                Path defaultDir = Files.exists(targetDir) ? targetDir : (Files.exists(buildDir) ? buildDir : baseDir);
                outputDir = defaultDir.resolve("docutavern-output");
                messager.printMessage(Diagnostic.Kind.WARNING, "docutavern.outputDir compiler option not set. Defaulting to: " + outputDir);
            }

            Files.createDirectories(outputDir); // Ensure the directory exists

            markdownGenerator.generate(finalModel, outputDir, filer, messager);
            messager.printMessage(Diagnostic.Kind.NOTE, "Docutavern Markdown generation successful. Output: " + outputDir);

        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "IO Error generating Docutavern Markdown: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error during Markdown generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getQualifiedName(Element element) {
        if (element instanceof TypeElement) { return ((TypeElement) element).getQualifiedName().toString(); }
        else if (element instanceof PackageElement) { return ((PackageElement) element).getQualifiedName().toString(); }
        else if (element instanceof ExecutableElement) { return element.getEnclosingElement().getSimpleName().toString() + "#" + element.getSimpleName().toString(); }
        else if (element instanceof VariableElement && element.getKind().isField()) { return element.getEnclosingElement().getSimpleName().toString() + "#" + element.getSimpleName().toString(); }
        return element.getSimpleName().toString();
    }
}