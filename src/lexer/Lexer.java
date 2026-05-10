package lexer;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

// ========== Lexer Class ==========
public class Lexer {
    private List<Token> tokens;
    private List<AFD> afds;
    private CharacterIterator code;
    private int currentLine = 1;  // Rastrear linha atual

    public Lexer(String code){
        tokens = new ArrayList<>();
        this.code = new StringCharacterIterator(code);
        this.currentLine = 1;
        afds = new ArrayList<>();
        afds.add(new Number());
        afds.add(new Alphabet());
        afds.add(new IdentifierStart());
        afds.add(new isString());
        afds.add(new specialCharacters());
    }

    public void skipWhiteSpace(){
        while (code.current() == ' ' || code.current() == '\n'){
            if (code.current() == '\n') {
                currentLine++;
            }
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
            
            // Adicionar linha ao token
            t.linha = currentLine;
            
            // Check if ID token is a reserved word
            if (t.tipo.equals("ID") && reservedWords.isReserved(t.lexema)) {
                String tokenType = reservedWords.getTokenType(t.lexema);
                t = new Token(tokenType, t.lexema, currentLine);
            }
            
            tokens.add(t);
        } while (!t.tipo.equals("EOF"));
        return tokens;
    }
}

