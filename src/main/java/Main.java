import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage

        Scanner sc = new Scanner(System.in);
        System.out.print("$ ");
        while(true) {

            String command;
            command = sc.nextLine();
            if(command.substring(0,4).equals("echo"))
            {
                System.out.println(command.substring(5));
                System.out.print("$ ");
                continue;
            }
            if(Objects.equals(command, "exit"))break;
            System.out.println(command + ": command not found");
            System.out.print("$ ");
        }
    }
}
