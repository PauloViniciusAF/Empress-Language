package lexer;

import java.text.CharacterIterator;

// ========== IdentifierStart Class ==========
public class IdentifierStart extends AFD {
    
    @Override
    public Token evaluate(CharacterIterator code){

        if (Character.isJavaIdentifierStart(code.current())){
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

        while (Character.isJavaIdentifierPart(code.current())){
            text += code.current();
            code.next();
        }
        return text;
    }
}
