package com.example;

import com.docutavern.annotations.*; // Importar tudo

/**
 * An example class showing how to use Docutavern annotations. (UPDATED)
 */
@TavernChapter(title = "Guia do Aventureiro para MyDocumentedClass", order = 1)
@TavernStory( description = "Descrição vai aqui!")
@TavernNote(type=NoteType.TIP, title="Dica Rápida", value="Consulte o `_sidebar.md` gerado para navegar facilmente!")
@TavernImage(path="assets/tavern-logo.png", caption="Logo da Taverna", width="100px") // NOVO: Imagem no capítulo
public class MyDocumentedClass {

    private static final String DEFAULT_GREETING = "Bem-vindo à Docutavern!";

    @TavernSection(title = "Preparando a Mochila (Configuração Inicial)")
    @TavernStory(description = "Descrição vai aqui!")
    @TavernCodeSnippet(lang="java", value = "snippet vai aqui!")
    @TavernNote(type=NoteType.IMPORTANT, value="O construtor imprime uma saudação no console.") // NOVO: Nota na seção
    public MyDocumentedClass() {
        System.out.println(DEFAULT_GREETING);
    }

    @TavernSection(title = "Explorando a Masmorra (Método Principal)")
    @TavernStory(
            description = "Este método é o coração da exploração.\n" +
                    "Use-o com sabedoria para interagir com o mundo."
    )
    @TavernParam(name="questGiver", description="O nome de quem te deu a missão. Se for nulo ou vazio, a exploração é anônima.")
    @TavernNote(type=NoteType.WARNING, value="Explorar masmorras pode ser perigoso!")
    public void exploreDungeon(String questGiver) { // Assinatura corresponde ao @TavernParam
        if (questGiver == null || questGiver.isEmpty()) {
            System.out.println("Um aventureiro anônimo explora a masmorra...");
        } else {
            System.out.println(questGiver + " te enviou para explorar a masmorra!");
        }
        System.out.println("Você encontrou uma poção!");
    }

    @TavernSection(title = "Coletando Tesouros (Método de Retorno)")
    @TavernStory(description = "Encontre tesouros valiosos (neste caso, uma String) com este método.")
    @TavernCodeSnippet(lang = "java", value = "snippet aqui!")
    @TavernReturn(description="Uma String contendo o nome do tesouro encontrado.")
    @TavernImage(path = "images/treasure-map.svg", caption = "Um mapa do tesouro!", altText = "Desenho de um mapa enrolado")
    public String findTreasure() {
        return "Mapa Secreto para a Fonte da Documentação Infinita";
    }

    @TavernSection(title = "O Pergaminho Padrão (Constante)")
    @TavernStory(description = "Uma constante útil disponível para todos os aventureiros.")
    @TavernNote(type=NoteType.NOTE, value="Constantes são documentadas como seções de campos.")
    public static final String STANDARD_SCROLL_TEXT = "Leia-me!";

}