package com.krxoid;

import com.krxoid.ast.Program;
import com.krxoid.codegen.CodeGenerator;
import com.krxoid.lexer.Lexer;
import com.krxoid.lexer.Token;
import com.krxoid.parser.Parser;
import com.krxoid.semantic.SemanticAnalyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Main {

    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: matcc <source.c> [output]");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputName = (args.length == 2) ? args[1] : "result";

        try {

            // Read source
            String source = Files.readString(Path.of(inputFile));

            // Lexing
            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.lex();

            // Parsing
            Parser parser = new Parser(tokens);
            Program program = parser.parse();

            // Semantic analysis
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            semanticAnalyzer.analyze(program);

            // Code generation
            CodeGenerator codeGenerator = new CodeGenerator();
            String assembly = codeGenerator.generate(program);

            // Write assembly
            try (FileWriter writer = new FileWriter(outputName + ".as")) {
                writer.write(assembly);
            }

            System.out.println("Compilation successful.");
            System.out.println("Output written to " + outputName + ".as");

        } catch (IOException e) {

            System.err.println("I/O Error: " + e.getMessage());
            System.exit(1);

        } catch (Exception e) {

            System.err.println("Compilation failed:");
            e.printStackTrace();
            System.exit(1);

        }
    }
}