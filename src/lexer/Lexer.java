import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

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

// ========== MathOperator Class ==========
class MathOperator extends AFD {
    
    @Override
    public Token evaluate(CharacterIterator code){

        switch (code.current()){
            case '+':
                code.next();
                return new Token("PLUS", "+");
            case '-':
                code.next();
                return new Token("MINUS", "-");
            case '*':
                code.next();
                return new Token("TIMES", "*");
            case '/':
                code.next();
                return new Token("DIVISION", "/");
            case '%':
                code.next();
                return new Token("MOD", "%");
            case '^':
                code.next();
                return new Token("POWER", "^");

            //EOF
            case CharacterIterator.DONE:
                return new Token("EOF", "$");            
            default:
                return null;
        }
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

// ========== String Class ==========
class especialCharacter extends AFD {

    @Override
    public Token evaluate(CharacterIterator code){

        switch (code.current()){
            case '(':
                code.next();
                return new Token("OPEN_PARENTHESIS", "(");
            case ')':
                code.next();
                return new Token("CLOSE_PARENTHESIS", ")");
            case '*':
                code.next();
                return new Token("TIMES", "*");
            case '/':
                code.next();
                return new Token("DIVISION", "/");
            case '%':
                code.next();
                return new Token("MOD", "%");
            case '^':
                code.next();
                return new Token("POWER", "^");

            //EOF
            case CharacterIterator.DONE:
                return new Token("EOF", "$");            
            default:
                return null;
        }
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
                return new Token("IDS", number);
            }
        }
        return null;
    }

    private String readLetter (CharacterIterator code){
        String number = "";

        while (Character.isJavaIdentifierStart(code.current())){
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
        afds.add(new MathOperator());
        afds.add(new Number());
        afds.add(new Alphabet());
        afds.add(new IdentifierStart());
        afds.add(new isString());
        afds.add(new especialCharacter());
    }

    public void skipWhiteSpace(){
        while (code.current() == ' ' || code.current() == '\n'){
            code.next();
        }
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
            t = searchNextToken();
            if (t == null) error();
            tokens.add(t);
        } while (!t.tipo.equals("EOF"));
        return tokens;
    }
}
