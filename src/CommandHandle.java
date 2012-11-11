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