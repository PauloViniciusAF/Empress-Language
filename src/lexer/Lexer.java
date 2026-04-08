import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

// ========== Token Class ==========
class Token {
    String tipo;
    String lexema;

    public Token(String tipo, String lexema){
        this.lexema = lexema;
        this.tipo = tipo;
    }

    @Override
    public String toString(){
        return "<" + tipo + ", " + lexema + ">";
    }
}

// ========== AFD Abstract Class ==========
abstract class AFD {
    public abstract Token evaluate(CharacterIterator code);

    public boolean isTokenSeparator(CharacterIterator code){
        return code.current() == ' ' ||
        code.current() == '+' ||
        code.current() == '-' ||
        code.current() == '*' ||
        code.current() == '/' ||
        code.current() == '(' ||
        code.current() == ')' ||
        code.current() == '%' ||
        code.current() == '^' ||
        code.current() == '=' ||
        code.current() == '\n' ||
        code.current() == CharacterIterator.DONE;
    }
}

// ========== Number Class ==========
class Number extends AFD {
    
    @Override
    public Token evaluate(CharacterIterator code){

        if (Character.isDigit(code.current())){
            String number = readNumber(code);
            if(code.current() == '.'){
                number += '.';
                code.next();
                number += readNumber(code);
            }
            if (isTokenSeparator(code)){
                String tokenType = number.contains(".") ? "DEC" : "INT";
                return new Token(tokenType, number);
            }
        }
        return null;
    }

    private String readNumber (CharacterIterator code){
        String number = "";

        while (Character.isDigit(code.current())){
            number += code.current();
            code.next();
        }
        return number;
    }
}

// ========== Alphabet Class ==========
class Alphabet extends AFD {
    
    @Override
    public Token evaluate(CharacterIterator code){

        if (Character.isAlphabetic(code.current())) {
            
            String number = readLetter(code);
            if(code.current() == '.'){
                number += '.';
                code.next();
                number += readLetter(code);
            }
            if (isTokenSeparator(code)){
                return new Token("ID", number);
            }
        }
        return null;
    }

    private String readLetter (CharacterIterator code){
        String number = "";

        while (Character.isAlphabetic(code.current())){
            number += code.current();
            code.next();
        }
        return number;
    }
}


// ========== String Class ==========
class isString extends AFD {

    @Override
    public Token evaluate(CharacterIterator code){

        if (code.current() == '"'){
            code.next();
            String string = readString(code);
            if (string != null) {
                code.next();
                return new Token("STR", string);
            }
        }
        return null;
        
    }

    private String readString (CharacterIterator code){
        String string = "";

        while (code.current() != '"'){
            if (code.current() == '\n' || code.current() == CharacterIterator.DONE){
                return null;
            }
            string += code.current();
            code.next();
        }
        return string;
    }
}

// ========== Special Characters Class ==========
class specialCharacters extends AFD {

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


// ========== ReservedWords Class ==========
class reservedWords {
    private static final java.util.Map<String, String> RESERVED = new java.util.HashMap<>();

    static {
        // Language Keywords with proper token types
        RESERVED.put("если", "OP_IF");
        RESERVED.put("иначе", "OP_ELSE");
        RESERVED.put("делать", "OP_DO");
        RESERVED.put("для", "OP_FOR");
        RESERVED.put("пока", "OP_WHILE");
        RESERVED.put("функция", "OP_FUNCTION");
        RESERVED.put("вернуть", "OP_RETURN");
        RESERVED.put("печать", "OP_PRINT");
        RESERVED.put("входной", "OP_INPUT");
        RESERVED.put("истинный", "BOOLEAN");
        RESERVED.put("ложь", "BOOLEAN");
        RESERVED.put("целое число", "TYPE");
        RESERVED.put("строка", "TYPE");
        RESERVED.put("десятичный", "TYPE");
        RESERVED.put("логический", "TYPE");
    }

    public static boolean isReserved(String word) {
        return RESERVED.containsKey(word);
    }

    public static String getTokenType(String word) {
        return RESERVED.get(word);
    }

    public static Set<String> getAll() {
        return new HashSet<>(RESERVED.keySet());
    }
}

// ========== IdentifierStart Class ==========
class IdentifierStart extends AFD {
    
    @Override
    public Token evaluate(CharacterIterator code){

        if (Character.isJavaIdentifierStart(code.current())){
            String number = readLetter(code);
            if(code.current() == '.'){
                number += '.';
                code.next();
                number += readLetter(code);
            }
            if (isTokenSeparator(code)){
                return new Token("ID", number);
            }
        }
        return null;
    }

    private String readLetter (CharacterIterator code){
        String number = "";

        while (Character.isJavaIdentifierPart(code.current())){
            number += code.current();
            code.next();
        }
        return number;
    }
}

// ========== Lexer Class ==========
public class Lexer {
    private List<Token> tokens;
    private List<AFD> afds;
    private CharacterIterator code;

    public Lexer(String code){
        tokens = new ArrayList<>();
        this.code = new StringCharacterIterator(code);
        afds = new ArrayList<>();
        afds.add(new Number());
        afds.add(new Alphabet());
        afds.add(new IdentifierStart());
        afds.add(new isString());
        afds.add(new specialCharacters());
    }

    public void skipWhiteSpace(){
        while (code.current() == ' ' || code.current() == '\n'){
            code.next();
        }
    }

    public boolean skipComment(){
        boolean found = false;
        int pos = code.getIndex();

        if (code.current() == '@'){
            found = true;
            while(code.current() != '\n' && code.current() != CharacterIterator.DONE){
                code.next();
            }
            if (code.current() == '\n') {
                code.next();
            }
        }
        else{
            code.setIndex(pos);
        }
        return found;
    }

    private Token searchNextToken(){
        int pos = code.getIndex();
        for (AFD afd : afds){
            Token t = afd.evaluate(code);
            if(t != null) return t;
            code.setIndex(pos);
        }
        return null;
    }

    public void error(){
        throw new RuntimeException("Error: token not recognized: " + code.current());
    }

    public List<Token> getTokens(){
        Token t;
        do {
            skipWhiteSpace();
            skipComment();
            t = searchNextToken();
            if (t == null) error();
            
            // Check if ID token is a reserved word
            if (t.tipo.equals("ID") && reservedWords.isReserved(t.lexema)) {
                String tokenType = reservedWords.getTokenType(t.lexema);
                t = new Token(tokenType, t.lexema);
            }
            
            tokens.add(t);
        } while (!t.tipo.equals("EOF"));
        return tokens;
    }
}
