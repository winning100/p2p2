import java.net.Socket;

/***
 * 
 * handle each request
 *
 */


class RequestHandle extends Thread{
	Socket socket;
	
	public RequestHandle(Socket socket){
		this.socket=socket;
	}
	
	@Override
	public void run(){
		
	}
	
	
	
}