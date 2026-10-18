
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static String[] parseArguments(String input) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean doubleQuotes = false;
        boolean hasToken = false;
        boolean backslash = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (backslash) {
                current.append(c);
                backslash = false;
                hasToken = true;
            } else if (c == '\\' && !inQuotes) {
                backslash = true;
            } else if (c == '"' && !inQuotes) {
                doubleQuotes = !doubleQuotes;
                hasToken = true;
            } else if (c == '\'' && !doubleQuotes) {
                inQuotes = !inQuotes;
                hasToken = true;
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

        if (backslash) {
            current.append('\\');
        }

        if (hasToken) {
            args.add(current.toString());
        }

        return args.toArray(new String[0]);
    }

    static Path resolvePath(String filepath) {
        Path path = Paths.get(filepath);

        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }

        return path.normalize();
    }

    static int redirectionIndex(String[] arguments) {
        for (int i = 1; i < arguments.length; i++) {
            if (arguments[i].equals(">") || arguments[i].equals("1>")) {
                return i;
            }
        }

        return -1;
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();

            String[] arguments = parseArguments(input);

            if (arguments.length == 0) {
                continue;
            }

            String command = arguments[0];
            String homeDirectory = System.getenv("HOME");

            if (command.equals("exit")) {
                break;

            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));

            } else if (command.equals("cd")) {
                String command2 = arguments.length > 1
                        ? arguments[1] : homeDirectory;

                Path path;

                if (command2.equals("~")) {
                    path = Paths.get(homeDirectory);
                } else {
                    path = Paths.get(command2);

                    if (!path.isAbsolute()) {
                        path = Paths.get(System.getProperty("user.dir"))
                                .resolve(path);
                    }
                }

                path = path.normalize();

                if (Files.isDirectory(path)) {
                    System.setProperty("user.dir", path.toAbsolutePath().toString());
                } else {
                    System.out.println("cd: " + command2
                            + ": No such file or directory");
                }

            } else if (command.equals("type")) {
                if (arguments.length < 2) {
                    continue;
                }

                String command1 = arguments[1];

                if (command1.equals("echo") || command1.equals("exit")
                        || command1.equals("type") || command1.equals("pwd")
                        || command1.equals("cd")) {
                    System.out.println(command1 + " is a shell builtin");
                } else {
                    String path = System.getenv("PATH");
                    boolean found = false;

                    for (String dir : path.split(":")) {
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
                int redirectIndex = redirectionIndex(arguments);
                StringBuilder output = new StringBuilder();

                int end = redirectIndex == -1
                        ? arguments.length : redirectIndex;

                for (int i = 1; i < end; i++) {
                    if (i > 1) {
                        output.append(' ');
                    }
                    output.append(arguments[i]);
                }

                if (redirectIndex != -1) {
                    if (redirectIndex + 1 >= arguments.length) {
                        System.out.println("Syntax error: expected file path after "
                                + arguments[redirectIndex]);
                        continue;
                    }

                    Path outputPath = resolvePath(arguments[redirectIndex + 1]);

                    Files.writeString(
                            outputPath,
                            output + System.lineSeparator()
                    );
                } else {
                    System.out.println(output);
                }

            } else {
                String path = System.getenv("PATH");
                boolean found = false;

                for (String dir : path.split(":")) {
                    String fullPath = dir + "/" + command;

                    if (Files.isExecutable(Path.of(fullPath))) {
                        List<String> commandArg = new ArrayList<>();
                        int redirectIndex = redirectionIndex(arguments);

                        int end = redirectIndex == -1
                                ? arguments.length : redirectIndex;

                        for (int i = 0; i < end; i++) {
                            commandArg.add(arguments[i]);
                        }

                        if (redirectIndex != -1
                                && redirectIndex + 1 >= arguments.length) {
                            System.out.println("Syntax error: expected file path after "
                                    + arguments[redirectIndex]);
                            found = true;
                            break;
                        }

                        ProcessBuilder processBuilder =
                                new ProcessBuilder(commandArg);

                        processBuilder.directory(
                                new File(System.getProperty("user.dir"))
                        );

                        if (redirectIndex != -1) {
                            Path outputPath =
                                    resolvePath(arguments[redirectIndex + 1]);

                            processBuilder.redirectOutput(outputPath.toFile());
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
            }
        }

        sc.close();
    }
}
