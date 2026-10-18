import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    // CHANGE 1: new helper that splits input into arguments and respects single quotes
    static String[] parseArguments(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean hasToken = false;
        boolean doubleQuotes = false;
        boolean backslash = false;

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            if (backslash) {
                backslash = !backslash;
                continue;
            }
            if (c == '"') {
                doubleQuotes = !doubleQuotes;
            } else if (c == '\\' && !inQuotes) {
                backslash = true;
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                }
            } else if (c == '\'' && !doubleQuotes) {
                inQuotes = !inQuotes;   // toggle quote mode, the quote itself is not added
                hasToken = true;        // so '' still counts as an argument
            } else if (c == ' ' && !inQuotes && !doubleQuotes) {
                if (hasToken) {
                    args.add(current.toString());
                    current.setLength(0);
                    hasToken = false;
                }
            } else {
                current.append(c);
                hasToken = true;
            }
        }

        if (hasToken) {
            args.add(current.toString());
        }

        return args.toArray(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();

            // CHANGE 2: use the new parser instead of input.split(" ")
            String[] arguments = parseArguments(input);
            if (arguments.length == 0) {
                continue;   // empty line, just show the prompt again
            }

            String command = arguments[0];
            String homeDirectory = System.getenv("HOME");
            if (command.equals("exit")) {
                break;
            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (command.startsWith("cd")) {
                String command2 = input.substring(2).trim();

                if (command2.startsWith("/")) {
                    Path path = Paths.get(command2);
                    if (Files.isDirectory(path)) {
                        System.setProperty("user.dir", command2);
                    } else {
                        System.out.println("cd: " + command2 + ": No such file or directory");
                    }
                } else if (command2.startsWith("../")) {
                    int count = 0;
                    int i;
                    for (i = 0; i < command2.length(); i += 3) {
                        if (command2.substring(i, i + 3).equals("../")) {
                            count++;
                        } else {
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

                    Path path = Paths.get(pwd.substring(0, index) + command2.substring(i));
                    if (Files.isDirectory(path)) {
                        System.setProperty("user.dir", pwd.substring(0, index) + command2.substring(i));
                    } else {
                        System.out.println("cd: " + command2 + ": No such file or directory");
                    }
                } else if (command2.equals("~")) {
                    System.setProperty("user.dir", homeDirectory);
                } else if (command2.startsWith("./")) {

                    command2 = System.getProperty("user.dir") + "/" + command2.substring(2);
                    Path path = Paths.get(command2);
                    if (Files.isDirectory(path)) {
                        System.setProperty("user.dir", command2);
                    } else {
                        System.out.println("cd: " + command2 + ": No such file or directory");
                    }
                }
            } else if (command.equals("type")) {
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
                // CHANGE 3: the parser already handled the quotes, so just join the args with a single space
                StringBuilder output = new StringBuilder();
                for (int k = 1; k < arguments.length; k++) {
                    if (k > 1) {
                        output.append(' ');
                    }
                    output.append(arguments[k]);
                }
                System.out.println(output);
            } else {
                String path = System.getenv("PATH");
                String[] directories = path.split(":");
                boolean found = false;

                for (String dir : directories) {
                    String fullPath = dir + "/" + command;

                    if (Files.isExecutable(Path.of(fullPath))) {
                        List<String> commandArg = new ArrayList<>();
                        String filepath = "";
                        boolean redirect = false;

                        for (int i = 0; i < arguments.length; i++) {
                            if (arguments[i].equals(">") || arguments[i].equals("1>")) {
                                if (i + 1 >= arguments.length) {
                                    System.out.println("Syntax error: expected file path after " + arguments[i]);
                                    redirect = true;
                                    break;
                                }

                                filepath = arguments[++i];
                                redirect = true;
                                break;
                            }

                            commandArg.add(arguments[i]);
                        }

                        if (redirect && filepath.isEmpty()) {
                            break;
                        }

                        ProcessBuilder processBuilder = new ProcessBuilder(commandArg);
                        processBuilder.directory(new File(System.getProperty("user.dir")));

                        if (redirect) {
                            processBuilder.redirectOutput(new File(filepath));
                        } else {
                            processBuilder.inheritIO();
                        }

                        Process process = processBuilder.start();
                        process.waitFor();

                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println(command + ": command not found");
                }
//            for(int i=0;i< arguments.length;i++)
//            {
//                System.out.println(arguments[i] + " " + i);
//            }
            }

            sc.close();
        }
    }
}