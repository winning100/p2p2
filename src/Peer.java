import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer{
	
private	int port;
private ServerSocket serverSocket;
private Socket socket;

//public static int PEER_SERVER_PORT

/**
 * 
 * @param initPort
 * @throws IOException
 * the exception has to be captured and prompt user to input another port number
 * 
 */
Peer (int initPort) throws IOException{
	//port=initPort;
	//serverSocket=new ServerSocket(port);
	
}


public int getPort(){
	return port;
}


public void setPort(int newPort){
	port=newPort;
}
	

	
	
	
}