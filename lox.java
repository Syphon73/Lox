//package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class lox {

  // dont run code if errors
  static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);

      // check for errors after parsing whole file
      if (hadError)
        System.exit(65);
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
  // enter
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

    for (Token token : tokens) {
      System.out.println(token);
    }
  }

  // handeling errors
  static void error(int line, String message) {
    // TODO: get the precice location of error from the code file -> add to where
    report(line, " ", message);
  }

  public static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message + " !Good luck fixing it :D");
    hadError = true;
  }
}
