import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Server {
    public static void main(String args[]) {
        try {
            //socket
            ServerSocket server = new ServerSocket(666);
            Socket s = server.accept();
            //input and output
            Scanner in = new Scanner(System.in);
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter pr = new PrintWriter(s.getOutputStream());
            //program
            while (true) {
                //listen for path and print
                String path = br.readLine();
                System.out.print(normalizePath(path) + ">");
                //get command
                String cmd = in.nextLine();
                //send command
                pr.println(cmd); pr.flush();
                //get command result
                String command = cmd.split(" ")[0];
                if (command.equals("download")) {
                    //checking if the file path is right
                    String res = br.readLine().replace("\n", "");
                    //if path right
                    if (!res.contains("Error!")) {
                        int size = Integer.parseInt(res);
                        byte[] b = new byte[size];
                        InputStream is = s.getInputStream();
                        String filename = getFileNameFronCommand(cmd);
                        FileOutputStream fo = new FileOutputStream(System.getProperty("user.dir") + "\\" + filename);
                        int count;
                        byte[] buffer = new byte[size]; // or 4096, or more
                        while (fo.getChannel().size() < size-1 && (count = is.read(buffer)) > 0)
                            fo.write(buffer, 0, count);
                        pr.println("finish"); pr.flush();
                    } else
                        System.out.println(res);
                } else if (command.equals("upload")) {
                    try {
                        String filename = getFileNameFronCommand(cmd);
                        FileInputStream fs = new FileInputStream(filename);
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
                        System.out.println("Error! File not found!");
                    }
                }
                else {
                    String res = br.readLine();
                    res = res.replaceAll("</br>", "\n");
                    if (res.length() > 1)
                        System.out.println(res);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String normalizePath(String path) {
        if (path.charAt(path.length() - 1) == '\\' && path.length() > 3)
            path = path.substring(0, path.length() - 1);
        return path;
    }

    static String getFileNameFronCommand(String cmd) {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.addAll(Arrays.asList(cmd.split(" ")));
        tmp.remove(0);
        return String.join(" ", tmp);
    }
}
