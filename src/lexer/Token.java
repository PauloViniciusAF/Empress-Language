package lexer;

// ========== Token Class ==========
public class Token {
    public String tipo;
    public String lexema;
    public int linha;

    public Token(String tipo, String lexema){
        this.lexema = lexema;
        this.tipo = tipo;
        this.linha = 1;  // Padrão é linha 1
    }

    public Token(String tipo, String lexema, int linha){
        this.lexema = lexema;
        this.tipo = tipo;
        this.linha = linha;
    }

    @Override
    public String toString(){
        return "<" + tipo + ", " + lexema + ">";
    }
}
