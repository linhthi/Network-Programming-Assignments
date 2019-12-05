/**
 * @Fullname: Hoang Thi Linh
 * @Student Code: 17020852
 * Server
 */
import java.net.*;
import java.util.Scanner;
import java.io.*;

class SocketThread extends Thread{
	private Socket conn;
	private static int nFileDowns = 0;
	private static int nFileUps = 0;

	public SocketThread(Socket conn){
		this.conn = conn;
	}

	public void run(){
		InetAddress addr = conn.getInetAddress();
		int port = conn.getPort();
		System.out.println("connection from client: " + addr.toString() + ":" + port);
		BufferedReader br = null;
		BufferedWriter bw = null;
		Scanner scanner = new Scanner(System.in);
		try{
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			int thisNFileDowns = 0;
			char[] buff = new char[4096];
			String s = null;
			while (true){
				String request = br.readLine();
				System.out.println("Command from client " + request);

				// send the list file folder's
				if (request.equals("1")) {
					File dir = new File("SharedFolder");
					String[] children = dir.list();
					if (children == null) {
						System.out.println( "Either dir does not exist or is not a directory");
					}
					else {
						int total = children.length;
						// send the number of file at the folder
						bw.write(Integer.toString(total));
						bw.newLine();
						bw.flush();
						
						// send list file name
						for (int i = 0; i < children.length; i++) {
							String filename = children[i];
							bw.write(filename);
							bw.newLine();
							bw.flush();
						}
					}   
				}
				// send file using filename request from client
				else if (request.equals("2")) {
					String filename = br.readLine();

					if (filename == null){
						System.err.println(addr.toString() + ":" + port + " closed connection");
						break;
					}
					filename = filename.trim();
					System.out.print("client " + addr.toString() + ":" + port
									+ " required file \'" + filename + "\'" + "\n");
	
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
					System.err.print("sent filesize to " + addr.toString() + ":" + port + "\n");
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
	
					System.out.println("sending \'" + filename + "\' to " + addr.toString() 
									+ ":" + port + " done");
					thisNFileDowns ++;
					System.out.println("sent " + thisNFileDowns + " files to " + addr.toString()
									+ ":" + port);
	
					increaseCountDownload();
				}
				else if (request.equals("3")) {
					//read filename
					String filename = br.readLine();
					//read filesize
					s = br.readLine();
					long filesize = Long.parseLong(s);
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
						System.err.println("encountered error while reading the file at the client side");
						bwFile.close();
						fos.close();
						continue;
					}
					System.out.printf("uploading '%s' done\n", filename);
					bwFile.close();
					fos.close();
					increaseCountUpload();
				}

				
			}
		} catch (IOException ex){
			ex.printStackTrace();
		} finally {
			System.err.println("closing connection to " + addr.toString() + ":"
								+ port);
			try{
				bw.close();
				br.close();
				scanner.close();
				conn.close();
			} catch (IOException ex2){
				ex2.printStackTrace();
			}
			System.out.println("connection to " + addr.toString() + ":" + port + " closed");
		}
	}

	public synchronized void increaseCountDownload(){
		nFileDowns ++;
		System.out.println("total files client downloaded: " + nFileDowns);
	}
	public synchronized void increaseCountUpload(){
		nFileUps ++;
		System.out.println("total files client uploaded : " + nFileUps);
	}
}

public class server {
	public static void main(String[] args){
		ServerSocket servSock = null;
		try{
			servSock = new ServerSocket(6500);
			servSock.setReuseAddress(true);
			Socket conn = null;
			while (true){
				try{
					conn = servSock.accept();
					SocketThread st = new SocketThread(conn);
					st.start();
				} catch (IOException ex){
					ex.printStackTrace();
					continue;
				}
			}
		} catch (IOException ex){
			ex.printStackTrace();
			System.exit(-1);
		}
	}
}
