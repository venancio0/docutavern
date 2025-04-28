package com.docutavern.cli;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.*; // Import Files and Paths
import java.util.concurrent.Executors;

public class DocutavernCli {

    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_DOCS_SUBDIR = "docutavern-docs"; // Nome padrão da pasta final
    private static final String DEFAULT_BUILD_DIR = "target"; // Ex: "target" ou "build"

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        Path explicitDir = null;
        Path workingDir = null; // Diretório de onde o usuário rodou o comando
        boolean serveCommand = false;

        // --- Parsing de Argumentos ---
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("serve".equals(arg)) {
                serveCommand = true;
            } else if ("--port".equals(arg) && i + 1 < args.length) {
                try { port = Integer.parseInt(args[++i]); }
                catch (NumberFormatException e) { /* erro tratado abaixo */ }
            } else if ("--dir".equals(arg) && i + 1 < args.length) {
                try { explicitDir = Paths.get(args[++i]).toAbsolutePath(); }
                catch (InvalidPathException e) { /* erro tratado abaixo */ }
            } else if ("--cwd".equals(arg) && i + 1 < args.length) { // Recebe CWD do script
                try { workingDir = Paths.get(args[++i]).toAbsolutePath(); }
                catch (InvalidPathException e) { /* erro tratado abaixo */ }
            }
        }
        // --- Fim Parsing ---

        if (!serveCommand) {
            printUsage();
            System.exit(0); // Não é um erro não passar comando
        }

        // Se workingDir não foi passado pelo script (fallback), usa o CWD do processo Java
        if (workingDir == null) {
            workingDir = Paths.get("").toAbsolutePath();
            logInfo("Working directory not passed via --cwd, using Java process CWD: " + workingDir);
        }

        Path docsDirToServe = null;

        // Determina qual diretório servir
        if (explicitDir != null) {
            logInfo("Using explicitly specified directory: " + explicitDir);
            docsDirToServe = explicitDir;
        } else {
            logInfo("Attempting to find default documentation directory relative to: " + workingDir);
            Path projectRoot = findProjectRoot(workingDir);
            if (projectRoot == null) {
                logError("Could not determine project root (looking for pom.xml or build.gradle) starting from: " + workingDir);
                logError("Please specify the documentation directory explicitly using --dir <path>");
                System.exit(1);
            }
            logInfo("Project root found at: " + projectRoot);
            // Constrói o caminho padrão: [PROJECT_ROOT]/target/docutavern-docs (ou build/)
            // Tenta 'target' primeiro, depois 'build'
            Path defaultPathTarget = projectRoot.resolve(DEFAULT_BUILD_DIR).resolve(DEFAULT_DOCS_SUBDIR);
            Path defaultPathBuild = projectRoot.resolve("build").resolve(DEFAULT_DOCS_SUBDIR); // Comum no Gradle

            if (Files.isDirectory(defaultPathTarget)) {
                docsDirToServe = defaultPathTarget;
                logInfo("Found default directory: " + docsDirToServe);
            } else if (Files.isDirectory(defaultPathBuild)) {
                docsDirToServe = defaultPathBuild;
                logInfo("Found default directory: " + docsDirToServe);
            } else {
                logError("Default documentation directory not found.");
                logError("Looked for: " + defaultPathTarget.toAbsolutePath());
                logError("And also for: " + defaultPathBuild.toAbsolutePath());
                logError("Please ensure documentation is generated or specify the directory using --dir <path>");
                System.exit(1);
            }
        }


        // Validação final do diretório a ser servido
        if (!Files.exists(docsDirToServe) || !Files.isDirectory(docsDirToServe)) {
            System.exit(1);
        }

        // Inicia o servidor
        startServer(docsDirToServe, port);

    }

    /**
     * Tenta encontrar a raiz do projeto subindo a partir de startDir,
     * procurando por pom.xml ou build.gradle.
     */
    private static Path findProjectRoot(Path startDir) {
        Path current = startDir.normalize();
        // Define limite para evitar loop infinito em casos estranhos
        for (int i = 0; i < 20 && current != null; i++) {
            if (Files.exists(current.resolve("pom.xml")) || Files.exists(current.resolve("build.gradle"))) {
                return current;
            }
            // Sobe um nível
            current = current.getParent();
        }
        return null; // Não encontrou
    }


    private static void startServer(Path rootDirectory, int port) {
        // ... (Código do startServer permanece o mesmo) ...
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            logInfo("Starting Docutavern server for " + rootDirectory + " on port " + port);

            server.createContext("/", new MarkdownFileHandler(rootDirectory)); // Handler permanece o mesmo
            server.setExecutor(Executors.newFixedThreadPool(5));
            server.start();

            logInfo("Server started successfully!");
            logInfo("Access documentation at: http://localhost:" + port);
            logInfo("Press Ctrl+C to stop the server.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logInfo("Stopping server...");
                server.stop(1);
                logInfo("Server stopped.");
            }));

            Thread.currentThread().join();

        } catch (IOException | InterruptedException e) { // Combinei catches
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            logError("Server execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("\nDocutavern CLI");
        System.out.println("Usage:");
        // Atualiza a mensagem de uso para refletir o comportamento padrão
        System.out.println("  docutavern serve [--dir <path>] [--port <number>]");
        System.out.println("Options:");
        System.out.println("  serve            Start the local development server.");
        System.out.println("  --dir <path>     (Optional) Specify the directory containing the generated");
        System.out.println("                   documentation. If omitted, searches for 'target/docutavern-docs'");
        System.out.println("                   or 'build/docutavern-docs' relative to the project root");
        System.out.println("                   (pom.xml/build.gradle location) based on current directory.");
        System.out.println("  --port <number>  (Optional) Specify the port (default: " + DEFAULT_PORT + ").");
        System.out.println("\nExamples:");
        System.out.println("  docutavern serve                 # Attempts auto-detection");
        System.out.println("  docutavern serve --dir my-custom-docs");
        System.out.println("  docutavern serve --port 9000");
    }

    private static void logInfo(String message) { System.out.println("[INFO] " + message); }
    private static void logError(String message) { System.err.println("[ERROR] " + message); }
}