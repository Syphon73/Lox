package com.packages;

import com.packages.tool.ASTprint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  // private = interpreter remembers values till session is active
  // final = dont overload interpreter() with a blank one once program runs
  private static final Interpreter interpreter = new Interpreter();

  // dont run code if errors
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);

      // check for errors after parsing whole file
      if (hadError)
        System.exit(65);
      else if (hadRuntimeError)
        System.exit(70);
    } else {
      runPrompt();
    }
  }

  // CLI : ig now you need to read files and convert to tokens [tokenRun() for
  // that]
  public static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    tokenRun(new String(bytes, Charset.defaultCharset()));
  }

  // interactive session: keep infinite loop and read buffer until user presses
  public static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null)
        break; // ctrlD in CLI
      tokenRun(line);
      hadError = false;
    }
  }

  // (scanner, token) is of lox not java stl
  public static void tokenRun(String source) {
    Scanner sc = new Scanner(source);
    List<Token> tokens = sc.scanTokens();
    System.out.println("--- TOKENS RECEIVED ---");
    for (Token token : tokens) {
      System.out.println(token);
    }

    Parser parser = new Parser(tokens);
    // Expr expression = parser.parse();
    List<Stmt> statements = parser.parse();

    // if (expression == null) {
    // System.out.println("DEBUG: Parser returned NULL!");
    // } else {
    // System.out.println("DEBUG: Expression Class -> " +
    // expression.getClass().getName());
    // }

    // if (hadError) {
    // return;
    // }
    // interpreter.interpret(expression);
    interpreter.interpret(statements);
    // System.out.println(new ASTprint().print(expression));
  }

  // handeling errors
  // static void error(int line, String message) {
  // // TODO: get the precise location of error from the code file -> calculate
  // (SOF
  // // to lexeme len) + (len of lexeme)
  // // dont do for every lexeme, only ones which needs to be reported to user
  // report(line, " ", message);
  // }
  public static void error(int line, String message) {
    report(line, "", message);
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  public static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Lox Error" + where + ": " + message + " !Good luck fixing it :D");
    hadError = true;
  }
}
