import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

class CommandHandle extends Thread{
	String cmd;
	
	String []cmdList={"search", "join","le","ls","get","pre"};
	Peer peer;
	
	public CommandHandle(String cmd, Peer peer){
		this.cmd=cmd;
		this.peer=peer;
	}
	
	@Override
	public void run(){
		try {
			handle();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				{
				 
				}
			else if (cmd.startsWith("join"))
				{Join join=new Join(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT);
				 join.startJoin();
				
				}
			else if (cmd.startsWith("le"))
				{
				 }
			else if (cmd.startsWith("findpre"))
			{  
			}
			else if (cmd.startsWith("findsuc"))	
			{
				
			}	
			else
				{System.out.println("something bad happens");
				 return;
				}
			//cmdThread=new Thread();
			//System.out.println("begin to handle cmd: "+cmd);
			//cmdThread.start();
		}
		
	}
	/***
	 * this command is not legal, print out some error information
	 */
	private void falseCommand(){
		
		
	}
	
	

	
	class Join {
		
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
		 * 
		 * when join network, it has to find suc
		 * @throws IOException 
		 * @throws UnknownHostException 
		 * 
		 */	
		int findSuc(int localId) throws UnknownHostException, IOException{
			int pre=findPre( localId);
			return findSuc(Peer.DEFAULT_DEST_IP,8000+pre);
		}
		
		/**
		 * request suc of a given host
		 * @param ip
		 * @param port
		 * @return
		 * @throws IOException 
		 * @throws UnknownHostException 
		 */
		int findSuc(String ip, int port) throws UnknownHostException, IOException{
			
			Socket socket=new Socket(ip,port);
			PrintWriter pw=RequestHandle.getWriter(socket);
			String cmd="yoursuc";
			pw.println(cmd);
			BufferedReader br=RequestHandle.getReader(socket);
			int suc=Integer.parseInt(br.readLine());
			
			socket.close();
			return suc;
			
			
		}
		
		/**
		 * try to find its own pre in network
		 * @param id
		 * @return
		 * @throws IOException 
		 * @throws UnknownHostException 
		 */
		int findPre(int localId) throws UnknownHostException, IOException{
			System.out.println("+++++++++in findPre++++++");
			//int localId=peer.getId();
			int remoteSuc=findSuc(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT);
			System.out.println("the default suc is "+remoteSuc);
			int remoteNodeId=0;
			while(true){
			if (RequestHandle.inRange(localId,remoteNodeId,remoteSuc,true,false))
			{   System.out.println(localId+"  in range from "+remoteNodeId+" to "+remoteSuc);
				//peer.fingerTable.preId=remoteNodeId;
				return remoteNodeId;
			}
			System.out.println(localId+" not in range from "+remoteNodeId+" to "+remoteSuc);
			
			remoteNodeId=findClosestPrecedingFinger(localId,remoteNodeId);
			System.out.println("next requestedNodeId "+remoteNodeId);
			remoteSuc=findSuc(Peer.DEFAULT_DEST_IP,8000+remoteNodeId);
			}
		}
		
		
		
		int findClosestPrecedingFinger(int id, int requestNodeId) throws UnknownHostException, IOException{
			
			Socket socket=new Socket(Peer.DEFAULT_DEST_IP,8000+requestNodeId);
			PrintWriter pw=getWriter(socket);
			String cmd="closestprecedingfinger "+id;
			pw.println(cmd);
			
			return Integer.parseInt(getReader(socket).readLine());
			
			
		}
		/**
		 * the entrance to join the network
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		public void startJoin() throws UnknownHostException, IOException{
		
			init_finger_table("123",888,0);
			update_neighbour();
			update_others();
			transfer_keys();
		}
		
		//use an arbitrary node to initiate the local table
		void init_finger_table(String ip, int port, int remoteId) throws UnknownHostException, IOException{
			int pre=findPre(peer.getId());
			int suc=findSuc(peer.getId());
			peer.fingerTable.sucId=suc;  //get the correct suc and pre
			peer.fingerTable.preId=pre;
			peer.fingerTable.sucTable.set(0, suc);
			for(int i=1;i<4;i++){
				suc=findSuc((int) ((peer.getId()+Math.pow(2, i))%16));
				int k=(int) ((peer.getId()+Math.pow(2, i))%16);
				if (RequestHandle.inRange(k,pre,peer.getId(),true,false))
					suc=peer.getId();
				peer.fingerTable.sucTable.set(i, suc);
			}
			
		}
		void update_others() throws UnknownHostException, IOException {
			int localId=peer.getId();
			int suc=peer.fingerTable.sucId;
			String cmd="new_node "+localId;
			Socket socket=new Socket(Peer.DEFAULT_DEST_IP, 8000+suc);
			PrintWriter pw=getWriter(socket);
			pw.println(cmd);
			socket.close();
			/*for (int i=0; i<4; i++){
				int j=(int) (peer.getId()-Math.pow(2, i));
				if (j<0)
					j=j+16;
		
				int p=findPre(j);
				
				update_node_finger_table(p,i);
			}*/
		}
		void update_node_finger_table(int nodeId,int rank) throws UnknownHostException, IOException{
			Socket socket=new Socket(Peer.DEFAULT_DEST_IP, 8000+nodeId);
			PrintWriter pw=getWriter(socket);
			String cmd="update_node_finger_table "+peer.getId()+" "+rank;
			pw.println(cmd);
			
		}
		void update_neighbour() throws UnknownHostException, IOException{
			int suc=peer.fingerTable.sucId;
			int pre=peer.fingerTable.preId;
			String cmd="newSuc "+peer.getId();
			Socket socket=new Socket(Peer.DEFAULT_DEST_IP,8000+pre);
			PrintWriter pw=RequestHandle.getWriter(socket);
			pw.println(cmd);
			socket.close();
			socket=new Socket(Peer.DEFAULT_DEST_IP, 8000+suc);
			pw=RequestHandle.getWriter(socket);
			cmd="newPre "+peer.getId();
			pw.println(cmd);
			socket.close();
		}
		
		void transfer_keys(){
			
		}
	}
	
	class Leave{
		
	}
	
	class Search{
		
		
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