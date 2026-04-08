# Император (The Empress) - Linguagem feita em Java para ? 

## Instalação 

Acesse o link ![google.com](https://google.com) para instalar e criar seu arquivo .emp

## Glossário

- True: истинный
- False: ложь
- Integer: интеграл
- String: строка
- Decimal: десятичный
- Boolean: логический
- Do: делать
- While: пока
- For: для
- Function: функция
- Return: вернуть
- If: если
- Else: иначе
- Print: печать
- Input: входной
- List: список
- Continue: продолжать
- Break: перерыв

## Expressões Regulares

- INT -> ```[0-9]+```
- DEC -> ```[0-9]+.[0-9]+```
- ID  -> ```[a-zA-Z\u0400-\u04FF0-9]+```
- STRING -> ``` '"[a-zA-Z\u0400-\u04FF_*/@=^<>!...]+"' ```
- BOOLEAN -> ``` 'истинный | ложь' ```
- TYPE -> ``` 'интеграл' | 'десятичный' | 'строка' | 'логический' ```
- PLUS -> ``` '+' ```
- MINUS -> ``` '-' ```
- TIMES -> ``` '*' ```
- DIV -> ``` '/' ```
- POW -> ``` '^' ```
- MOD -> ``` '%' ```
- COMMA -> ``` ',' ```
- SEMICOLON -> ``` ';' ```
- ASSIGN -> ``` := ```
- PLUS_ASSIGN -> ``` '+:=' ```
- MINUS_ASSIGN -> ``` '-:=' ```
- TIMES_ASSIGN -> ``` '*:=' ```
- POW_ASSIGN -> ``` '^:=' ```
- GREATER -> ``` '>' ```
- LESS -> ``` '<' ```
- GREATER_EQUAL -> ``` '>=' ```
- LESS_EQUAL -> ``` '<=' ```
- EQUAL -> ``` '==' ```
- DIFFERENT -> ``` '!=' ``` 
- AND -> ``` '&' ```
- OR -> ``` '||' ```
- OPEN_BRACES -> ``` '{' ```
- CLOSE_BRACES -> ``` '}' ```
- OPEN_BRACKETS -> ``` '[' ```
- CLOSE_BRACKETS -> ``` ']' ```
- OPEN_PARENTHESIS -> ``` '(' ```
- CLOSE_PARENTHESIS -> ``` ')' ```
- OP_IF -> ``` 'если' ```
- OP_ELSE -> ``` 'иначе' ```
- OP_DO -> ``` 'делать' ```
- OP_WHILE -> ``` 'пока' ```
- OP_FOR -> ``` 'для' ```
- OP_FUNCTION -> ``` 'функция' ``` 
- OP_RETURN -> ``` 'вернуть' ```
- OP_PRINT -> ``` 'печать' ```
- OP_INPUT -> ``` 'входной' ```
- OP_CONTINUE -> ``` 'продолжать' ```
- OP_BREAK -> ``` 'перерыв' ```
- EOF -> ``` '$' ```



## Gramática 

### OBS: termos em caps lock são terminais
```

file: bloco EOF

bloco: comando* 

comando: atribuicao | condicional | laco_while | laco_for | funcao_def | retorno | comando_print | comando_input | comando_continue | comando_break

atribuicao: ID acessoOp operador_atrib expressao

operador_atrib: ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | TIMES_ASSIGN | POW_ASSIGN

acessoOp: OPEN_BRACKETS expressao CLOSE_BRACKETS

expressao: disjuncao

disjuncao: conjuncao (OR conjuncao)*

conjuncao: comparacao (AND comparacao)*

comparacao: aritmetica (operador_comp aritmetica)*

operador_comp: EQUAL | DIFFERENT | LESS | GREATER | LESS_EQUAL | GREATER_EQUAL

aritmetica: termo (operador_adi termo)*

operador_adi: PLUS | MINUS

termo: fator (operador_mult fator)*

operador_mult: TIMES | DIV | MOD

fator: base (POW base)*

base: primario chamadaOp

chamadaOp: OPEN_PARENTHESIS corpoLista CLOSE_PARENTHESIS

primario: INT | DEC | STRING | BOOLEAN | ID | lista | OPEN_PARENTHESIS expressao CLOSE_PARENTHESIS

lista: OPEN_BRACKETS corpoLista CLOSE_BRACKETS

corpoLista: (expressao (COMMA expressao)*)? 

condicional: OP_IF expressao OPEN_BRACES bloco CLOSE_BRACES senao

senao: OP_ELSE OPEN_BRACES bloco CLOSE_BRACES

laco_while: OP_WHILE expressao OPEN_BRACES bloco CLOSE_BRACES

laco_for: OP_FOR ID OP_DO expressao OPEN_BRACES bloco CLOSE_BRACES

funcao_def: OP_FUNCTION ID OPEN_PARENTHESIS parametros CLOSE_PARENTHESIS OPEN_BRACES bloco CLOSE_BRACES

parametros: (ID (COMMA ID)*)?

retorno: OP_RETURN expressao?

comando_print: OP_PRINT OPEN_PARENTHESIS corpoLista CLOSE_PARENTHESIS

comando_input: OP_INPUT OPEN_PARENTHESIS ID CLOSE_PARENTHESIS

comando_continue: OP_CONTINUE

comando_break: OP_BREAK

```