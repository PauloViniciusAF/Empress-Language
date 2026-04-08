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
- ID  -> ```[a-zA-Z\u0400-\u04FF]+```
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
```
- programa: bloco
- bloco: cmd bloco |  ε
- cmd: cmdId | cmdIf
- cmdId: ID acesso complemento
- cmdIf: <OP_IF> expressao '{' bloco '}'
- cmdElse: <OP_ELSE> '{' bloco '}'
- atribNormal: <ASSIGN> valor

```