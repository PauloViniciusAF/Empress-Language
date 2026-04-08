package lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ========== ReservedWords Class ==========
public class reservedWords {
    private static final Map<String, String> RESERVED = new HashMap<>();

    static {
        // Language Keywords with proper token types
        RESERVED.put("если", "OP_IF");                  //if 
        RESERVED.put("иначе", "OP_ELSE");               //else       
        RESERVED.put("делать", "OP_DO");                //do
        RESERVED.put("для", "OP_FOR");                  //for
        RESERVED.put("пока", "OP_WHILE");               //while
        RESERVED.put("функция", "OP_FUNCTION");         //function
        RESERVED.put("вернуть", "OP_RETURN");           //return
        RESERVED.put("печать", "OP_PRINT");             //print
        RESERVED.put("входной", "OP_INPUT");            //input
        RESERVED.put("продолжать", "OP_INPUT");         //continue
        RESERVED.put("перерыв", "OP_INPUT");            //break
        RESERVED.put("истинный", "BOOLEAN");            //true
        RESERVED.put("ложь", "BOOLEAN");                //false
        RESERVED.put("интеграл", "TYPE");               //integral
        RESERVED.put("строка", "TYPE");                 //string
        RESERVED.put("десятичный", "TYPE");             //decimal
        RESERVED.put("логический", "TYPE");             //boolean
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
