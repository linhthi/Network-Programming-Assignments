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
		DataInputStream in = null;
		DataOutputStream out = null;
		Scanner scanner = new Scanner(System.in);

		try{
			in = new DataInputStream(conn.getInputStream());
			out = new DataOutputStream(conn.getOutputStream());
			int thisNFileDowns = 0;
			byte[] buff = new byte[4096];
			String s = null;
			while (true){
				String request = in.readUTF();
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
						out.writeUTF(Integer.toString(total));
						out.flush();
						
						// send list file name
						for (int i = 0; i < children.length; i++) {
							String filename = children[i];
							out.writeUTF(filename);
							out.flush();
						}
					}   
				}
				// send file using filename request from client
				else if (request.equals("2")) {
					String filename = in.readUTF();

					if (filename == null){
						System.err.println(addr.toString() + ":" + port + " closed connection");
						break;
					}
					filename = filename.trim();
					System.out.print("client " + addr.toString() + ":" + port
									+ " required file \'" + filename + "\'" + "\n");
	
					File file = null;
					FileInputStream fis = null;
	
					long filesize = 0;
					try{
						filename = "SharedFolder/" +  filename;
						file = new File(filename);
						fis = new FileInputStream(file);
						filesize = file.length();
					} catch (IOException ex){
						ex.printStackTrace();
						System.err.println("cannot open \'" + filename + "\'");
						out.writeUTF("0");
						out.flush();
						continue;
					}
	
					System.out.print("size of \'" + filename + "\': " + filesize + "\n");
					//send filesize
					out.writeUTF(Long.toString(filesize));
					out.flush();
					System.err.print("sent filesize to " + addr.toString() + ":" + port + "\n");
					int nBytes = 0;
					boolean fileError = false;
					while (nBytes != -1){
	
						try{
							nBytes = fis.read(buff, 0, buff.length);
						} catch (IOException ex){
							ex.printStackTrace();
							fileError = true;
							fis.close();
						}
	
						if (nBytes < 0){
							break;
						}
	
						// Write in Socket Conn
						out.write(buff, 0, nBytes);
						out.flush();
						
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
					String filename = in.readUTF();
					//read filesize
					s = in.readUTF();
					long filesize = Long.parseLong(s);
					System.out.println("filesize: " + filesize);
	
					long nBytesRead = 0;
					filename = "SharedFolder/" +  filename;
					FileOutputStream fos = new FileOutputStream(filename);
	
					try{
						while (nBytesRead < filesize){
							//the number of byte to read 
							long needToRead = 0;
							if (filesize - nBytesRead < 4096)
								needToRead = filesize - nBytesRead;
							else
								needToRead = 4096;
	
							int nBytes = in.read(buff, 0, (int)needToRead);
							nBytesRead += nBytes;
							fos.write(buff, 0, nBytes);
						}
					}catch (SocketTimeoutException ex){
						System.err.println("encountered error while reading the file at the client side");
						fos.close();
						continue;
					}
					System.out.printf("uploading '%s' done\n", filename);
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
				in.close();
				out.close();
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
