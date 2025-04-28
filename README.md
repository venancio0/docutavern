# Docutavern Documentation Generator

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](...) 
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![GitHub last commit](https://img.shields.io/github/last-commit/venancio0/echodev-newsletter)](#)
![Java](https://img.shields.io/badge/Java-%23ED8B00.svg??style=for-the-badge&logo=openjdk&logoColor=white)

**Docutavern: Your Friendly Tavern for Java Documentation!** :scroll::beer:

Docutavern is a Java tool designed to drastically simplify the creation and maintenance of documentation for your Java projects. Inspired by tools like MkDocs and Docusaurus, Docutavern allows you to write your documentation directly within your Java source code using Annotations, keeping it synchronized and easy to update.

The result is well-structured Markdown files, ready to be used with your favorite static site generator or previewed locally with Docutavern's built-in server.

## :sparkles: Key Features

*   **In-Code Documentation:** Write descriptions, examples, and metadata directly in your classes, methods, and fields using `@Tavern*` annotations.
*   **Automatic Markdown Generation:** The integrated annotation processor generates `.md` files during your project's compilation phase.
*   **Themed Annotations:** Intuitive annotations like `@TavernChapter`, `@TavernSection`, `@TavernStory`, `@TavernCodeSnippet`, `@TavernParam`, `@TavernReturn`, `@TavernNote`, `@TavernImage`, and more.
*   **Build Tool Integration:** Easy setup with Maven (Gradle support planned) to integrate documentation generation into your build lifecycle.
*   **Local Preview Server:** Use the `docutavern serve` command to instantly preview your rendered documentation as HTML in your browser.
*   **Extensible:** Modular architecture allowing for the addition of new annotations and features.

## :dart: Motivation

Keeping documentation up-to-date can be challenging. External tools often lead to documentation becoming desynchronized from the code. Docutavern aims to solve this by:

1.  **Keeping documentation close to the code** it describes.
2.  **Automating generation** to reduce manual effort.
3.  Offering a **Java-centric solution**.

## :gear: Core Concept

Docutavern's workflow is straightforward:

1.  **Annotate:** Add `@Tavern*` annotations to your Java code.
2.  **Compile:** Build your project (`mvn compile`). The `docutavern-processor` activates automatically.
3.  **Generate:** `.md` files are generated in the configured output directory (default: `target/docutavern-output`).
4.  **Preview:** Use `docutavern serve` (after building Docutavern itself) to view the rendered documentation locally.
5.  **Publish:** Use the generated `.md` files with MkDocs, Docsify, Docusaurus, VuePress, Hugo, or any other tool that consumes Markdown to build your final documentation site.

## :package: Project Modules

Docutavern is organized into the following Maven modules:

*   **`docutavern-annotations`:** Defines the `@Tavern*` annotation interfaces you'll use in your code.
*   **`docutavern-model`:** Contains the Java classes (POJOs) representing the internal structure of the collected documentation.
*   **`docutavern-processor`:** The heart of the project. Implements the `javax.annotation.processing.Processor` that reads annotations during compilation and populates the data model.
*   **`docutavern-generator`:** Responsible for taking the populated data model and generating the final `.md` files.
*   **`docutavern-cli`:** Provides the command-line interface, including the `serve` command which uses an embedded Java HTTP server and Flexmark to render Markdown to HTML dynamically.
*   **`docutavern-example-usage`:** An example project demonstrating how to use Docutavern annotations.
*   **`docutavern-parent`:** The parent POM managing dependencies and build configurations for all modules.

## :rocket: Prerequisites

To **use** Docutavern in your project, you will need:

*   **JDK 11** or higher.
*   **Maven 3.6+** (Gradle support planned).

To **preview** documentation using the `serve` command, you need to have built Docutavern locally first (see below).

## :wrench: Installation and Setup (in Your Project)

1.  **Add Annotation Dependency:**
    In your project's `pom.xml`, add the `docutavern-annotations` dependency:

    ```xml
    <dependencies>
        <dependency>
            <groupId>com.docutavern</groupId>
            <artifactId>docutavern-annotations</artifactId>
            <version>0.1.0-SNAPSHOT</version> <!-- Use the current Docutavern version -->
            <!-- Scope can be 'compile' or 'provided' depending on RetentionPolicy -->
            <scope>compile</scope>
        </dependency>
        <!-- Your other dependencies -->
    </dependencies>
    ```

2.  **Configure the Annotation Processor:**
    Configure the `maven-compiler-plugin` to use the `docutavern-processor`. Ensure you include the processor and *its* required dependencies in the `annotationProcessorPaths`.

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version> <!-- Use a recent version -->
                <configuration>
                    <source>11</source> <!-- Or your Java version -->
                    <target>11</target> <!-- Or your Java version -->
                    <annotationProcessorPaths>
                        <!-- The Docutavern Processor -->
                        <path>
                            <groupId>com.docutavern</groupId>
                            <artifactId>docutavern-processor</artifactId>
                            <version>0.1.0-SNAPSHOT</version> <!-- Docutavern version -->
                        </path>
                        <!-- Dependencies of the Processor (Model, Generator, etc.) -->
                        <path>
                            <groupId>com.docutavern</groupId>
                            <artifactId>docutavern-model</artifactId>
                             <version>0.1.0-SNAPSHOT</version>
                         </path>
                         <path>
                            <groupId>com.docutavern</groupId>
                            <artifactId>docutavern-generator</artifactId>
                            <version>0.1.0-SNAPSHOT</version>
                         </path>
                         <!-- Third-party dependencies used by Generator/Processor -->
                          <path>
                            <groupId>org.commonmark</groupId> <!-- If used by Generator -->
                            <artifactId>commonmark</artifactId>
                            <version>0.21.0</version> <!-- Use the correct version -->
                         </path>
                          <path> <!-- Flexmark required by CLI's Handler, needed if processor calls it -->
                             <groupId>com.vladsch.flexmark</groupId>
                             <artifactId>flexmark-all</artifactId>
                              <version>0.64.8</version> <!-- Flexmark version -->
                         </path>
                         <path> <!-- Processor might use AutoService internally -->
                             <groupId>com.google.auto.service</groupId>
                             <artifactId>auto-service-annotations</artifactId>
                             <version>1.1.1</version>
                         </path>
                    </annotationProcessorPaths>
                    <compilerArgs>
                        <!-- Optional: Specify the output directory -->
                        <arg>-Adocutavern.outputDir=${project.build.directory}/docutavern-docs</arg>
                    </compilerArgs>
                    <showWarnings>true</showWarnings>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```

## :books: Usage Guide

1.  **Annotate Your Code:**
    Start adding `@Tavern*` annotations to your classes, packages (`package-info.java`), methods, constructors, and fields.

    ```java
    package com.mypackage;

    import com.docutavern.annotations.*;

    @TavernChapter(title = "My Awesome Class", order = 1)
    @TavernStory("This class demonstrates the basic usage of Docutavern.")
    @TavernNote(type=NoteType.TIP, value="Use annotations to keep documentation close to the code!")
    public class MyAwesomeClass {

        @TavernSection(title = "Greeting Method")
        @TavernStory("A simple method that returns a greeting.")
        @TavernParam(name = "name", description = "The name to greet.")
        @TavernReturn(description = "The formatted greeting string.")
        @TavernCodeSnippet("String greeting = new MyAwesomeClass().greet(\"Adventurer\");")
        public String greet(String name) {
            return "Hello, " + name + "! Welcome to the tavern!";
        }
    }
    ```

2.  **Compile Your Project:**
    Execute your build tool's compile command. For Maven:
    ```bash
    mvn compile
    # or
    mvn package
    ```
    This will activate the `docutavern-processor`, which reads the annotations and generates `.md` files in the configured directory (default: `target/docutavern-output` or `target/docutavern-docs` if you used the `compilerArg`).

3.  **Preview Locally with `docutavern serve`:**
    *   **Step One (Only once or after updating Docutavern):** Clone the Docutavern repository and build it to generate the executable CLI. In the *Docutavern project root*:
        ```bash
        git clone https://github.com/your-username/docutavern.git # Or your URL
        cd docutavern
        mvn clean package
        ```
        This creates the JAR in `docutavern-cli/target/` and the wrapper scripts (`docutavern.sh`, `docutavern.bat`) in the Docutavern root.
    *   **Step Two (In your project):** Navigate back to the root of *your* project (where you added the annotations). Ensure you have compiled your project (step 2 above) to generate the documentation in `target/docutavern-output`.
    *   **Step Three (Run the server):** Execute the Docutavern wrapper script (which you just built) from the root of *your* project:
        *   Linux/macOS: `/path/to/docutavern/docutavern.sh serve`
        *   Windows: `C:\path\to\docutavern\docutavern.bat serve`
        *(Tip: Add the Docutavern root directory to your system's PATH to just run `docutavern serve`)*

        This starts a local web server (default: port 8080) serving the generated files, converting `.md` files to HTML dynamically. Access `http://localhost:8080` in your browser.

4.  **Use the Markdown Output:**
    The generated `.md` files in `target/docutavern-output` are standard and can be used as source for tools like:
    *   [MkDocs](https://www.mkdocs.org/) (using `mkdocs serve`)
    *   [Docsify](https://docsify.js.org/#/) (copy the basic `index.html` and `_sidebar.md` generated by Docutavern or provide your own)
    *   [Docusaurus](https://docusaurus.io/), [VuePress](https://vuepress.vuejs.org/), [Hugo](https://gohugo.io/), etc. (adapt the file structure as needed).

## :mortar_board: Complete Example

See the `docutavern-example-usage` module in this repository for a practical example of applying the annotations.

## :handshake: Contribution

Contributions are welcome! Feel free to open Issues for bugs or feature suggestions. If you want to contribute code, please open an Issue first to discuss the change and then submit a Pull Request.

(Add more details about the contribution process, code style, etc., if desired).

## :page_facing_up: License

This project is licensed under the [Apache License 2.0](LICENSE). <!-- Create a LICENSE file -->

---

Made with :heart: and :coffee: by the Venancio0.
