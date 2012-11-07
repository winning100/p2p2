import java.io.IOException;
import java.util.Scanner;

/***
 * 
 *Wrapper initiate peer with parameters 
 *
 */

public class Wrapper{

	
public static void main(String []args){
	
	//int nodeId=Integer.parseInt(args[1]);
	//int serverPort=Integer.parseInt(args[2]);
	
	int nodeId=0;
	int serverPort=8000+0;
    	
	PeerServer peerServer=new PeerServer(serverPort,nodeId);
	System.out.println("start the server, port: "+serverPort+"  nodeId: "+nodeId);
	peerServer.start();
	Scanner scanner=new Scanner(System.in);
	while (true){
		String cmd=scanner.nextLine();
		CommandHandle commandHandle=new CommandHandle(cmd);
		
		
	}
}	
	
	
}