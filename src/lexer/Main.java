import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;



// ========== Main Method ==========
public class Main {
    public static void main(String[] args) throws IOException {
        
        List<Token> tokens = null;
        

        //Scanning a file
        File myFile = new File ("example.emp");

        try(Scanner scanf = new Scanner(myFile)){
            
            while (scanf.hasNextLine()){
                String data = scanf.nextLine();
                Lexer lexer = new Lexer(data);
                tokens = lexer.getTokens();
            
                for(Token token : tokens){
                    System.out.println(token);
                }

            }
            scanf.close();
        } catch (FileNotFoundException e){
            System.out.println("Erro");
            e.printStackTrace();        
        }

    }
}