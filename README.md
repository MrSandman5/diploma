# Diploma
Project describes automatic code instrumentation tool for Java language. It uses rules file written on DSL, which defines settings for code instrumentation. Main principle: parsing into AST => code instrumentation => AST generation.

Features:
- Supported code blocks:
  - **if-then-else**
  - **switch-case**
  - **while/do-while**
  - **for/foreach**
- Supported instrumentation methods:
  - **before**
  - **after**
  - **after return**
  - **after throwing**
- Generation of info message with metadata about instrumentated code:
  - **current CLASS**
  - **current METHOD**
  - **current block (ITEM)**
  - **instrumentation method (LOCATION)**
  - **tracked VARIABLE**
  - **type of handled EXCEPTION**

Used instruments:
- **Java 8**
- **Javaparser + JavaSymbolSolver**
- **JAXB**
