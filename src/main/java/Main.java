import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String command = sc.nextLine();

            if (command.equals("exit")) {
                break;
            } else if (command.startsWith("type ")) {
                String command1 = command.substring(5).trim();

                if (command1.equals("echo") || command1.equals("exit") || command1.equals("type")) {
                    System.out.println(command1 + " is a shell builtin");
                }
                else
                {
                    // check if not print
                    String path = System.getenv("PATH");
                    String []directories = path.split(":");
                    for(String dir:directories)
                    {
                        String fullPath = dir+"/"+command1;
                        if(Files.isExecutable(Path.of(fullPath)))
                        {
                            System.out.println(command1 + " is " + fullPath);
                            break;
                        }
                    }
                }
            } else if (command.startsWith("echo ")) {
                System.out.println(command.substring(5));
            } else {
                System.out.println(command + ": command not found");
            }
        }

        sc.close();
    }
}