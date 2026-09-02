# Mava / matcc

**matcc** is a small C-like compiler targeting the **BatPU-2 8-bit architecture**.

It is designed as a learning/toy compiler project rather than a replacement for Clang or GCC. The goal is to take a small subset of C, compile it into BatPU-2 assembly, and eventually provide a standalone native toolchain around that assembly.

## Overview

The current toolchain looks like this:

```text
        C source
           │
           ▼
       ┌───────┐
       │ matcc │
       └───┬───┘
           │
           ▼
   BatPU-2 assembly
           │
           ▼
   ┌──────────────┐
   │ assembler.py │
   └───────┬──────┘
           │
           ▼
  BatPU-2 Machine Code
           
           

```

The compiler itself does **not** need to know anything about the native assembler. `matcc-as` is intended to be a completely standalone BatPU-2 assembler/backend.

---

## Features

Currently supported or partially supported:

* C-like syntax
* Integer variables
* Local variables
* Function declarations
* Function parameters
* Function calls
* Return values
* Assignment expressions
* Binary arithmetic
* Comparisons
* Unary operators
* `if` / `else`
* `while`
* `for`
* Nested function calls
* Lexical scopes
* Basic semantic analysis
* BatPU-2 assembly generation
* 8-bit register-oriented code generation

Example:

```c
int add(int a, int b) {
    return a + b;
}

int twice(int x) {
    return add(x, x);
}

int main() {
    return twice(5);
}
```

Produces BatPU-2 assembly similar to:

```asm
LDI r15 239
CAL .main
HLT

.add
STR r1, r15, 0
STR r2, r15, 1
LOD r1, r15, 0
MOV r1, r2
LOD r1, r15, 1
ADD r1, r2, r1
RET

.twice
STR r1, r15, 0
LOD r1, r15, 0
MOV r1, r2
LOD r1, r15, 0
CAL .add
RET

.main
LDI r1, 5
CAL .twice
RET
```

---

# Architecture

matcc currently targets the **BatPU-2** architecture.

BatPU-2 is an 8-bit CPU with 16 registers:

```text
r0   r1   r2   r3
r4   r5   r6   r7
r8   r9   r10  r11
r12  r13  r14  r15
```

`r0` is permanently zero.

The compiler currently uses `r15` as its stack/frame register and uses the remaining registers for values, arguments, and temporary calculations.

The compiler therefore operates under an explicitly 8-bit model rather than assuming normal 32-bit or 64-bit C integers.

---

# Compiler Pipeline

matcc is split into several stages.

## Lexer

Converts source code into tokens.

```text
int main() {
    return 5;
}
```

becomes a stream of tokens representing keywords, identifiers, literals, operators, and punctuation.

## Parser

Builds an Abstract Syntax Tree (AST) from the token stream.

## Semantic Analyzer

Performs basic semantic validation, including things such as:

* undefined variables
* undefined functions
* duplicate declarations
* scope handling
* expression validation

## Code Generator

Converts the AST into BatPU-2 assembly.

The code generator is intentionally simple. It prioritizes understandable generated code over aggressive optimization.

---

# Building

matcc uses Gradle.

Clone the repository and build it with:

```bash
./gradlew build
```

The resulting JAR will be located under:

```text
build/libs/
```

If the project is configured with Gradle's application plugin, it can also be run through:

```bash
./gradlew run
```

---

# Usage

Compile a C source file:

```bash
java -jar /path/to/matcc.jar test.c test.as
```

For example:

```c
int main() {
    return 42;
}
```

can produce:

```asm
LDI r1 42
RET
```

The generated `.as` file is BatPU-2 assembly and is independent of the Java compiler implementation.


---

# Example Program

```c
int add(int a, int b) {
    return a + b;
}

int main() {
    return add(2, 3);
}
```

Expected result:

```text
program exit code = 5
```

Another example:

```c
int add(int a, int b) {
    return a + b;
}

int twice(int x) {
    return add(x, x);
}

int main() {
    return twice(5);
}
```

Expected result:

```text
program exit code = 10
```

---

# Project Structure

The project roughly follows this structure:

```text
matcc/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── krxoid/
│                   ├── ast/
│                   ├── codegen/
│                   ├── lexer/
│                   ├── parser/
│                   └── semantic/
│
├── build.gradle
├── settings.gradle
└── README.md
```

The exact package structure may change as the compiler develops.

---

# Design Philosophy

matcc is intentionally small.

The project is **not** trying to implement:

* full ISO C
* LLVM
* GCC
* Clang
* advanced optimization passes
* a sophisticated register allocator
* every possible C language feature

Instead, the project focuses on making a complete compiler pipeline understandable and functional.

The main objective is:

> **Source code → compiler → machine code**

with as little unnecessary complexity as possible.

---

# Current Limitations

matcc is still a toy compiler and has several limitations.

Some C features are currently unsupported or incomplete, including:

* pointers
* arrays
* structs
* floating-point types
* dynamic memory
* preprocessor support
* headers
* the full C type system
* advanced optimization
* complete error recovery
* full C standard compliance

Arithmetic operations such as multiplication, division, and modulo also depend on whether appropriate BatPU-2 operations or compiler-generated routines are available.

The code generator is currently optimized for simplicity rather than optimal register usage.

---

# Roadmap

Possible future improvements:

### Compiler

* [ ] Better diagnostics
* [ ] Complete semantic checking
* [ ] More C operators
* [ ] `break` / `continue`
* [ ] Arrays
* [ ] Pointers
* [ ] More robust stack-frame handling
* [ ] Register allocation
* [ ] Constant folding
* [ ] Dead-code elimination
* [ ] Better optimization

### Native Toolchain

* [ ] Complete standalone `matcc-as`
* [ ] Full BatPU-2 instruction support
* [ ] BatPU-2 pseudo-instruction support
* [ ] Correct BatPU-2 memory model
* [ ] x86-64 assembly output
* [ ] ELF generation through GNU `as` and `ld`
* [ ] Standalone executable workflow

---

# Why?

Because making a compiler is cool.

matcc started as a small compiler experiment targeting an 8-bit Minecraft CPU. The project is primarily about learning how programming languages, compilers, assembly, calling conventions, and CPUs actually fit together.

It doesn't need to be Clang.

It just needs to compile.

---

## License

## License

matcc is licensed under the GNU General Public License v3.0.

See the `LICENSE` file for the full license text.
