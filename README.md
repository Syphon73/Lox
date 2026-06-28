# Lox Interpreter

A tree-walk interpreter for the Lox language, built from scratch in Java based on *Crafting Interpreters*

# Setup

compile ASTtreeGenerator to generate Expr automatically
```
javac com/packages/tool/ASTgenr.java
java com/packages/tool/ASTgenr.java com/packages
```
Compile everything else and fire the interpreter up
```
javac com/packages/*.java com/packages/tool/*.java
java com.packages.Lox
```
