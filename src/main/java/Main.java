import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args)  {
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
                    boolean x=false;
                    for(String dir:directories)
                    {
                        String fullPath = dir+"/"+command1;
                        if(Files.isExecutable(Path.of(fullPath)))
                        {
                            // how to execute it  ?
                            String input;
                            input = sc.nextLine();
                            String[] arg = input.trim().split("\\s+");

                            String[] command_arg = new String[args.length + 1];
                            command_arg[0] = command1;
                            System.arraycopy(args, 1, command_arg, 1, arg.length - 1);

                            try {
                                ProcessBuilder pb = new ProcessBuilder(command_arg);
                                pb.redirectErrorStream(true);
                                Process process= pb.start();
                                List<String> results = readOutput(process.getInputStream());
                                System.out.println("Program was passed "+ arg.length+1+" args (including program name).");
                                System.out.println(results);
                                x=true;
                            }
                            catch (Exception e)
                            {

                            }


                        }
                    }
                    if(!x)
                    {
                        System.out.println(command1+": not found");
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
    public static List<String> readOutput(InputStream input)
            throws IOException {

        List<String> results = new java.util.ArrayList<>();

        try (java.io.BufferedReader reader =
                     new java.io.BufferedReader(
                             new java.io.InputStreamReader(input))) {

            String line;

            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
        }

        return results;
    }

}