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
        

        //Scanning a file
        File myFile = new File ("example.emp");

        try(Scanner scanf = new Scanner(myFile)){
            StringBuilder code = new StringBuilder();
            
            while (scanf.hasNextLine()){
                code.append(scanf.nextLine()).append("\n");
            }
            
            Lexer lexer = new Lexer(code.toString());
            tokens = lexer.getTokens();

            //----------PRINT TOKENS------------        
            for(Token token : tokens){
                System.out.println(token);
            }

            scanf.close();
        } catch (FileNotFoundException e){
            System.out.println("Erro");
            e.printStackTrace();        
        }
        Parser parser = new Parser(tokens);
        parser.main();

    }
}
