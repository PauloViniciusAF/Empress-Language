package lexer;

import java.text.CharacterIterator;

// ========== Alphabet Class ==========
public class Alphabet extends AFD {
    
    @Override
    public Token evaluate(CharacterIterator code){

        if (Character.isAlphabetic(code.current())) {
            
            String text = readLetter(code);
            if(code.current() == '.'){
                text += '.';
                code.next();
                text += readLetter(code);
            }
            return new Token("ID", text);
        }
        return null;
    }

    private String readLetter (CharacterIterator code){
        String text = "";

        while (Character.isAlphabetic(code.current())){
            text += code.current();
            code.next();
        }
        return text;
    }
}
