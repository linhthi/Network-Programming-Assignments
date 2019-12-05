/**
 * @Fullname: Hoang Thi Linh
 * @Student Code: 17020852
 * Client
 */
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client{
	

	public static void main(String[] args){
		System.out.println("Enter server's IP addr and port in the form of <IP>:<port>");
		Scanner scanner = null;
		String[] tokens = null;
		Socket servSock = null;
		try{
			scanner = new Scanner(System.in);
			tokens = scanner.nextLine().split(":");
			String host = tokens[0];
			int port = Integer.parseInt(tokens[1]);

			InetAddress addr = InetAddress.getByName(host);
			servSock = new Socket(addr, port);
			servSock.setSoTimeout(5000);

			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(servSock.getOutputStream()));
			BufferedReader br = new BufferedReader(
					new InputStreamReader(servSock.getInputStream()));
			char[] buff = new char[4096];

			while (true){
				System.out.println("Enter your choice: ");
				System.out.println("---------------------------------------");
				System.out.println("1: show the list file that server has");
				System.out.println("2: download file");
				System.out.println("3: upload file");
				System.out.println("@logout: for exit and close connection");
				System.out.println("---------------------------------------");
				String c = scanner.nextLine();
				bw.write(c);
				bw.newLine();
				bw.flush();
				String s = null;
				if (c.equals("1")) {
					System.out.println("This is the list file:");
					// read the number of file in this folder
					s = br.readLine();
                	int total = Integer.parseInt(s);
					System.out.println("number of file: " + total);
					// print the list file
					for (int i = 0; i < total; i++){
						String fileName = br.readLine();
						System.out.println(fileName);
					}
					System.out.println("\n");
				}
				else if (c.equals("2")) {
					System.out.print("Enter file name you want to download!");
					String filename = scanner.nextLine();
					filename.trim();

					if (filename.length() == 0)
						continue;
	
					//send filename
					bw.write(filename + "\n");
					bw.newLine();
					bw.flush();
					
					//read filesize
					s = br.readLine();
					long filesize = Long.parseLong(s);
					if (filesize == 0){
						System.out.println("Cannot download the file");
						continue;
					}
					System.out.println("filesize: " + filesize);
	
					long nBytesRead = 0;
					filename = "SharedFolder/" +  filename;
					FileOutputStream fos = new FileOutputStream(filename);
					BufferedWriter bwFile = new BufferedWriter(new OutputStreamWriter(fos));
	
					try{
						while (nBytesRead < filesize){
							//the number of byte to read 
							long needToRead = 0;
							if (filesize - nBytesRead < 4096)
								needToRead = filesize - nBytesRead;
							else
								needToRead = 4096;
	
							int nBytes = br.read(buff, 0, (int)needToRead);
							nBytesRead += nBytes;
							bwFile.write(buff, 0, nBytes);
						}
					}catch (SocketTimeoutException ex){
						System.err.println("encountered error while reading the file at the server side");
						bwFile.close();
						fos.close();
						continue;
					}
					System.out.printf("downloading '%s' done\n", filename);
					bwFile.close();
					fos.close();
				}
				else if (c.equals("3")) {
					System.out.print("Enter file name you want to upload!");
					String filename = scanner.nextLine();
					filename.trim();

					if (filename.length() == 0)
						continue;
	
					//send filename
					bw.write(filename);
					bw.newLine();
					bw.flush();
					
					File file = null;
					FileInputStream fis = null;
					BufferedReader brFile = null;
	
					long filesize = 0;
					try{
						filename = "SharedFolder/" +  filename;
						file = new File(filename);
						fis = new FileInputStream(file);
						brFile = new BufferedReader(new InputStreamReader(fis)); 
						filesize = file.length();
					} catch (IOException ex){
						ex.printStackTrace();
						System.err.println("cannot open \'" + filename + "\'");
						bw.write("0");
						bw.newLine();
						bw.flush();
						continue;
					}
	
					System.out.print("size of \'" + filename + "\': " + filesize + "\n");
					//send filesize
					bw.write(Long.toString(filesize));
					bw.newLine();
					bw.flush();
					System.err.print("sent filesize to server \n");
					int nBytes = 0;
					boolean fileError = false;
					while (nBytes != -1){
	
						try{
							nBytes = brFile.read(buff, 0, buff.length);
						} catch (IOException ex){
							ex.printStackTrace();
							fileError = true;
							brFile.close();
							fis.close();
						}
	
						if (nBytes < 0){
							break;
						}
	
						bw.write(buff, 0, nBytes);
						bw.flush();
					}
	
					if (fileError)
						continue;
	
					System.out.println("sending \'" + filename + "\' to " + "server done");
				}
				else if (c.equals("@logout")) {
					break;
				}
				
			}
			servSock.close();
			scanner.close();
		} catch (IOException ex){
			ex.printStackTrace();
			System.exit(-1);
		//} catch (UnknownHostException ex){
		//	ex.printStackTrace();
		//	System.exit(-1);
		}
		
	}
}
