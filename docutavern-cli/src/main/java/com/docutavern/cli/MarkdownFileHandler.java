package com.docutavern.cli;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class MarkdownFileHandler implements HttpHandler {

    private final Path rootDir;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    private final Map<String, String> mimeTypes = new HashMap<>();

    public MarkdownFileHandler(Path rootDir) {
        this.rootDir = rootDir.toAbsolutePath();

        // Configure Flexmark Parser and Renderer
        MutableDataSet options = new MutableDataSet();
        // options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), ...)); // Add extensions if needed
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();

        // Basic MIME Types
        mimeTypes.put(".html", "text/html");
        mimeTypes.put(".htm", "text/html");
        mimeTypes.put(".css", "text/css");
        mimeTypes.put(".js", "application/javascript");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".jpg", "image/jpeg");
        mimeTypes.put(".jpeg", "image/jpeg");
        mimeTypes.put(".gif", "image/gif");
        mimeTypes.put(".svg", "image/svg+xml");
        mimeTypes.put(".ico", "image/x-icon");
        mimeTypes.put(".md", "text/markdown"); // Mime type if serving raw MD needed
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(requestMethod)) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String requestedPath = exchange.getRequestURI().getPath();
        // Normalize path and handle default files
        if (requestedPath.endsWith("/") || requestedPath.isEmpty()) {
            // Prioritize index.html, then try rendering index.md (or README.md)
            if (Files.exists(rootDir.resolve(stripLeadingSlash(requestedPath) + "index.html"))) {
                requestedPath += "index.html";
            } else if (Files.exists(rootDir.resolve(stripLeadingSlash(requestedPath) + "index.md"))) {
                requestedPath += "index.md"; // We will convert this MD
            } else if (Files.exists(rootDir.resolve(stripLeadingSlash(requestedPath) + "README.md"))) {
                requestedPath += "README.md"; // Or convert this MD
            } else {
                // Fallback: No default file found
                sendError(exchange, 404, "Default file (index.html, index.md, README.md) not found in directory.");
                return;
            }
        }

        Path filePath = rootDir.resolve(stripLeadingSlash(requestedPath)).normalize();

        // --- Security Check ---
        if (!filePath.startsWith(rootDir)) {
            System.err.println("[WARN] Attempted access outside root: " + filePath);
            sendError(exchange, 403, "Forbidden");
            return;
        }

        if (Files.isDirectory(filePath)) {
            // If path normalization resulted in directory again, redirect to add trailing slash
            // (Tells browser it's a directory, allowing relative links to work better)
            String location = exchange.getRequestURI().toString() + "/";
            exchange.getResponseHeaders().set("Location", location);
            sendError(exchange, 301, "Moved Permanently");
            return;
        }


        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            String fileName = filePath.getFileName().toString().toLowerCase();

            // --- Markdown Conversion ---
            if (fileName.endsWith(".md")) {
                try {
                    String markdownContent = Files.readString(filePath, StandardCharsets.UTF_8);
                    Node document = markdownParser.parse(markdownContent);
                    String htmlBody = htmlRenderer.render(document);

                    // Wrap in a basic HTML template
                    String fullHtml = createHtmlTemplate(stripExtension(fileName), htmlBody);
                    byte[] htmlBytes = fullHtml.getBytes(StandardCharsets.UTF_8);

                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, htmlBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(htmlBytes);
                    }
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to convert Markdown: " + filePath + " - " + e.getMessage());
                    e.printStackTrace(); // Log full error
                    sendError(exchange, 500, "Internal Server Error: Markdown conversion failed.");
                }
            }
            // --- Serve Static Files ---
            else {
                String contentType = guessContentType(filePath);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, Files.size(filePath));
                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(filePath, os);
                } catch (IOException e) {
                    // Handle potential broken pipe errors gracefully if client disconnects
                    System.err.println("[WARN] IO Error sending file " + filePath + ": " + e.getMessage());
                }
            }
        } else {
            System.out.println("[INFO] File not found: " + filePath);
            sendError(exchange, 404, "Not Found: " + requestedPath);
        }
    }

    /** Creates a very basic HTML structure around the converted Markdown body. */
    private String createHtmlTemplate(String title, String htmlBody) {
        // TODO: Make this template external and configurable
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                // Link to a potential CSS file (assumes style.css in root output dir)
                "  <link rel=\"stylesheet\" href=\"/style.css\">\n" +
                "  <title>" + escapeHtml(title) + " - Docutavern</title>\n" +
                // Basic styles (can be moved to style.css)
                "<style>\n" +
                "  body { font-family: sans-serif; line-height: 1.6; padding: 20px; max-width: 900px; margin: auto; }\n" +
                "  pre { background-color: #f4f4f4; padding: 15px; border-radius: 4px; overflow-x: auto; }\n" +
                "  code { font-family: monospace; }\n" +
                "  blockquote { border-left: 3px solid #ccc; padding-left: 15px; margin-left: 0; color: #555; }\n" +
                "  img { max-width: 100%; height: auto; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                // TODO: Add sidebar rendering logic here if needed
                "  <main>\n" +
                htmlBody + "\n" +
                "  </main>\n" +
                "</body>\n" +
                "</html>";
    }

    /** Very basic HTML escaping */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", "'")
                    .replace("'", "'");
     }

    private String guessContentType(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (Map.Entry<String, String> entry : mimeTypes.entrySet()) {
            if (fileName.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        // Default guess based on common types not in the map explicitly
        try {
             String detectedType = Files.probeContentType(path);
             if (detectedType != null) {
                 return detectedType;
             }
        } catch (IOException e) {
             System.err.println("[WARN] Could not probe content type for " + path + ": " + e.getMessage());
        }
        return "application/octet-stream"; // Fallback binary type
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, messageBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(messageBytes);
        }
    }

    private String stripLeadingSlash(String path) {
        if (path != null && path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

     private String stripExtension(String fileName) {
         int lastDot = fileName.lastIndexOf('.');
         if (lastDot > 0) {
             return fileName.substring(0, lastDot);
         }
         return fileName;
     }
}