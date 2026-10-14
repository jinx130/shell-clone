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
            boolean x=false;
            if (command.startsWith("type")) {
               String command1 = command.substring(4).trim();
                 if(command1.equals("echo")||command1.equals("exit")||command1.equals("type"))
                 {

                    System.out.println(command1 + " is a shell builtin");
                    x = true;
                }
            }
            if(command.substring(0,4).equals("echo"))
            {
                System.out.println(command.substring(5));
                x=true;
            }
            if(Objects.equals(command, "exit"))break;
            if(!x) {
                System.out.println(command + ": command not found");
            }
            System.out.print("$ ");
        }
    }
}
