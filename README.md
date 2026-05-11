# Императрица (Imperatriz) - Linguagem feita em Java para C

## Instalação 

Acesse o link ![google.com](https://google.com) para instalar e criar seu arquivo .emp

## Como executar

```bash 
./императрица <arquivo>.emp
```

## Argumentos opcionais

```bash 
--tree      //Retorna a AST no terminal
```


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
- INCREMENT -> ``` '++' ```
- MINUS -> ``` '-' ```
- DECREMENT -> ``` '--' ```
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

### OBS: termos em caps lock são terminais, 'ε' representa produção vazia

```
file → bloco EOF

bloco → cmd bloco | ε

cmd → cmdIf | cmdFor | cmdWhile | cmdReturn | cmdDefFunc | cmdPrint | cmdInput | cmdID | OP_CONTINUE | OP_BREAK

cmdID → tipo ID acessoListaOp complemento SEMICOLON | ID acessoListaOp complemento SEMICOLON

acessoListaOp → acessoLista acessoListaOp | ε

acessoLista → OPEN_BRACKETS expressaoAritmetica CLOSE_BRACKETS

complemento → ASSIGN valor | operadorAssignOp valor | OPEN_PARENTHESIS corpoLista CLOSE_PARENTHESIS | ε

operadorAssignOp → PLUS_ASSIGN | MINUS_ASSIGN | TIMES_ASSIGN | DIV_ASSIGN | MOD_ASSIGN | POW_ASSIGN

valor → expressaoLogica | lista | cmdPrint | cmdInput

expressaoLogica → expressaoRelacional (AND expressaoRelacional)* | expressaoRelacional (OR expressaoRelacional)*

expressaoRelacional → expressaoAritmetica (opComparacao expressaoAritmetica)*

opComparacao → GREATER | LESS | EQUAL | DIFFERENT | GREATER_EQUAL | LESS_EQUAL

expressaoAritmetica → termo (opAdicao termo)*

opAdicao → PLUS | MINUS

termo → fator (opMultiplicacao fator)*

opMultiplicacao → TIMES | DIV | MOD

fator → elemento (POW elemento)*

elemento → INCREMENT ID X
         | DECREMENT ID X
         | ID X
         | INT
         | FLOAT
         | STR
         | BOOL
         | OPEN_PARENTHESIS expressaoLogica CLOSE_PARENTHESIS

X → composicao X | INCREMENT | DECREMENT | ε

composicao → OPEN_BRACKETS expressaoAritmetica CLOSE_BRACKETS acessoListaOp | OPEN_PARENTHESIS corpoLista CLOSE_PARENTHESIS

corpoLista → valor entradaLista | ε

entradaLista → COMMA valor entradaLista | ε

lista → OPEN_BRACKETS corpoLista CLOSE_BRACKETS

cmdIf → OP_IF valor OPEN_BRACES bloco CLOSE_BRACES cmdElse

cmdElse → OP_ELSE OPEN_BRACES bloco CLOSE_BRACES | ε

cmdWhile → OP_WHILE valor OPEN_BRACES bloco CLOSE_BRACES

cmdFor → OP_FOR OPEN_PARENTHESIS variavelFor SEMICOLON expressaoRelacional SEMICOLON expressaoAritmetica CLOSE_PARENTHESIS OPEN_BRACES bloco CLOSE_BRACES

variavelFor → tipo ID complemento | ID complemento

cmdDefFunc → OP_FUNCTION ID OPEN_PARENTHESIS listaParametros CLOSE_PARENTHESIS OPEN_BRACES bloco CLOSE_BRACES

listaParametros → ID entradaListaParam | ε

entradaListaParam → COMMA ID entradaListaParam | ε

cmdReturn → OP_RETURN valorRetorno

valorRetorno → valor | ε

cmdPrint → OP_PRINT OPEN_PARENTHESIS corpoLista CLOSE_PARENTHESIS SEMICOLON

cmdInput → OP_INPUT OPEN_PARENTHESIS corpoLista CLOSE_PARENTHESIS SEMICOLON

tipo → INT_TYPE | FLOAT_TYPE | BOOL_TYPE | STR_TYPE

id → ID
```




