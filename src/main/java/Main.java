
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            String[] arguments = input.split(" ");
            String command = arguments[0];
            String currDirectory = System.getProperty("user.dir");
            if (command.equals("exit")) {
                break;
            }
            else if(command.equals("pwd"))
            {
                System.out.println(currDirectory);
            }
            else if(command.startsWith("cd"))
            {
                String command2 = input.substring(2).trim();

                    Path path = Paths.get(command2);
                    if(Files.isDirectory(path))
                    {
                        System.setProperty("user.dir",command2);
                    }
                    else
                    {
                        System.out.println("cd: "+command2+": No such file or directory");
                    }
            }
            else if (command.equals("type")) {
                String command1 = input.substring(5).trim();

                if (command1.equals("echo") || command1.equals("exit") || command1.equals("type") || command1.equals("pwd")) {
                    System.out.println(command1 + " is a shell builtin");
                } else {
                    String path = System.getenv("PATH");
                    String[] directories = path.split(":");
                    boolean found = false;

                    for (String dir : directories) {
                        String fullPath = dir + "/" + command1;

                        if (Files.isExecutable(Path.of(fullPath))) {
                            System.out.println(command1 + " is " + fullPath);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println(command1 + ": not found");
                    }
                }
            } else if (command.equals("echo")) {
                System.out.println(input.substring(5));
            }


            else {
                String path = System.getenv("PATH");
                String[] directories = path.split(":");
                boolean found = false;

                for (String dir : directories) {
                    String fullPath = dir + "/" + command;

                    if (Files.isExecutable(Path.of(fullPath))) {
                        List<String> commandArg = new ArrayList<>();

                        for (String argument : arguments) {
                            commandArg.add(argument);
                        }

                        ProcessBuilder processBuilder = new ProcessBuilder(commandArg);
                        processBuilder.inheritIO();

                        Process process = processBuilder.start();
                        process.waitFor();

                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println(command + ": command not found");
                }
            }
        }

        sc.close();
    }
}
