import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class PeerServer extends Thread{
	
	private int serverPort;
	private int nodeId;
	ServerSocket serverSocket;
	Socket socket;
	
	public PeerServer(int serverPort, int nodeId){
		this.serverPort=serverPort;
		this.nodeId=nodeId;
		
	}
	
	
	@Override
	public void run(){
		try {
			listenRequest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("the server port might be in use");
		}
		
	}
	
	
	public void setPort(int serverPort){
		this.serverPort=serverPort;
		}
	
	public int getPort( ){
		
		return serverPort;
	}
	
	public int getNodeId(){
		return nodeId;
	}
	
	public void setNodeId(int nodeId){
		this.nodeId=nodeId;
	}
	/***
	 * 
	 * @throws IOException the server port might be in use.
	 */
	public void listenRequest() throws IOException{
		serverSocket=new ServerSocket(serverPort);
		while (true){
		socket=serverSocket.accept();
		Thread requestHandle=new RequestHandle(socket);
		requestHandle.start();
		
		}
	}
	
	
	
}