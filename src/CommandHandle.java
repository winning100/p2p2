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
/**
 * This class used to analyze the input command and generate 
 * socket request. 
 * 
 *
 */
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
			//e.printStackTrace();
			System.out.println("change another ip and port");
		}
	}
	/**
	 * get socket buffer reader
	 * @param socket
	 * @return
	 */
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
	/***
	 * get socket print writer
	 * @param socket
	 * @return
	 */
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
	
	/***
	 * according to the command type, using different class 
	 * and methods to handle the command
	 * @throws Exception
	 */
	public void handle() throws Exception{
		//System.out.println("in the commandHandle");
		if (!isGoodCommand())
			falseCommand();
		else{
			Thread cmdThread=null;
			// it's a get command, download a data item
			if (cmd.startsWith("get")){
				
				String []s=cmd.split(" ");
				int searchId=Integer.parseInt(s[1]);
				Search search=new Search(searchId);
				search.startSearch(1);
				
			}
			//ls command, display the finger table
			else if (cmd.startsWith("ls")){
				LS ls=new LS();
				ls.list();
				}
			//search command, search the key in the ring
			else if (cmd.startsWith("search"))
				{String []s=cmd.split(" ");
				 int searchId=Integer.parseInt(s[1]);
				 Search search=new Search(searchId);
				 search.startSearch(0);
				}
			//join command, join in a ring
			else if (cmd.startsWith("join"))
				{String []s=cmd.split(" ");
				Join join=null;
				if (s.length>1)
				{String join_ip=s[1];
				 int join_port=Integer.parseInt(s[2]);
				 join=new Join(join_ip,join_port);	
				}
				else
				join=new Join(Peer.DEFAULT_DEST_IP,Peer.DEFAULT_DEST_PORT);
				
				join.startJoin();
				
				}
			//le command, leave the ring
			else if (cmd.startsWith("le"))
				{Leave leave=new Leave();
				leave.start_leave();
				 }
			else
				{System.out.println("input command is bad");
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
	
	
/***
 * 
 * This class is responsible for joining in the ring
 *
 */
	
	public class Join {
		
		String dest_ip; // the default ip and port
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
		 * @throws Exception 
		 * @throws IOException 
		 * @throws UnknownHostException 
		 * 
		 * return address and nodeId of suc
		 */	
		String findSuc(int localId) throws Exception {
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
		String findSuc(String ip, int port) throws Exception {
			
			Socket socket=new Socket(ip,port);
			PrintWriter pw=RequestHandle.getWriter(socket);
			String cmd="yoursuc";
			pw.println(cmd);
			BufferedReader br=RequestHandle.getReader(socket);
			String suc=br.readLine();
			System.out.println("+++in findsuc +++ : "+suc);
			return suc;
			
			
			
		}
		/***
		 * 
		 * @param ip
		 * @param port
		 * @return
		 * @throws UnknownHostException
		 * @throws IOException
		 * given a node and return its pre
		 */
		String findPre(String ip, int port) throws Exception{
			
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
		 * @throws Exception 
		 * @throws IOException 
		 * @throws UnknownHostException 
		 */
		String findPre(int localId) throws Exception {
			System.out.println("+++++++++in findPre++++++");
			//int localId=peer.getId();
			String remoteSucString=findSuc(peer.DEFAULT_DEST_IP,peer.DEFAULT_DEST_PORT);
			String []str=remoteSucString.split(" ");
			String remoteIp=str[0];
			int remotePort=Integer.parseInt(str[1]);
			int remoteSuc=Integer.parseInt(str[2]);
			
			String remoteNodeIp=peer.DEFAULT_DEST_IP;
			
			int remoteNodePort=peer.DEFAULT_DEST_PORT;
			
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
		 * @throws Exception 
		 */
		public void startJoin() throws Exception{
		    System.out.println("+++In the startJoin+++");
			init_finger_table("123",888,0);
			update_neighbour();
			update_others();
			System.out.println("before transfer key");
			transfer_keys();
			suc_suc_pre_pre();
			Stable stable=new Stable(peer.fingerTable.preId,peer.fingerTable.preIP,peer.fingerTable.prePort);
			stable.start();
			
		}
		/***
		 * 
		 * @throws Exception 
		 */
		void suc_suc_pre_pre() throws Exception{
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
		void init_finger_table(String ip1, int port1, int remoteId123) throws Exception{
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
			/**
			 * suc_suc_pre_pre test code
			 */
			init_suc_suc_pre_pre();
				
			
			
		}
		
		void init_suc_suc_pre_pre(){
			String cmd="yoursuc";
			Socket socket=null;
			try {
				socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			}catch (IOException e) {
				
				e.printStackTrace();
			}
			PrintWriter pw=getWriter(socket);
			BufferedReader br=getReader(socket);
			pw.println(cmd);
			String k="";
			try {
				k=br.readLine();
			} catch (IOException e) {
			
				e.printStackTrace();
			}
			String []s=k.split(" ");
			peer.fingerTable.sucsucIP=s[0];
			peer.fingerTable.sucsucPort=Integer.parseInt(s[1]);
			peer.fingerTable.sucsucId=Integer.parseInt(s[2]);
			
			System.out.println("@@@@@@@@@@@@@@@");
			cmd="yourpre";
			System.out.println("asdas: "+peer.fingerTable.preIP+" "+peer.fingerTable.port);
			try {
				socket=new Socket(peer.fingerTable.preIP,peer.fingerTable.port);
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
			pw=getWriter(socket);
			br=getReader(socket);
			pw.println(cmd);
			
			try {
				k=br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("###########");
			s=k.split(" ");
			peer.fingerTable.prepreIP=s[0];
			peer.fingerTable.preprePort=Integer.parseInt(s[1]);
			peer.fingerTable.prepreId=Integer.parseInt(s[2]);
			
		}
	
		
		/**
		 * When a join in the ring, it has to send msg to tell others to
		 * update their finger table
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		void update_others() throws UnknownHostException, IOException {
			System.out.println("++++update others++++");
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
		
		/***
		 * update neighbour's successor and predecessor
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		void update_neighbour() throws UnknownHostException, IOException{
			System.out.println("++++update neighbour++++");
			int suc=peer.fingerTable.sucId;
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
			
			int pre=peer.fingerTable.preId;
			String preIp=peer.fingerTable.preIP;
			int prePort=peer.fingerTable.prePort;
			
			String cmd="newSuc "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId()+" "+suc+" "+sucIp+" "+sucPort;
			Socket socket=new Socket(preIp,prePort);
			PrintWriter pw=RequestHandle.getWriter(socket);
			pw.println(cmd);
			socket.close();
			
			socket=new Socket(sucIp, sucPort);
			pw=RequestHandle.getWriter(socket);
			cmd="newPre "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId()+" "+pre+" "+preIp+" "+prePort;
			pw.println(cmd);
			socket.close();
		}
		
		/**
		 * When a node join in a network, other nodes must transfer the keys,
		 * which belong to the new node.
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		void transfer_keys() throws UnknownHostException, IOException{
			System.out.println("++++ In the transfer key++++");
			int sucId=peer.fingerTable.sucId;
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
			String cmd="transfer_key "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId()+" "+peer.fingerTable.preId;
			if (sucId==peer.getId())
				return;
			Socket socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			PrintWriter pw=getWriter(socket);
			pw.println(cmd);
			socket.close();
			
		}
	}
	/***
	 * This class is used for le command
	 * A node leaves the ring
	 *
	 *
	 */
	class Leave{
		
		
		void start_leave() throws UnknownHostException, IOException{
			leave_transfer();
			send_leave_msg();
			tell_neighbor();
			System.out.println("already left the network");
		}
		/**
		 * send its own suc and suc_suc information to its predecessor
		 * send its own pre and pre_pre information to its successor
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		void tell_neighbor() throws UnknownHostException, IOException{
			String cmd_to_pre="your_suc_le "+peer.getId()+" "+peer.fingerTable.sucId+" "+peer.fingerTable.sucIP+" "+peer.fingerTable.sucPort+" "+peer.fingerTable.sucsucId+" "+peer.fingerTable.sucsucIP+" "+peer.fingerTable.sucsucPort;
			String cmd_to_suc="your_pre_le "+peer.getId()+" "+peer.fingerTable.preId+" "+peer.fingerTable.preIP+" "+peer.fingerTable.prePort+" "+peer.fingerTable.prepreId+" "+peer.fingerTable.prepreIP+" "+peer.fingerTable.preprePort;
			
			Socket socket=new Socket(peer.fingerTable.preIP,peer.fingerTable.prePort);
			PrintWriter pw=getWriter(socket);
			pw.println(cmd_to_pre);
			socket.close();
			
			socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			pw=getWriter(socket);
			pw.println(cmd_to_suc);
			socket.close();
			/**
			 * test code
			 */
			String cmd="set_your_suc_suc "+peer.fingerTable.sucId+" "+peer.fingerTable.sucIP+" "+peer.fingerTable.sucPort;
			socket=new Socket(peer.fingerTable.prepreIP,peer.fingerTable.preprePort);
			pw=getWriter(socket);
			pw.println(cmd);
			socket.close();
			
			cmd="set_your_pre_pre "+peer.fingerTable.preId+" "+peer.fingerTable.preIP+" "+peer.fingerTable.prePort;
			socket=new Socket(peer.fingerTable.sucsucIP,peer.fingerTable.sucsucPort);
			pw=getWriter(socket);
			pw.println(cmd);
			socket.close();
		}
		/**
		 * When a node wants to leave the ring, it has to transfer its keys
		 * to its successor
		 * 
		 * @throws UnknownHostException
		 * @throws IOException
		 */
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
		/**
		 * circulate the leava msg in the ring, all the nodes in the network 
		 * will update their finger table
		 */
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
	
	/**
	 * This class is used to handle search command
	 * 
	 *
	 */
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
		/***
		 * If the local doesn't have the key,
		 * use the finger table to find the next node,
		 * which may contain the key.
		 * @param nextIp
		 * @param nextPort
		 * @param localip
		 * @param localport
		 * @param localid
		 * @param ttl
		 * @param get
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		void searchNext(String nextIp,int nextPort,String localip, int localport, int localid, int ttl, int get) throws UnknownHostException, IOException{
			Socket socket=new Socket(nextIp,nextPort);
			PrintWriter pw=getWriter(socket);
			String new_cmd="search "+searchId+" "+localip+" "+localport+" "+ttl+" "+get; 
			pw.println(new_cmd);
			socket.close();
		}
		
	}
	/**
	 * This class is used to get the data item
	 * 
	 *
	 */
	class Get{
		int key;
		Get (int key) {
			this.key=key;
		}
		/**
		 * use search class to find out the key's address and data item
		 */
		void start_get(){
			Search search=new Search(key);
		}
	}
	
	/**
	 * This thread is call periodically 
	 * It detect its predecessor to see if it works
	 * If there is something wrong with the predecessor, 
	 * It will take actions to reconstruct the affected part in the ring
	 * @author jordan
	 *
	 */
	
	class Stable extends Thread{
		
		int detect_id;
		String detect_ip;
		int detect_port;
		
		
		public Stable(int id,String ip,int port){
			this.detect_id=id;
			this.detect_ip=ip;
			this.detect_port=port;
		}
		@Override
		public void run(){
			while(true){
				if (!detectPre()){
					preRecover();
					sendRecover();
				}
				
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("sleep error");
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * rule out the node's predecessor in the ring.
		 */
		void preRecover(){
			String ip=peer.fingerTable.prepreIP;
			int port=peer.fingerTable.preprePort;
			int id=peer.fingerTable.prepreId;
			
			
			String cmd="your_suc_le "+peer.getId()+" "+peer.getId()+" "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.fingerTable.sucId+" "+peer.fingerTable.sucIP+" "+peer.fingerTable.sucPort;
			
			Socket socket=null;
			try {
				socket=new Socket(ip,port);
			} catch (IOException e) {
				
				e.printStackTrace();
				return;
			}
			PrintWriter pw=getWriter(socket);
			
			pw=getWriter(socket);
			pw.println(cmd);  //pre's suc and suc_suc updated
			
			
			cmd="your_pre_pre_le "+peer.getId()+" "+id+" "+ip+" "+port;
			
			try {
				socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			}
			 catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			pw=getWriter(socket);
			pw.println(cmd);// after this, neighbours are all updated
			
			//below will update self
			peer.fingerTable.preId=peer.fingerTable.prepreId;
			peer.fingerTable.preIP=peer.fingerTable.prepreIP;
			peer.fingerTable.prePort=peer.fingerTable.preprePort;
			
			try {
				socket=new Socket(peer.fingerTable.preIP,peer.fingerTable.prePort);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			cmd="yourpre";
			pw=getWriter(socket);
			pw.println(cmd);
			BufferedReader br=getReader(socket);
			String rec="";
			try {
				rec=br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			String []s=rec.split(" ");
			peer.fingerTable.prepreId=Integer.parseInt(s[2]);
			peer.fingerTable.prepreIP=s[0];
			peer.fingerTable.preprePort=Integer.parseInt(s[1]);
		    
			//update own's key list
			for (int i=0; i<peer.fingerTable.sucTable.size();i++){
				int fingerNode=peer.fingerTable.sucTable.get(i);
				if (fingerNode == detect_id)
					{peer.fingerTable.sucTable.set(i, peer.getId());
					 peer.fingerTable.ipTable.set(i, peer.fingerTable.ip+" "+peer.fingerTable.port);
					}
			}
		}
		
	/**
	 * Circulate a leave msg to tell others that a node has left the ring
	 */
		void sendRecover(){
			String cmd="le "+this.detect_id+" "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId()+" "+16;
			this.detect_id=peer.fingerTable.preId;
			this.detect_ip=peer.fingerTable.preIP;
			this.detect_port=peer.fingerTable.prePort;
			Socket socket=null;
			try {
				socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			PrintWriter pw=getWriter(socket);
			pw.println(cmd);
			
		}
		
		/*boolean detectSuc(){
			
			String sucIp=peer.fingerTable.sucIP;
			int sucPort=peer.fingerTable.sucPort;
			
			Socket socket=null;
			try {
				socket = new Socket(sucIp,sucPort);
			} catch (Exception e) {
			    
				System.out.println("suc is lost");
				return false;
			}
			PrintWriter pw=getWriter(socket);
			pw.println("ping");
			BufferedReader br=getReader(socket);
			String rec_msg="";
			try {
				rec_msg = br.readLine();
			} catch (IOException e) {
				System.out.println("suc lost");
				return false;
			}
			//socket.close();
			if (rec_msg.equals("pong"))
				return true;
			return false;
		}*/
		/***
		 * detect predecessor to see if it works 
		 * @return
		 */
		
		boolean detectPre(){
			String preIp=peer.fingerTable.preIP;
			int prePort=peer.fingerTable.prePort;
			
			Socket socket=null;
			try {
				socket = new Socket(preIp,prePort);
			} catch (Exception e) {
			    
				System.out.println("pre is lost");
				return false;
			}
			PrintWriter pw=getWriter(socket);
			pw.println("ping");
			BufferedReader br=getReader(socket);
			String rec_msg="";
			try {
				rec_msg = br.readLine();
			} catch (IOException e) {
				System.out.println("pre lost");
				return false;
			}
			//socket.close();
			if (rec_msg.equals("pong"))
				return true;
			return false;
		}
		
	}
	
	//LS class handle the finger display function
	
	class LS {
		
		public void list(){
			peer.display();
		}
	}
	
}