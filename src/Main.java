import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length > 0) {
			Get g = new Get(args[0]);
			System.out.println(g.getFileKey());
			try {
				System.out.println(g.download("127.0.0.1", 8888));
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			try {
				new GetHandle(8888).Listen();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

class Get {
	String fileName;
	Get (String fileName) {
		this.fileName=fileName;
	}
	/***
	 * translate the fileName to key
	 * you can choose any method to map file names to 
	 * integer ranging from 0 to 15. you even can
	 * require the file's name is in some sort of format.
	 * @return
	 */
	int getFileKey(){
		char[] namc = this.fileName.toCharArray();
		int i, s=0;
		for(i = 0; i < namc.length; i++)
			s = s + (int)namc[i];
		return s % 16;
	}
	/**
	 * download ip: Peer.DEFAULT_DEST_IP, 
	 * port: getFileKey()+8000
	 * The downloaded file should be stored in file/
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public boolean download(String IP, int port) throws UnknownHostException, IOException{
		Socket socket = new Socket(IP, port);
		PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
		pr.println(this.fileName);
		byte[] mybytearray = new byte[1024];
		int bytesRead;
		boolean result = false;
	    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
	    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("download/"+this.fileName));
	    while(true) {
	    	bytesRead = bis.read(mybytearray);
	    	if (bytesRead > 0)
	    	{
	    		bos.write(mybytearray, 0, bytesRead);
	    		result = true;
	    	}
	    	else
	    		break;
	    }
	    bos.close();
	    bis.close();
	    socket.close();
		return result;
	}
}

class GetHandle {
	ServerSocket ss;
	GetHandle(int port) throws IOException {
		this.ss = new ServerSocket(port);	
	}
	void Listen() throws IOException {
		System.out.println("Server starting at "+this.ss.getLocalPort());
		while(true) {
			Socket socket = this.ss.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String filename = br.readLine();
			System.out.println("Receiving request for "+filename);
			File f = new File(filename);
			if (f.exists()) {
				byte[] mybytearray = new byte[(int) f.length()];
			    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			    bis.read(mybytearray);
			    OutputStream os = socket.getOutputStream();
			    os.write(mybytearray, 0, mybytearray.length);
			    os.flush();
			    os.close();
			    bis.close();
			}
			socket.close();
		}
	}
}