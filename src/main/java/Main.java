
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
            String[] arguments = input.trim().split("\\s+");
            String command = arguments[0];
            String homeDirectory = System.getenv("HOME");
            if (command.equals("exit")) {
                break;
            }
            else if(command.equals("pwd"))
            {
                System.out.println(System.getProperty("user.dir"));
            }

            else if(command.startsWith("cd"))
            {
                String command2 = input.substring(2).trim();

                    if(command2.startsWith("/")) {
                        Path path = Paths.get(command2);
                        if (Files.isDirectory(path)) {
                            System.setProperty("user.dir", command2);
                        } else {
                            System.out.println("cd: " + command2 + ": No such file or directory");
                        }
                    }
                   else  if (command2.startsWith("../"))
                    {
                        int count=0;
                        int i;
                       for( i=0;i<command2.length();i+=3)
                       {
                           if(command2.substring(i,i+3).equals("../"))
                           {
                               count++;
                           }
                           else
                           {
                               break;
                           }
                       }
                       // new command will be count directories back and add command 2 substring

                        String pwd = System.getProperty("user.dir");
                        int index = pwd.lastIndexOf('/');

                        int n = count;

                        for (int j = 1; j < n; j++) {
                            index = pwd.lastIndexOf('/', index - 1);
                        }

                        Path path = Paths.get(pwd.substring(0,index)+command2.substring(i));
                        if(Files.isDirectory(path))
                        {
                            System.setProperty("user.dir",pwd.substring(0,index)+command2.substring(i));
                        }
                        else
                        {
                            System.out.println("cd: " + command2 + ": No such file or directory");
                        }
                    }
                    else if (command2.equals("~")) {
                        System.setProperty("user.dir", homeDirectory);
                    }
                    else if(command2.startsWith("./"))
                    {

                            command2 = System.getProperty("user.dir")+"/"+command2.substring(2);
                            Path path = Paths.get(command2);
                            if (Files.isDirectory(path)) {
                                System.setProperty("user.dir", command2);
                            } else {
                                System.out.println("cd: " + command2 + ": No such file or directory");
                            }
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
                input = input.substring(5).trim();
                StringBuffer stringBuffer = new StringBuffer();
                int i = 0;

                while (i < input.length()) {
                    while (i < input.length() && input.charAt(i) != ' ') {
                        if (input.charAt(i) == '\'') {
                            i++;
                            while (i < input.length() && input.charAt(i) != '\'') {
                                stringBuffer.append(input.charAt(i));
                                i++;
                            }
                            if (i < input.length()) {
                                i++;
                            }
                        } else {
                            stringBuffer.append(input.charAt(i));
                            i++;
                        }
                    }

                    if (i < input.length()) {
                        stringBuffer.append(' ');
                    }

                    while (i < input.length() && input.charAt(i) == ' ') {
                        i++;
                    }
                }

                System.out.println(stringBuffer);
               // System.out.println(stringBuffer);
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
                            commandArg.add(argument.replace("'", ""));
                        }

                        ProcessBuilder processBuilder = new ProcessBuilder();
                        processBuilder.command(commandArg);
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
