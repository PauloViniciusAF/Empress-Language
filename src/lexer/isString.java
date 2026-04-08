package lexer;

import java.text.CharacterIterator;

// ========== String Class ==========
public class isString extends AFD {

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
