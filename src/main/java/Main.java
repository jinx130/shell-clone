import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static String[] parseArguments(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean singleQuotes = false;   // CLEANUP 8: renamed from inQuotes
        boolean hasToken = false;
        boolean doubleQuotes = false;
        // CLEANUP 5: "backslash" flag removed

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            // CLEANUP 5: the "if (backslash) { ... continue; }" block is gone

            if (c == '"') {
                doubleQuotes = !doubleQuotes;
            } else if (c == '\\' && !singleQuotes) {
                if (i + 1 < input.length()) {
                    current.append(input.charAt(i + 1));
                }
                i++;   // CLEANUP 5: skip the escaped char right here
            } else if (c == '\'' && !doubleQuotes) {
                singleQuotes = !singleQuotes;
                hasToken = true;
            } else if (c == ' ' && !singleQuotes && !doubleQuotes) {
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

    // CLEANUP 1: one PATH search used by both "type" and external commands
    static String findExecutable(String name) {
        for (String dir : System.getenv("PATH").split(":")) {
            String fullPath = dir + "/" + name;
            if (Files.isExecutable(Path.of(fullPath))) {
                return fullPath;
            }
        }
        return null;
    }

    // CLEANUP 2: running the process moved out of the PATH loop into its own method
    static void runExternal(String[] arguments) throws Exception {
        String command = arguments[0];

        if (findExecutable(command) == null) {
            System.out.println(command + ": command not found");
            return;
        }

        List<String> commandArg = new ArrayList<>();
        String filepath = null;
        String currarguments = null;   // remembers which operator we saw: >, 1> or 2>

        for (int i = 0; i < arguments.length; i++) {
            // CHANGE 1: arguments[i].equals("2>") instead of arguments.equals("2>")
            if (arguments[i].equals(">") || arguments[i].equals("1>") || arguments[i].equals("2>")) {
                if (i + 1 >= arguments.length) {
                    System.out.println("bash: syntax error near unexpected token `newline'");
                    return;
                }
                currarguments = arguments[i];
                filepath = arguments[i + 1];
                break;
            }
            commandArg.add(arguments[i]);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandArg);
        processBuilder.directory(new File(System.getProperty("user.dir")));

        if (filepath != null) {
            // same for 1> and 2>
            File outputFile = Path.of(System.getProperty("user.dir")).resolve(filepath).toFile();

            // CHANGE 2: parent folder check now runs for both 1> and 2>
            if (!outputFile.getParentFile().isDirectory()) {
                System.out.println("bash: " + filepath + ": No such file or directory");
                return;
            }

            if (currarguments.equals(">") || currarguments.equals("1>")) {
                processBuilder.redirectOutput(outputFile);                         // stdout goes to the file
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);     // stderr stays on screen
            } else {
                // CHANGE 3: the new 2> case
                processBuilder.redirectError(outputFile);                          // stderr goes to the file
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);    // stdout stays on screen
            }
        } else {
            processBuilder.inheritIO();
        }

        Process process = processBuilder.start();
        process.waitFor();
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String homeDirectory = System.getenv("HOME");   // CLEANUP 8: moved above the loop

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();

            String[] arguments = parseArguments(input);
            if (arguments.length == 0) {
                continue;
            }

            String command = arguments[0];
            if (command.equals("exit")) {
                break;
            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (command.startsWith("cd")) {
                String command2 = input.substring(2).trim();

                if (command2.startsWith("/")) {
                    Path path = Path.of(command2);   // CLEANUP 8: Path.of everywhere
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

                    String pwd = System.getProperty("user.dir");
                    int index = pwd.lastIndexOf('/');

                    int n = count;

                    for (int j = 1; j < n; j++) {
                        index = pwd.lastIndexOf('/', index - 1);
                    }

                    Path path = Path.of(pwd.substring(0, index) + command2.substring(i));
                    if (Files.isDirectory(path)) {
                        System.setProperty("user.dir", pwd.substring(0, index) + command2.substring(i));
                    } else {
                        System.out.println("cd: " + command2 + ": No such file or directory");
                    }
                } else if (command2.equals("~")) {
                    System.setProperty("user.dir", homeDirectory);
                } else if (command2.startsWith("./")) {
                    command2 = System.getProperty("user.dir") + "/" + command2.substring(2);
                    Path path = Path.of(command2);
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
                    // CLEANUP 1: uses findExecutable, no "found" flag
                    String fullPath = findExecutable(command1);
                    if (fullPath != null) {
                        System.out.println(command1 + " is " + fullPath);
                    } else {
                        System.out.println(command1 + ": not found");
                    }
                }
            } else {
                runExternal(arguments);   // CLEANUP 1 + 2: the whole old else block is now this one line
            }
        }
        sc.close();
    }
}