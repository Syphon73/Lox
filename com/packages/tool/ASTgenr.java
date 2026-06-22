// automatically create the Expr() boilerplate as the Terminals/Values grows

// import com.package.lox
// class Expr {
//    class binary {
//      binary(a,b,c){
//          this.a = a;
//          this.b=b;
//          .
//          .
//      }
//    }
//    .
//    .
// }

package com.packages.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class ASTgenr {
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];

    defineAst(outputDir, "Expr", Arrays.asList( // recurssion happening here
        "Assign   : Token name, Expr value",
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right",
        "Variable : Token name"));

    defineAst(outputDir, "Stmt", Arrays.asList(
        "Expression : Expr expression",
        "Print      : Expr expression",
        "Block      : List<Stmt> statements",
        "Var        : Token name, Expr initializer"));
  }

  // pastry visitor pattern implementation

  // STEP 1
  // interface Visitor<R> {
  // R visitBinaryExpr(Binary expr);
  // R visitUnaryExpr(Unary expr);

  // STEP 2
  // abstract <R> R accept(Visitor<R> visitor);

  // STEP 3
  // static class Binary extends Expr {
  // @Override
  // <R> R accept(Visitor<R> visitor) {
  // return visitor.visitBinaryExpr(this);
  // }

  private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package com.packages;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("public abstract class " + baseName + " {");

    // Generate the Visitor Interface (Step 1 & 2)
    defineVisitor(writer, baseName, types);

    // Loop through and generate each concrete subclass (Step 3 inside)
    for (String type : types) {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, baseName, className, fields);
    }

    writer.println("}");
    writer.close();
  }

  private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
    // STEP 1
    writer.println("public interface Visitor<R> {");

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println(" R visit" + typeName + baseName + "(" +
          typeName + " " + baseName.toLowerCase() + ");");
    }
    writer.println(" }");

    // STEP 2
    writer.println();
    writer.println("public abstract <R> R accept(Visitor<R> visitor);");
    // writer.println("}");
  }

  private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
    writer.println();
    writer.println(" public static class " + className + " extends " + baseName + " {");

    // Constructor.
    writer.println("   public " + className + "(" + fieldList + ") {");

    // Store parameters in fields
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";");
    }

    writer.println("    }");

    // STEP 3: Visitor pattern override execution inside subclass
    writer.println();
    writer.println("    @Override");
    writer.println("    public <R> R accept(Visitor<R> visitor) {");
    writer.println("      return visitor.visit" + className + baseName + "(this);");
    writer.println("    }");

    // Fields
    writer.println();
    for (String field : fields) {
      writer.println("   public final " + field + ";");
    }

    writer.println(" }");
  }
}
