// yes we're not using java's scanner class or regex expressions
// lox scanner -> read in digits and omit double
//                read tokens even without space

package com.packages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.packages.TokenType.*;

class Scanner {
  private final String source; // entire raw string
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0;
  private int current = 0; // for checking where word ends
  private int line = 1; // tracks row number

  // constructor
  Scanner(String source) {
    this.source = source;
  }

  // STEP3 for eating next char
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  // for two lexemme (eat only if valid)
  private boolean match(char ptr) {
    if (isAtEnd()) {
      return false;
    }
    if (source.charAt(current) != ptr) {
      return false;
    }
    current++;
    return true;
  }

  // for checking comments (not eating char)
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  // STEP 5
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  // STEP2 for recognising char
  private void scanToken() {
    // consume next char
    char c = advance();
    switch (c) {

      // STEP 4
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;

      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;

      // for checking commentss
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else {
          addToken(SLASH);
        }
        break;

      // ignore whitespaces
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        line++;
        break;

      default:
        Lox.error(line, "Unexpected character.");
        break;
    }
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  // STEP1: for looping and saving tokens
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

}
