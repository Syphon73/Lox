package com.packages;

import java.util.List;

// parser -> func () goes to an AST tree node Stmt.Function (parsing phase) -> 
// interpreter -> wrap blueprint into LoxFunction Object + new environment (function declaration phase) ->
// (function call/execution phase) cast LoxCallable(LoxFunction Obj) -> .call() -> new env + block execution

interface LoxCallable {
  int arity();

  Object call(Interpreter interpreter, List<Object> arguments);
}
