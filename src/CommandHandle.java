import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Timer;

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
				
				String []s=cmd.split(" ");
				int searchId=Integer.parseInt(s[1]);
				Search search=new Search(searchId);
				search.startSearch(1);
				
			}
			else if (cmd.startsWith("ls")){
				LS ls=new LS();
				ls.list();
				}
			else if (cmd.startsWith("search"))
				{String []s=cmd.split(" ");
				 int searchId=Integer.parseInt(s[1]);
				 Search search=new Search(searchId);
				 search.startSearch(0);
				}
			else if (cmd.startsWith("join"))
				{Join join=new Join(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT);
				 join.startJoin();
				
				}
			else if (cmd.startsWith("le"))
				{Leave leave=new Leave();
				leave.start_leave();
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
	
	

	
	public class Join {
		
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
		 * return address and nodeId of suc
		 */	
		String findSuc(int localId) throws UnknownHostException, IOException{
			String preString=findPre(localId);
			String []k=preString.split(" ");
			String preIp=k[0];
			int port=Integer.parseInt(k[1]);
			int pre=Integer.parseInt(k[2]);
			return findSuc(preIp,port);
		}
		
		/**
		 * request suc of a given host
		 * @param ip
		 * @param port
		 * @return
		 * @throws IOException 
		 * @throws UnknownHostException 
		 * 
		 * return address and nodeId of suc
		 */
		String findSuc(String ip, int port) throws UnknownHostException, IOException{
			
			Socket socket=new Socket(ip,port);
			PrintWriter pw=RequestHandle.getWriter(socket);
			String cmd="yoursuc";
			pw.println(cmd);
			BufferedReader br=RequestHandle.getReader(socket);
			String suc=br.readLine();
			System.out.println("+++in findsuc +++ : "+suc);
			return suc;
			
			
		}
		
		String findPre(String ip, int port) throws UnknownHostException, IOException{
			
			Socket socket=new Socket(ip,port);
			PrintWriter pw=RequestHandle.getWriter(socket);
			String cmd="yourpre";
			pw.println(cmd);
			BufferedReader br=RequestHandle.getReader(socket);
			String pre=br.readLine();
			System.out.println("+++in findsuc +++ : "+pre);
			return pre;
			
			
		}
		
		/**
		 * try to find its own pre in network
		 * @param id
		 * @return
		 * @throws IOException 
		 * @throws UnknownHostException 
		 */
		String findPre(int localId) throws UnknownHostException, IOException{
			System.out.println("+++++++++in findPre++++++");
			//int localId=peer.getId();
			String remoteSucString=findSuc(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT);
			String []str=remoteSucString.split(" ");
			String remoteIp=str[0];
			int remotePort=Integer.parseInt(str[1]);
			int remoteSuc=Integer.parseInt(str[2]);
			
			String remoteNodeIp=Peer.DEFAULT_DEST_IP;
			
			int remoteNodePort=Peer.DEFAULT_DEST_PORT;
			
			System.out.println("the default suc is "+remoteSuc);
			int remoteNodeId=0;
			while(true){
			if (RequestHandle.inRange(localId,remoteNodeId,remoteSuc,true,false))
			{   System.out.println(localId+"  in range from "+remoteNodeId+" to "+remoteSuc);
				//peer.fingerTable.preId=remoteNodeId;
			    System.out.println("return in pre: "+remoteNodeIp+" "+remoteNodePort+" "+remoteNodeId);
				return remoteNodeIp+" "+remoteNodePort+" "+remoteNodeId;
			}
			System.out.println(localId+" not in range from "+remoteNodeId+" to "+remoteSuc);
			
			//remoteNodeId=findClosestPrecedingFinger(localId,remoteIp,remotePort);
			String remoteNodeIdString=findClosestPrecedingFinger(localId,remoteIp,remotePort);
			String []k=remoteNodeIdString.split(" ");
			remoteNodeIp=k[0];
			remoteNodePort=Integer.parseInt(k[1]);
			remoteNodeId=Integer.parseInt(k[2]);
			
			System.out.println("next requestedNodeId "+remoteNodeId);
			remoteSucString=findSuc(remoteIp,remotePort);
			remoteIp=str[0];
			remotePort=Integer.parseInt(str[1]);
			remoteSuc=Integer.parseInt(str[2]);
			}
		}
		
		
		/***
		 * ip + port + id
		 * @param id
		 * @param remoteIp
		 * @param remotePort
		 * @return
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		String findClosestPrecedingFinger(int id, String remoteIp, int remotePort) throws UnknownHostException, IOException{
			
			Socket socket=new Socket(remoteIp,remotePort);
			PrintWriter pw=getWriter(socket);
			String cmd="closestprecedingfinger "+id;
			pw.println(cmd);
			String str=getReader(socket).readLine();
			//String []str1=str.split(" ");
			return str;
			
			
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
			suc_suc_pre_pre();
			
		}
		
		void suc_suc_pre_pre() throws UnknownHostException, IOException{
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
		
			
			
			
			String preIp=peer.fingerTable.preIP;
			int prePort=peer.fingerTable.prePort;
			
			String s1=findSuc(sucIp,sucPort);
			String p1=findPre(preIp,prePort);
			
			String []suc_suc=s1.split(" ");
			peer.fingerTable.sucsucIP=suc_suc[0];
			peer.fingerTable.sucsucPort=Integer.parseInt(suc_suc[1]);
			peer.fingerTable.sucsucId=Integer.parseInt(suc_suc[2]);
			
			String []pre_pre=p1.split(" ");
			peer.fingerTable.prepreIP=pre_pre[0];
			peer.fingerTable.preprePort=Integer.parseInt(pre_pre[1]);
			peer.fingerTable.prepreId=Integer.parseInt(pre_pre[2]);
			
			
		}
		        
		//use an arbitrary node to initiate the local table
		void init_finger_table(String ip1, int port1, int remoteId123) throws UnknownHostException, IOException{
			String preString=findPre(peer.getId());
			String []preS=preString.split(" ");
			String preIp=preS[0];
			int prePort=Integer.parseInt(preS[1]);
			int pre=Integer.parseInt(preS[2]);
			
			String sucString=findSuc(peer.getId());
			String []sucS=sucString.split(" ");
			String sucIp=sucS[0];
			int sucPort=Integer.parseInt(sucS[1]);
			int suc=Integer.parseInt(sucS[2]);
			
			System.out.println("preString:"+preString);
			System.out.println("sucString:"+sucString);
			//int pre=findPre(peer.getId());
			//int suc=findSuc(peer.getId());
			  //get the correct suc and pre
			peer.fingerTable.sucId=suc;
			peer.fingerTable.sucIP=sucIp;
			peer.fingerTable.sucPort=sucPort;
			
			peer.fingerTable.preIP=preIp;
			peer.fingerTable.prePort=prePort;
			peer.fingerTable.preId=pre;
			
			peer.fingerTable.sucTable.set(0, suc);
			peer.fingerTable.ipTable.set(0, sucIp+" "+sucPort);
			for(int i=1;i<4;i++){
				System.out.println("++++++in for +++++"+"  "+i);
				
				sucString=findSuc((int) ((peer.getId()+Math.pow(2, i))%16));
				sucS=sucString.split(" ");
				sucIp=sucS[0];
				sucPort=Integer.parseInt(sucS[1]);
				suc=Integer.parseInt(sucS[2]);
				
				int k=(int) ((peer.getId()+Math.pow(2, i))%16);
				if (RequestHandle.inRange(k,pre,peer.getId(),true,false))
					{suc=peer.getId();
					 sucIp=peer.fingerTable.ip;
					 sucPort=peer.fingerTable.port;
					}
				peer.fingerTable.sucTable.set(i, suc);
				peer.fingerTable.ipTable.set(i, sucIp+" "+sucPort);
			}
			
		}
		void update_others() throws UnknownHostException, IOException {
			int localId=peer.getId();
			String localIp=peer.fingerTable.ip;
			int localport=peer.fingerTable.port;
			
			int suc=peer.fingerTable.sucId;
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
			
			String cmd="new_node "+localIp+" "+localport+" "+localId;
			Socket socket=new Socket(sucIp, sucPort);
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
		
		void update_neighbour() throws UnknownHostException, IOException{
			
			int suc=peer.fingerTable.sucId;
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
			
			int pre=peer.fingerTable.preId;
			String preIp=peer.fingerTable.preIP;
			int prePort=peer.fingerTable.prePort;
			
			String cmd="newSuc "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId();
			Socket socket=new Socket(preIp,prePort);
			PrintWriter pw=RequestHandle.getWriter(socket);
			pw.println(cmd);
			socket.close();
			
			socket=new Socket(sucIp, sucPort);
			pw=RequestHandle.getWriter(socket);
			cmd="newPre "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId();
			pw.println(cmd);
			socket.close();
		}
		
		void transfer_keys() throws UnknownHostException, IOException{
			int sucId=peer.fingerTable.sucId;
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
			String cmd="transfer_key "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId()+" "+peer.fingerTable.preId;
			Socket socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			PrintWriter pw=getWriter(socket);
			pw.println(cmd);
			socket.close();
			
		}
	}
	
	class Leave{
		
		
		void start_leave() throws UnknownHostException, IOException{
			leave_transfer();
			send_leave_msg();
			tell_neighbor();
			System.out.println("already left the network");
		}
		
		void tell_neighbor() throws UnknownHostException, IOException{
			String cmd_to_pre="your_suc_le "+peer.getId()+" "+peer.fingerTable.sucId+" "+peer.fingerTable.sucIP+" "+peer.fingerTable.sucPort;
			String cmd_to_suc="your_pre_le "+peer.getId()+" "+peer.fingerTable.preId+" "+peer.fingerTable.preIP+" "+peer.fingerTable.prePort;
			
			Socket socket=new Socket(peer.fingerTable.preIP,peer.fingerTable.prePort);
			PrintWriter pw=getWriter(socket);
			pw.println(cmd_to_pre);
			socket.close();
			
			socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			pw=getWriter(socket);
			pw.println(cmd_to_suc);
			socket.close();
			
		}
		
		void leave_transfer() throws UnknownHostException, IOException{
			int suc_id=peer.fingerTable.sucId;
			String suc_ip=peer.fingerTable.sucIP;
			int suc_port=peer.fingerTable.sucPort;
			for (Map.Entry<Integer,String>entry : peer.fingerTable.keyList.entrySet() ){
				int key=entry.getKey();
				String value=entry.getValue();
				Socket socket=new Socket(suc_ip, suc_port);
				PrintWriter pw=getWriter(socket);
				String cmd="give_you_data_item "+key+"#"+value;
				pw.println(cmd);
				socket.close();
			}
		}
		
		void send_leave_msg(){
			int leave_id=peer.getId();
			String suc_ip=peer.fingerTable.sucIP;
			int suc_port=peer.fingerTable.sucPort;
			Socket socket=null;
			try {
				socket=new Socket(suc_ip, suc_port);
			} catch (Exception e) {
				e.printStackTrace();
			}
			PrintWriter pw=RequestHandle.getWriter(socket);
			String le_msg="le "+leave_id+" "+peer.fingerTable.sucIP+" "+peer.fingerTable.sucPort+" "+peer.fingerTable.sucId+" "+peer.TTL;
			pw.println(le_msg);
		}
	}
	
	class Search{
		int searchId;
		Search(int searchId){
			this.searchId=searchId;
		}
		
		void startSearch(int get) throws UnknownHostException, IOException{

			System.out.println("++++In the start search++++");
			if (peer.fingerTable.keyList.containsKey(searchId))
			{
				System.out.println("the data is local");
				System.out.println(peer.fingerTable.ip+" "+peer.fingerTable.port);
				return ;
			}
			
			
			
			for (int i=0; i<4; i++){
				int low=(int)((peer.getId()+Math.pow(2, i))%16);
				int high=(int)((peer.getId()+Math.pow(2, i+1))%16);
				if (RequestHandle.inRange(searchId, low, high, false, true))
				{   String []p=peer.fingerTable.getIp(i).split(" ");
				    String nextIp=p[0];
				    int nextPort=Integer.parseInt(p[1]);
				    
				    //send out search information
					searchNext(nextIp,nextPort,peer.fingerTable.ip,peer.fingerTable.port, searchId, Peer.TTL,get);
					return;
				}
				
			}
		}
		
		void searchNext(String nextIp,int nextPort,String localip, int localport, int localid, int ttl, int get) throws UnknownHostException, IOException{
			Socket socket=new Socket(nextIp,nextPort);
			PrintWriter pw=getWriter(socket);
			String new_cmd="search "+searchId+" "+localip+" "+localport+" "+ttl+" "+get; 
			pw.println(new_cmd);
			socket.close();
		}
		
	}
	
	class Get{
		int key;
		Get (int key) {
			this.key=key;
		}
		
		void start_get(){
			Search search=new Search(key);
		}
	}
	
	
	
	class Stable extends Thread{
		
		
		
		@Override
		public void run(){
			while(true){
				
				
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("sleep error");
					e.printStackTrace();
				}
			}
			
			
			
			
		}
		
		
		void detectPre(){
			String preIp=peer.fingerTable.preIP;
			int prePort=peer.fingerTable.prePort;
			
			Socket socket=null;
			try {
				socket = new Socket(preIp,prePort);
			} catch (Exception e) {
			
				//preLost(); 
				e.printStackTrace();
				return ;
			}
			PrintWriter pw=getWriter(socket);
			pw.println("ping");
			BufferedReader br=getReader(socket);
			String rec_msg="";
			try {
				rec_msg = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (rec_msg.equals("pong"))
				return;
		}
		
	}
	
	
	
	class LS {
		
		public void list(){
			peer.display();
		}
	}
	
}