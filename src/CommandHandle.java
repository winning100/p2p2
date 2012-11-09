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
	
	
	
	/***
	 * 
	 * given a id, ask a host to resolve its successor
	 * @throws IOException 
	 *
	 */
	int findSuc(String dest_ip, int dest_port, int id) throws IOException{
		Socket socket=null;
		try {
			socket = new Socket(dest_ip,dest_port);
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
			System.out.println("the ip or dest_port is illegal");
			return -1;
		} catch (IOException e) {
			System.out.println("socket set up error");
			e.printStackTrace();
			return -1;
		}
		
		PrintWriter pw=getWriter(socket);
		String cmd_send="findsuc "+id;
		pw.write(cmd_send);  // send out find successor request
		BufferedReader br=getReader(socket);
		
		int sucId=br.read();   
		int remoteHostId=br.read();
		String new_dest_ip=br.readLine();
		int new_dest_port=br.read();
		
		socket.close();
		
		if (sucId<8000){
			System.out.println("error in find successor");
			return -1;
		}
		
		if (sucId==remoteHostId){  // find the right predecessor
			System.out.println("pre is "+sucId);
			return sucId;}
		else
		{
			return findSuc(new_dest_ip, new_dest_port, id);
			}
		
		
	}
	
	
	int findPre(String dest_ip, int dest_port, int id) throws Exception{
		Socket socket=null;
		try {
			socket = new Socket(dest_ip,dest_port);
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
			System.out.println("the ip or dest_port is illegal");
			return -1;
		} catch (IOException e) {
			System.out.println("socket set up error");
			e.printStackTrace();
			return -1;
		}
		
		PrintWriter pw=getWriter(socket);
		String cmd_send="findpre "+id;
		pw.write(cmd_send);  // send out find successor request
		BufferedReader br=getReader(socket);
		
		int preId=br.read();   
		int remoteHostId=br.read();
		String new_dest_ip=br.readLine();
		int new_dest_port=br.read();
		
		socket.close();
		
		if (preId<8000){
			System.out.println("error in find predecessor");
			return -1;
		}
		
		if (preId==remoteHostId){  // find the right predecessor
			System.out.println("pre is "+preId);
			return preId;}
		else
		{
			return findPre(new_dest_ip, new_dest_port, id);
			}
		
		
		
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
			
			try {
				int pre=findPre(dest_ip, dest_port, peer.getId());
				int suc=findSuc(dest_ip, dest_port, peer.getId());
				System.out.println("pre: "+pre+"  suc: "+suc);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("nnd");
				e.printStackTrace();
			}
			
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