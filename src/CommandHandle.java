import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

class CommandHandle{
	String cmd;
	
	String []cmdList={"search", "join","le","ls","get","pre"};
	Peer peer;
	
	public CommandHandle(String cmd, Peer peer){
		this.cmd=cmd;
		this.peer=peer;
	}
	
	
	
	public BufferedReader getReader(Socket socket){
		InputStream socketIn=null;
		try {
			socketIn = socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("get reader error");
			e.printStackTrace();
			return null;
		}
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public PrintWriter getWriter(Socket socket){
		OutputStream socketOut=null;
		try {
			socketOut=socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("get writer error");
			
			e.printStackTrace();
			return null;
		}
		PrintWriter pw=new PrintWriter(socketOut,true);
		return pw;
	}
	
	
	/***
	 * if cmd is legal command
	 * @return
	 */
	public boolean isGoodCommand(){
		return true;
	}
	
	public void handle(){
		if (!isGoodCommand())
			falseCommand();
		else{
			Thread cmdThread=null;
			if (cmd.startsWith("get"))
				cmdThread=new Get();
			else if (cmd.startsWith("ls"))
				cmdThread=new LS();
			else if (cmd.startsWith("search"))
				cmdThread=new Search();
			else if (cmd.startsWith("join"))
				cmdThread=new Join();
			else if (cmd.startsWith("le"))
				cmdThread=new Leave();
			else
				{System.out.println("something bad happens");
				 return;
				}
			cmdThread=new Thread();
			System.out.println("begin to handle cmd: "+cmd);
			cmdThread.start();
		}
		
	}
	/***
	 * this command is not legal, print out some error information
	 */
	private void falseCommand(){
		
		
	}
	
	
	class Join extends Thread{
		
		String dest_ip;
		int dest_port;
		
		Join(){
			dest_ip=Peer.DEFAULT_DEST_IP;
			dest_port=Peer.DEFAULT_DEST_PORT;
		}
		Join(String ip,int port){
			/**
			 * the ip and port format might be checked
			 */
			dest_ip=ip;
			dest_port=port;
		}
		
		@Override
		public void run(){
			Socket socket=null;
			try {
				socket = new Socket(dest_ip,dest_port);
			} catch (UnknownHostException e) {
				
				e.printStackTrace();
				System.out.println("the ip or dest_port is illegal");
				return;
			} catch (IOException e) {
				System.out.println("socket set up error");
				e.printStackTrace();
				return;
			}
			
			PrintWriter pw=getWriter(socket);
			String cmd_send="join "+peer.getId();
			pw.write(cmd_send);  // send out join request
			BufferedReader br=getReader(socket);
			
			
		}
		
	}
	
	class Leave extends Thread{
		@Override
		public void run(){
			
		}
	}
	
	class Search extends Thread{
		@Override
		public void run(){
			
		}
	}
	
	class Get extends Thread{
		@Override
		public void run(){
			
		}
	}
	
	class LS extends Thread{
		@Override
		public void run(){
			
		}
	}
	
}