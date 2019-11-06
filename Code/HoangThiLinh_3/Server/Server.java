/**
 * @Fullname: Hoang Thi Linh
 * @Student Code: 17020852
 * Send file from Server to Client using TCP socket
 */
import java.net.*;
import java.io.*;
import java.util.*;

class Server {
    public static int PORT = 6500;
 
    public static void main(String[] args) throws IOException {
        // Create server listen at port 6500 to connect from client
        ServerSocket serverSocket = new ServerSocket(PORT);

        // accept the connect from client and return a socket looklike client's
        Socket socket = serverSocket.accept();
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter os = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String request;
        do {
            request = is.readLine();
            System.out.println("Request from client " + request);

            // send the list file folder's
            if (request.equals("ls")) {
                File dir = new File("SharedFolder");
                String[] children = dir.list();
                if (children == null) {
                    System.out.println( "Either dir does not exist or is not a directory");
                }
                else {
                    int total = children.length;
                    // send the number of file at the folder
                    os.write(Integer.toString(total));
                    os.newLine();
                    os.flush();    
                    // send list file name
                    for (int i = 0; i < children.length; i++) {
                        String filename = children[i];
                        os.write(filename);   
                        os.newLine();
                        os.flush();
                    }
                }   
            }
            // send file using filename request from client
            else if (request.equals("dw")) {
    
                String fileName = is.readLine();
                fileName = "SharedFolder/" +  fileName;
                System.out.println("Sending file: " + fileName);

                // Count and Send the number line of file to client
                File file = new File(fileName);
                Scanner scanner = null;
                try {
                    scanner = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                int count = 0;
                while (scanner.hasNextLine()) {
                    String line1 = scanner.nextLine();
                    count++;
                }
                os.write(count);
                os.flush();

                // Send file
                Scanner scanners = null;
                try {
                    scanners = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
       
                while (scanners.hasNextLine()) {
                    String line = scanners.nextLine();
                    os.write(line);
                    os.newLine();
                    os.flush();
                }
            }
    
        } while (request != null);

    }
}