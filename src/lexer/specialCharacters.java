package lexer;

import java.text.CharacterIterator;

// ========== Special Characters Class ==========
public class specialCharacters extends AFD {

    @Override
    public Token evaluate(CharacterIterator code){
        int pos = code.getIndex();
        
        switch (code.current()){
            // Parenthesis, Braces and Brackets
            case '[':
                code.next();
                return new Token("OPEN_BRACKET", "[");
            case ']':
                code.next();
                return new Token("CLOSE_BRACKET", "]");
            case '{':
                code.next();
                return new Token("OPEN_BRACES", "{");
            case '}':
                code.next();
                return new Token("CLOSE_BRACES", "}");
            case '(':
                code.next();
                return new Token("OPEN_PARENTHESIS", "(");
            case ')':
                code.next();
                return new Token("CLOSE_PARENTHESIS", ")");
            
            // Arithmetic Operators
            case '+':
                code.next();
                if (code.current() == ':') {
                    code.next();
                    if (code.current() == '=') {
                        code.next();
                        return new Token("PLUS_ASSIGN", "+:=");
                    }
                    code.setIndex(pos + 1);
                }
                return new Token("PLUS", "+");
            case '-':
                code.next();
                if (code.current() == ':') {
                    code.next();
                    if (code.current() == '=') {
                        code.next();
                        return new Token("MINUS_ASSIGN", "-:=");
                    }
                    code.setIndex(pos + 1);
                }
                return new Token("MINUS", "-");
            case '*':
                code.next();
                if (code.current() == ':') {
                    code.next();
                    if (code.current() == '=') {
                        code.next();
                        return new Token("TIMES_ASSIGN", "*:=");
                    }
                    code.setIndex(pos + 1);
                }
                return new Token("TIMES", "*");
            case '/':
                code.next();
                return new Token("DIV", "/");
            case '%':
                code.next();
                return new Token("MOD", "%");
            case '^':
                code.next();
                if (code.current() == ':') {
                    code.next();
                    if (code.current() == '=') {
                        code.next();
                        return new Token("POW_ASSIGN", "^:=");
                    }
                    code.setIndex(pos + 1);
                }
                return new Token("POW", "^");
            
            // Assignment and Comparison
            case ':':
                code.next();
                if (code.current() == '=') {
                    code.next();
                    return new Token("ASSIGN", ":=");
                }
                code.setIndex(pos);
                return null;
            case '=':
                code.next();
                if (code.current() == '=') {
                    code.next();
                    return new Token("EQUAL", "==");
                }
                return new Token("ASSIGN", "=");
            case '!':
                code.next();
                if (code.current() == '=') {
                    code.next();
                    return new Token("DIFFERENT", "!=");
                }
                code.setIndex(pos);
                return null;
            case '<':
                code.next();
                if (code.current() == '=') {
                    code.next();
                    return new Token("LESS_EQUAL", "<=");
                }
                return new Token("LESS", "<");
            case '>':
                code.next();
                if (code.current() == '=') {
                    code.next();
                    return new Token("GREATER_EQUAL", ">=");
                }
                return new Token("GREATER", ">");
            
            // Logical Operators
            case '&':
                code.next();
                return new Token("AND", "&");
            case '|':
                code.next();
                if (code.current() == '|') {
                    code.next();
                    return new Token("OR", "||");
                }
                code.setIndex(pos);
                return null;
            
            // Separator
            case ',':
                code.next();
                return new Token("COMMA", ",");
            case ';':
                code.next();
                return new Token("SEMICOLON", ";");

            // EOF
            case CharacterIterator.DONE:
                return new Token("EOF", "$");            
            default:
                return null;
        }
    }
}
