// yes we're not using java's scanner class or regex expressions
// lox scanner -> read in digits and omit double
//                read tokens even without space
//TODO: add multiline comments /* */
package com.packages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.packages.Token.*;

import static com.packages.TokenType.*;

class Scanner {
  private final String source; // entire raw string
  private final List<Token> tokens = new ArrayList<>();

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }

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

  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
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
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;

      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;

      // for checking commentss
      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) {
            advance();
          }
        } else {
          addToken(TokenType.SLASH);
        }
        break;

      case '"':
        string();
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
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
          break;
        }

    }
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean isAlpha(char str) {
    return (str >= 'a' && str <= 'z') || (str == '_') || (str >= '0' && str <= '9');
  }

  private boolean checkalpha(char str) {
    return isAlpha(str) || isDigit(str);
  }

  private void identifier() {
    while (checkalpha(peek()) == true) {
      advance();
    }

    String txt = source.substring(start, current);
    TokenType type = keywords.get(txt);
    if (type == null)
      type = TokenType.IDENTIFIER;
    addToken(type);
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();

    }
    if (isAtEnd()) {
      Lox.error(line, "undetermined string");
      return;
    }
    // read the string and trim off " "
    advance();
    String val = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, val);
  }

  private boolean isDigit(char c) {
    return (c >= '0' && c <= '9');
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }
    if (peek() == '.' && isDigit(peekNext())) {
      advance();

      while (isDigit(peek()))
        advance();
    }

    // for catching wrong variables like 9hello
    if (isAlpha(peek())) {
      while (isAlpha(peek())) {
        advance();
      }
      String wrongName = source.substring(start, current);
      Lox.error(line, wrongName + " is not a valid variable!");
      return;
    }

    // advance();
    // Lox.error(line, "overflow number");
    // return;

    String num = source.substring(start, current);
    addToken(TokenType.NUMBER, Double.parseDouble(num));

  }

  // STEP1: for looping and saving tokens
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }
    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

}
