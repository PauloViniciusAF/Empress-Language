package lexer;

import java.text.CharacterIterator;

// ========== Number Class ==========
public class Number extends AFD {
    
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
