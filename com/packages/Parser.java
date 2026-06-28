package com.packages;

import java.util.List;

import com.packages.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import static com.packages.TokenType.*;

//Lox Grammar (recursive descent: top-bottom) (priority: bottom-top)
//expression → equality ;
//equality → comparison ( ( "!=" | "==" ) comparison )* ;
//comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
//term → factor ( ( "-" | "+" ) factor )* ;
//factor → unary ( ( "/" | "*" ) unary )* ;
//unary → ( "!" | "-" ) unary | primary ;
//primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;

class Parser {
  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // loop throgh the whole file instead of one statement
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }
    return statements;
  }

  // ---phaltu ke fnx--------------

  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private boolean check(TokenType type) {
    if (isAtEnd())
      return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  private boolean match(TokenType... types) {
    for (TokenType i : types) {
      if (check(i)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private Expr factor() {
    Expr expr = unary();
    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();
    while (match(TokenType.MINUS, TokenType.PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  // parsing each statement in file
  private Stmt statement() {
    if (match(TokenType.PRINT))
      return printStatement();
    if (match(TokenType.IF))
      return ifStatement();
    if (match(TokenType.WHILE))
      return whileStatement();
    if (match(TokenType.FOR))
      return forStatement();
    if (match(TokenType.LEFT_BRACE))
      return new Stmt.Block(block());
    return expressionStatement();
  }

  private Stmt ifStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if' ");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition");

    Stmt thenBranch = statement();

    Stmt elseBranch = null;
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }
    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt printStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after print");
    Expr value = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after value");
    consume(TokenType.SEMICOLON, "Expect ';' after value");
    return new Stmt.Print(value);
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect variable name");
    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }
    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration");
    return new Stmt.Var(name, initializer);
  }

  // TODO: create incrementing condition for i++ and i--
  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after condition");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt forStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'");

    // initializer
    Stmt initializer;
    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    // condition
    Expr condition = null;
    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    }
    consume(TokenType.SEMICOLON, "Expect ';' after loop condition");

    // incrementation
    Expr increment = null;
    if (!check(TokenType.RIGHT_PAREN)) {
      increment = expression();
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses");

    // body of the loop
    Stmt body = statement();

    // inner local block, run body -> i++ -> increment body
    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }

    // for(;;)
    if (condition == null) {
      condition = new Expr.Literal(true);
    }
    body = new Stmt.While(condition, body);
    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments");
        }
        arguments.add(expression());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments");
    return new Expr.Call(callee, paren, arguments);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after expression");
    return new Stmt.Expression(expr);
  }

  private Stmt.Function function(String kind) {
    Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name");
    consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name");
    List<Token> parameters = new ArrayList<>();

    // parse parameters separated by commas
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }
        parameters.add(
            consume(TokenType.IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters");
    consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body");
    List<Stmt> body = block();

    return new Stmt.Function(name, parameters, body);
  }

  // ---main grammar---------

  private Expr expression() {
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.FUNC))
        return function("function");
      if (match(TokenType.VAR))
        return varDeclaration();

      return statement();
    } catch (ParseError error) {
      syncronize();
      return null;

    }
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after block");
    return statements;
  }

  private Expr assignment() {

    Expr expr = or();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }
      error(equals, "Invalid assignment target.");
    }
    return expr;
  }

  private Expr or() {
    Expr expr = and(); // higher precedence
    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }
    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }
    return expr;
  }

  private Expr equality() {
    Expr expr = comparison(); // nonterminal

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();
    while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    // return primary();
    return call();
  }

  private Expr primary() {
    if (match(TokenType.FALSE))
      return new Expr.Literal(false);
    if (match(TokenType.TRUE))
      return new Expr.Literal(true);
    if (match(TokenType.NIL))
      return new Expr.Literal(null);

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().literal);
    }
    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression");
  }

  private Expr call() {
    Expr expr = primary();
    while (true) {
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }
    return expr;
  }

  // -----error reporting---------------
  // panic-sync mode or sentinel mode
  private static class ParseError extends RuntimeException { // sentinel class

  }

  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();
    throw error(peek(), message);
  }

  // Panic mode: unwind java call stack until syncronization point
  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void syncronize() {
    advance();
    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON)
        return;
      switch (peek().type) {
        case CLASS:
        case FUNC:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }
      advance();
    }

  }
}
