package semantico;

/**
 * Exceção lançada pelo analisador semântico da linguagem Императрица.
 */
public class SemanticException extends Exception {
    public SemanticException(String message) {
        super(message);
    }
}