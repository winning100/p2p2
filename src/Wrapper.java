import java.io.IOException;
import java.util.Scanner;

/***
 * 
 *Wrapper initiate peer with parameters 
 *
 */

public class Wrapper{

	
public static void main(String []args) throws Exception{
	
	//int nodeId=Integer.parseInt(args[1]);
	//int serverPort=Integer.parseInt(args[2]);
	
	Scanner scanner=new Scanner(System.in);
	
	System.out.println("input node:");
	int nodeId=Integer.parseInt(scanner.nextLine());
	
	
	//int nodeId=5;
	int serverPort=8000+nodeId;
	
	PeerServer peerServer=new PeerServer(serverPort,nodeId);
	System.out.println("start the server, port: "+serverPort+"  nodeId: "+nodeId);
	peerServer.start();
	
	while (scanner.hasNextLine()){
		//System.out.println("before read");
		String cmd=scanner.nextLine();
		//System.out.println("after read");
		CommandHandle commandHandle=new CommandHandle(cmd,peerServer.peer);
		try {
			commandHandle.start();
		} catch (Exception e) {
			System.out.println("cmd execute error: "+cmd);
			e.printStackTrace();
		}
		
		
	}
}	
	
	
}