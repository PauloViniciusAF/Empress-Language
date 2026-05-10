import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lexer.Lexer;
import lexer.Token;
import sintatico.*;

// ========== Main Method ==========
public class Main {
    public static void main(String[] args) throws IOException {
        
        List<Token> tokens = null;
        String sourceCode = null;
        boolean showTree = false;
        String fileName = null;

        for (String arg : args) {
            if (arg.equals("--tree")) {
                showTree = true;
            } else if (fileName == null) {
                fileName = arg;
            }
        }

        if (fileName == null) {
            System.err.println("Uso: java Main <arquivo.emp> [--tree]");
            System.exit(1);
        }
        
        File myFile = new File(fileName);

        try(Scanner scanf = new Scanner(myFile)){
            StringBuilder code = new StringBuilder();
            
            while (scanf.hasNextLine()){
                code.append(scanf.nextLine()).append("\n");
            }
            
            sourceCode = code.toString();
            Lexer lexer = new Lexer(sourceCode);
            tokens = lexer.getTokens();

            // //----------PRINT TOKENS------------        
            // for(Token token : tokens){
            //     System.out.println(token);
            // }

            scanf.close();
        } catch (FileNotFoundException e){
            System.out.println("Erro");
            e.printStackTrace();        
        }
        Parser parser = new Parser(tokens, myFile.getName(), sourceCode, showTree);
        parser.main();

    }
}
