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
	
	public void handle() throws Exception{
		//System.out.println("in the commandHandle");
		if (!isGoodCommand())
			falseCommand();
		else{
			Thread cmdThread=null;
			if (cmd.startsWith("get")){
				//Get get=new Get();
			}
			else if (cmd.startsWith("ls")){
				LS ls=new LS();
				ls.list();
				}
			else if (cmd.startsWith("search"))
				{cmdThread=new Search();
				 
				}
			else if (cmd.startsWith("join"))
				{cmdThread=new Join();
				 cmdThread.start();}
			else if (cmd.startsWith("le"))
				{cmdThread=new Leave();
				 cmdThread.start();
				 }
			else if (cmd.startsWith("findpre"))
			{   System.out.println("process findpre ");
				
				int preId=findPre(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT,peer.getId());
				System.out.println("preId should be "+preId);
			/*try {
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			else if (cmd.startsWith("findsuc"))	
			{
				int sucId=findSuc(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT,peer.getId());
				System.out.println("the suc is "+sucId);
				/*try {
					int sucId=findSuc(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT,peer.getId());
					//System.out.println("sucId should be "+sucId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}	
			else
				{System.out.println("something bad happens");
				 return;
				}
			cmdThread=new Thread();
			//System.out.println("begin to handle cmd: "+cmd);
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
		pw.println(cmd_send);  // send out find successor request
		BufferedReader br=getReader(socket);
		
		int sucId=Integer.parseInt(br.readLine());   
		int remoteHostId=Integer.parseInt(br.readLine());
		String new_dest_ip=br.readLine();
		int new_dest_port=Integer.parseInt(br.readLine());
		
		socket.close();
		
		if (sucId+8000<8000){
			System.out.println("error in find successor");
			return -1;
		}
		
		if (sucId==remoteHostId){  // find the right predecessor
			System.out.println("suc is "+sucId);
			return sucId;}
		else
		{
			return findSuc(new_dest_ip, new_dest_port, id);
			}
		
		
	}
	
	
	int findPre(String dest_ip, int dest_port, int id) throws Exception{
		
		System.out.println("in the find pre");
		
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
		//OutputStream output=socket.getOutputStream();
		String cmd_send="findpre "+id;
		//output.write(cmd_send.getBytes());  // send out find successor request
		pw.println(cmd_send);
		//pw.flush();
		System.out.println("cmd_send is "+cmd_send);
		BufferedReader br=getReader(socket);
		
		int preId=Integer.parseInt(br.readLine());   
		//System.out.println(1);
		int remoteHostId=Integer.parseInt(br.readLine());
		//System.out.println(2);
		String new_dest_ip=br.readLine();
		//System.out.println(3);
		int new_dest_port=Integer.parseInt(br.readLine());
		System.out.println("receicve something ");
		
		System.out.println("preid: "+preId);
		System.out.println("remoteHost id "+remoteHostId);
		socket.close();
		
		if (preId+8000<8000){
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
		/**
		 * tell successor one node has joined
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		void sendJoinMsg() throws UnknownHostException, IOException{
			String joinMsg="joinMsg "+peer.getId();
			String ip=dest_ip;
			int port=peer.fingerTable.sucId;
			
			Socket socket=null;
			socket=new Socket(ip, port+8000);
			
			PrintWriter pw=getWriter(socket);
			//BufferedReader br=getReader(socket);
			
			pw.println(joinMsg);
			socket.close();
			
		}
		
		void setFingerTable() throws Exception{
			int suc=findSuc(dest_ip,dest_port,peer.getId());
			int pre=findPre(dest_ip,dest_port,peer.getId());
			peer.fingerTable.preId=pre;
			peer.fingerTable.sucId=suc;
			for (int i=0; i<4; i++){
				suc=findSuc(dest_ip,dest_port,(int) (peer.getId()+Math.pow(2, i)));
				pre=findPre(dest_ip,dest_port,(int) (peer.getId()+Math.pow(2, i)));
				peer.fingerTable.preTable.set(i, pre);
				peer.fingerTable.sucTable.set(i, suc);
			}
		}
		
		@Override
		public void run(){
			
			try {
				setFingerTable();
			} catch (Exception e) {
				System.out.println("set fingertable error");
				e.printStackTrace();
			}
			
			peer.display();
			/*try {
				int pre=findPre(dest_ip, dest_port, peer.getId());
				int suc=findSuc(dest_ip, dest_port, peer.getId());
				System.out.println("pre: "+pre+"  suc: "+suc);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("nnd");
				e.printStackTrace();
			}*/
			
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
	
	class Get{
		String fileName;
		Get (String fileName){
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
			return 0;
		}
		/**
		 * download ip: Peer.DEFAULT_DEST_IP, 
		 * port:getFileKey()+8000
		 * The downloaded file should be stored in file/
		 */
		public void download( ){
			
		}
	}
	
	class LS {
		
		public void list(){
			peer.display();
		}
	}
	
}