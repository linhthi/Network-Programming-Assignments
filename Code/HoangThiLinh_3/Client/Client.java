/**
 * @Fullname: Hoang Thi Linh
 * @Student Code: 17020852
 * Send file from Server to Client using TCP
 * Client can download files, command from keyboad
 */
import java.net.Socket;
import java.util.*;
import java.io.*;

class Client {
    public static void main(String[] args) throws Exception {
        // Connect to server with IP address and port number from input
        System.out.println("Enter IP address & port to connect: <IP>:<port>");
        Scanner scanner = new Scanner(System.in);
        String address = scanner.nextLine();
        String[] add = address.split(":");
        String IP = add[0];
        int port = Integer.parseInt(add[1]);

        // create a socket connect with server
        Socket socket = new Socket(IP, port);
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        
        String c;
        do {
            System.out.println("Enter your choice: ");
            System.out.println("---------------------------------------");
            System.out.println("ls: show the list file");
            System.out.println("dw: download file");
            System.out.println("@logout: for exit and close connection");
            System.out.println("---------------------------------------");
            c = scanner.nextLine();
            os.write(c);
            os.newLine();
            os.flush();

            if (c.equals("ls")) {
                System.out.println("This is the list file:");
                // read the number of file in this folder
                String s = is.readLine();
                int total = Integer.parseInt(s);
                System.out.println("number of file: " + total);
                // print the list file
                for (int i = 0; i < total; i++){
                    String fileName = is.readLine();
                    System.out.println(fileName);
                }
                System.out.println("\n");
        
            }
            if (c.equals("dw")) {
                // send file name want to download
                System.out.print("Enter the file name: ");
                String fileName = scanner.nextLine();
                os.write(fileName);
                os.newLine();
                os.flush();
    
                // Number of line in the file
                int numLine = is.read();
                // System.out.println(numLine);
                System.out.println("Downloding...............");
                // Download file
                FileWriter fw = new FileWriter(fileName);
                for (int i = 0; i < numLine; i++){
                    String data = is.readLine();
                    try {                      
                        fw.write(data);
                        fw.write("\n");
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
                fw.close();
                System.out.println("Save the file successfully!");
            }
            if (c.equals("@logout")) {
                System.out.println("Bye");
                socket.close();
            }
        } while (!c.equals("@logout"));

    }

}