import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

/***
 * 
 * This thread is used to handle each request
 *
 */
class RequestHandle extends Thread{
	Socket socket;
	Peer peer;
	//get the io reader of socket
	public BufferedReader getReader(){
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
	
	//get the io reader of socket
	public static BufferedReader getReader(Socket socket){
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
	
	
	// get the io writer of socket
	public PrintWriter getWriter(){
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
	
	//get the io writer of the socket
	public static PrintWriter getWriter(Socket socket){
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
	
	
	
	public RequestHandle(Socket socket, Peer peer){
		this.socket=socket;
		this.peer=peer;
	}
	
	@Override
	public void run(){
		//InputStream in=new InputStream(socket.getInputStream());
	
		BufferedReader br=getReader();
		
		//System.out.println("server: "+0);
		String cmd_rec="";
		
		try {
			//System.out.println("server: "+1);
			cmd_rec=br.readLine(); //read command
		} catch (IOException e) {
			System.out.println("receive command error");
			e.printStackTrace();
			return;
		}
		
		if (!cmd_rec.startsWith("ping"))
	    System.out.println("cmd_rec is "+cmd_rec);
	    
	    String []str=cmd_rec.split(" ");
	    //int requested_id=Integer.parseInt(str[1]);
	    //Thread thread=null;
	   
	    
	    //send back suc
	   if (str[0].equals("yoursuc")){
	    	PrintWriter pw=getWriter();
	    	pw.println(peer.fingerTable.sucIP+" "+peer.fingerTable.sucPort+" "+peer.fingerTable.sucId);
	    	return;
	    }
	   // send back pre
	    else if(str[0].equals("yourpre")){
	    	PrintWriter pw=getWriter();
	    	pw.println(peer.fingerTable.preIP+" "+peer.fingerTable.prePort+" "+peer.fingerTable.preId);
	    	return;
	    }
	   // send back closestprecedingfinger
	    else if (str[0].equals("closestprecedingfinger")){
	    	int id=Integer.parseInt(str[1]);
	    	String kString=get_closest_preceding_finger(id);
	    	//String []kstr=kString.split(" ");
	    	PrintWriter pw=getWriter();
	    	pw.println(kString);
	    }
	   // new successor joins, update own successor
	    else if (str[0].equals("newSuc")){
	    	int new_sucid=Integer.parseInt(str[3]);
	    	int new_sucport=Integer.parseInt(str[2]);
	    	String new_sucIp=str[1];
	    	updateSuc(new_sucid,new_sucIp,new_sucport);
	    	
	    	int suc_suc=Integer.parseInt(str[4]);
	    	String suc_suc_ip=str[5];
	    	int suc_suc_port=Integer.parseInt(str[6]);
	    	
	    	updateSucSuc(suc_suc,suc_suc_ip,suc_suc_port);
	    }
	   //new predecessor joins, update own predecessor
	    else if (str[0].equals("newPre")){
	    	int new_preid=Integer.parseInt(str[3]);
	    	int new_preport=Integer.parseInt(str[2]);
	    	String new_preIp=str[1];
		    updatePre(new_preid,new_preIp,new_preport);
		    
		    int pre_pre=Integer.parseInt(str[4]);
		    String pre_pre_ip=str[5];
		    int pre_pre_port=Integer.parseInt(str[6]);
		    updatePrePre(pre_pre,pre_pre_ip,pre_pre_port);
	    }
	   //update own sucessor's successor
	    else if (str[0].equals("set_your_suc_suc")){
	    	int suc_suc=Integer.parseInt(str[1]);
	    	String suc_ip=str[2];
	    	int suc_port=Integer.parseInt(str[3]);
	    	
	    	updateSucSuc(suc_suc,suc_ip,suc_port);
	    }
	   //update own predecessor's predecessor
	    else if (str[0].equals("set_your_pre_pre")){
	    	int pre_pre=Integer.parseInt(str[1]);
	    	String pre_ip=str[2];
	    	int pre_port=Integer.parseInt(str[3]);
	    	
	    	updateSucSuc(pre_pre,pre_ip,pre_port);
	    	
	    }
	   //new nodes joins in
	    else if (str[0].equals("new_node")){
	    	int new_id=Integer.parseInt(str[3]);
	    	String new_ip=str[1];
	    	int new_port=Integer.parseInt(str[2]);
	    	try {
				new_node_come(new_id,new_ip,new_port);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    }
	   //transfer key request
	    else if (str[0].equals("transfer_key")){
	    	String new_ip=str[1];
	    	int new_port=Integer.parseInt(str[2]);
	    	int new_id=Integer.parseInt(str[3]);
	    	int new_preid=Integer.parseInt(str[4]);
	    	try {
				transfer_keys(new_ip,new_port,new_id,new_preid);
			} catch (Exception e) {
			
				e.printStackTrace();
			}
	    }
	    //request for the data item
	    else if(str[0].equals("give_you_data_item"))
	    {// cmd: give_you_data_item key#this is data item 1 
	    	String []s=cmd_rec.split("#");
	    	String value=s[1];
	    	String []k=s[0].split(" ");
	    	int key=Integer.parseInt(k[1]);
	    	receive_data_item(key, value);
	    	
	    }
	    // handle for search command
	    else if (str[0].equals("search")){
	    	int search_id=Integer.parseInt(str[1]);
	    	String origin_ip=str[2];
	    	int origin_port=Integer.parseInt(str[3]);
	    	int ttl=Integer.parseInt(str[4]);
	    	int get=Integer.parseInt(str[5]);
	    	try {
				search_data(search_id,origin_ip,origin_port,ttl,get);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    //after send search request, the result will be contained in the find command
	    else if (str[0].equals("find")){
	    	int result=Integer.parseInt(str[1]);
	    	if (result==-1){
	    		System.out.println("search result: data not in the ring");
	    		return;}
	    	get_data_addr(str[2],Integer.parseInt(str[3]),Integer.parseInt(str[4]), Integer.parseInt(str[5]),Integer.parseInt(str[6]));
	    	
	    }
	    // handle le request
	    else if (str[0].equals("le")){
	    	int le_id=Integer.parseInt(str[1]);
	    	String le_suc_ip=str[2];
	    	int le_suc_port=Integer.parseInt(str[3]);
	    	int le_suc_id=Integer.parseInt(str[4]);
	    	int ttl=Integer.parseInt(str[5]);
	    	try {
				rec_leave(le_suc_ip,le_suc_port,le_id,le_suc_id,ttl);
			} catch (Exception e) {
			
				e.printStackTrace();
			}
	    }
	   //suc is leaving
	    else if (str[0].equals("your_suc_le")){
	    	int next_id=Integer.parseInt(str[2]);
    		String next_ip=str[3];
    		int next_port=Integer.parseInt(str[4]);
    		
    		int next_next_id=Integer.parseInt(str[5]);
    		String next_next_ip=str[6];
    		int next_next_port=Integer.parseInt(str[7]);
    		
    		updateSuc(next_id,next_ip,next_port,next_next_id, next_next_ip, next_next_port);
    		
    	}
	   //predecessor is leaving
	    else if (str[0].equals("your_pre_le")){
	    	int next_id=Integer.parseInt(str[2]);
    		String next_ip=str[3];
    		int next_port=Integer.parseInt(str[4]);
    		
    		int next_next_id=Integer.parseInt(str[5]);
    		String next_next_ip=str[6];
    		int next_next_port=Integer.parseInt(str[7]);
    		updatePre(next_id,next_ip,next_port,next_next_id,next_next_ip,next_next_port); 
	    	
	    }
	    //pre's pre is leaving
	    else if (str[0].equals("your_pre_pre_le")){
	    	int next_id=Integer.parseInt(str[2]);
	    	String next_ip=str[3];
	    	int next_port=Integer.parseInt(str[4]);
	    	updatePrePre(next_id,next_ip,next_port);
	    }
	    //suc's suc is leaving
	    else if (str[0].equals("your_suc_suc_le")){
	    	int next_id=Integer.parseInt(str[2]);
	    	String next_ip=str[3];
	    	int next_port=Integer.parseInt(str[4]);
	    	updateSucSuc(next_id,next_ip,next_port);
	    }
	   //ping msg to detect if the node works, send back a "pong" message
	    else if (str[0].equals("ping")){
	    	PrintWriter pw=getWriter(socket);
	    	pw.println("pong");
	    }
	    //empty command	
	    else if (str[0].equals("")){
	    	
	    }
	    
	    
	}
	
	void rec_leave(String le_ip, int le_port, int le_id, int le_suc_id, int ttl) throws UnknownHostException, IOException{
		if (ttl<0)
			return;
		if (le_id==peer.getId())
			return;
		//rec_leave(le_suc_ip,le_suc_port,le_id,le_suc_id,ttl);
		for (int i=0; i<peer.fingerTable.tableSize;i++){
			int finger_node=peer.fingerTable.getSucTableElement(i);
			if (finger_node==le_id){
				peer.fingerTable.sucTable.set(i,le_suc_id);
				peer.fingerTable.ipTable.set(i, le_ip+" "+le_port);
			}
		}
		// forward the leave msg to suc.
		Socket socket=new Socket(peer.fingerTable.sucIP,peer.fingerTable.sucPort);
		String new_cmd="le "+le_id+" "+le_ip+" "+le_port+" "+le_suc_id+" "+(ttl-1);
		PrintWriter pw=getWriter(socket);
		pw.println(new_cmd);
		socket.close();
		
	}
	/**
	 * get new data
	 * @param des_ip
	 * @param des_port
	 * @param des_id
	 * @param search_id
	 * @param get
	 */
	void get_data_addr(String des_ip, int des_port, int des_id, int search_id, int get){
			
		System.out.println("search result for "+search_id+" : "+des_ip+" "+des_port+" "+des_id);
		
		if (get==1){
			peer.fingerTable.keyList.put(search_id, "This is data item "+search_id);
			System.out.println("already get the data");
		}
			
			
	}
	/**
	 * 
	 * @param searchid
	 * @param origin_ip
	 * @param origin_port
	 * @param ttl
	 * @param get
	 * @throws IOException
	 * handle search request, it may forward this request to its suc
	 */
	void search_data(int searchid, String origin_ip, int origin_port, int ttl, int get) throws IOException
	{
		Socket socket=new Socket(origin_ip,origin_port);
		PrintWriter pw=getWriter(socket);
		String new_cmd="";
		if (ttl<0)
		{
			new_cmd="find "+"-1 "+searchid;
			pw.println(new_cmd);
			socket.close();
			return;
		}
		if (peer.fingerTable.keyList.containsKey(searchid)){
			new_cmd="find "+"0 "+peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId()+" "+searchid+" "+get;
			pw.println(new_cmd);
			socket.close();
			return;
		}
		
		//forward the search request to proper finger
		else{
			int low=peer.getId()+1;
			int high=-1;
			
			for (int i=0; i<4; i++){
				high=(int) ((low+Math.pow(2, i))%16);
				if (RequestHandle.inRange(searchid, low, high, false, true))
				{   String []p=peer.fingerTable.getIp(i).split(" ");
				    String nextIp=p[0];
				    int nextPort=Integer.parseInt(p[1]);
				   
				    //send out search information
<<<<<<< HEAD
<<<<<<< HEAD
				    
=======
>>>>>>> 2ddc4d13d63ea241bf679a53381200d4c2d1659f
=======
				    
>>>>>>> 4ba1dcbef2e187f0876aa1c516210e0c5647d9f5
				    System.out.println(searchid+"in range from "+low+" to "+high);
				    System.out.println("forward addr: "+nextIp+" "+nextPort);
					searchNext(nextIp,nextPort,origin_ip, origin_port, searchid,ttl-1, get);
					return;
				}	
		     }
		   }
  	 }
	//forward search reqeust to suc
	void searchNext(String next_ip,int next_port, String origin_ip, int origin_port, int searchid, int ttl, int get) throws IOException{
		Socket socket=new Socket(next_ip,next_port);
		PrintWriter pw=getWriter(socket);
		String new_cmd="search "+searchid+" "+origin_ip+" "+origin_port+" "+ttl+" "+get; 
		pw.println(new_cmd);
		socket.close();
	}
	//handle transfer_key request
	void transfer_keys(String new_ip, int new_port, int new_id, int new_pre) throws UnknownHostException, IOException{
		ArrayList<Integer> removedList=new ArrayList<Integer>();
		for (Map.Entry<Integer, String> entry : peer.fingerTable.keyList.entrySet()) {
			   int key = entry.getKey();
			   String value = entry.getValue();
			   
			   if (inRange(key, new_pre, new_id,true, false)){
				   transfer_data_item(new_ip, new_port, key,value );
				   removedList.add(key);
			   }
			   
			   
			   //System.out.println("key=" + key + " value=" + value);
			  }
		for (int i=0; i<removedList.size();i++){
			int k=removedList.get(i);
			peer.fingerTable.keyList.remove(k);
		}
	}
	
	void transfer_data_item(String new_ip, int new_port, int key, String value) throws UnknownHostException, IOException{
		Socket socket=new Socket(new_ip, new_port);
		PrintWriter pw=getWriter(socket);
		pw.println("give_you_data_item "+key+"#"+value);
		//peer.fingerTable.keyList.remove(key);
		socket.close();
	}
	
	//put new data item into the key list
	void receive_data_item(int key, String value){
		peer.fingerTable.keyList.put(key, value);
	}
	// new node joins in the network
	void new_node_come(int id, String ip, int port) throws UnknownHostException, IOException{
		int localid=peer.getId();
		if (id==localid)
		return;
		for (int i=0; i<4; i++){
			int low=(int) (peer.getId()+Math.pow(2, i))%16;
			//int high=(int)(peer.getId()+Math.pow(2, i+1))%16;
			int finger=peer.fingerTable.getSucTableElement(i);
			System.out.println("id: "+id);
			System.out.println("low: "+low);
			System.out.println("finger: "+finger);
			
			int new_distance=id-low;
			if (new_distance <0)
				new_distance+=16;
			
			int cur_distance=finger-low;
			if (cur_distance <0)
				cur_distance+=16;
			
			System.out.println("++++new dis: "+new_distance+" ++++");
			System.out.println("++++cur dis: "+cur_distance+" ++++");
			if (new_distance<cur_distance){
				peer.fingerTable.sucTable.set(i, id);
				peer.fingerTable.ipTable.set(i, ip+" "+port);
			}
		}
		
		String cmd="new_node "+ip+" "+port+" "+id;
		Socket socket=new Socket(peer.fingerTable.sucIP, peer.fingerTable.sucPort);
		PrintWriter pw=getWriter(socket);
		pw.println(cmd);
		socket.close();
	}
	
	
	
	/*void update_finger_table(int id,int rank) throws UnknownHostException, IOException{
		int n=peer.getId();
		int s=id;
		int i=rank;
		
		if (inRange(s,n,peer.fingerTable.getSucTableElement(i),false,true))
			{peer.fingerTable.sucTable.set(i, s);
			 int p=peer.fingerTable.preId;
			 //if (p==n)
				// return;
			 Socket socket=new Socket(Peer.DEFAULT_DEST_IP,8000+p);
			 PrintWriter pw=getWriter(socket);
			 String cmd="update_node_finger_table "+s+" "+i;
			
			 pw.println(cmd);
			 
			 }
	}*/
	//update suc's suc
	void updateSucSuc(int new_sucid,String new_sucip,int new_sucport){
		peer.fingerTable.sucsucId=new_sucid;
		peer.fingerTable.sucsucPort=new_sucport;
		peer.fingerTable.sucsucIP=new_sucip;
	}
	//update pre's pre
	void updatePrePre(int new_id,String new_ip, int new_port){
		peer.fingerTable.prepreId=new_id;
		peer.fingerTable.prepreIP=new_ip;
		peer.fingerTable.preprePort=new_port;
	}
	
	//update suc
	void updateSuc(int new_sucid,String new_sucip,int new_sucport){
		peer.fingerTable.sucId=new_sucid;
		peer.fingerTable.sucPort=new_sucport;
		peer.fingerTable.sucIP=new_sucip;
	}
	//update suc and suc's suc
	void updateSuc(int new_sucid,String new_sucip,int new_sucport, int suc_suc_id, String suc_suc_ip, int suc_suc_port){
		peer.fingerTable.sucId=new_sucid;
		peer.fingerTable.sucPort=new_sucport;
		peer.fingerTable.sucIP=new_sucip;
		
		peer.fingerTable.sucsucId=suc_suc_id;
		peer.fingerTable.sucsucPort=suc_suc_port;
		peer.fingerTable.sucsucIP=suc_suc_ip;
	}
	//update pre
	void updatePre(int new_preid, String new_preip,int new_preport){
		peer.fingerTable.preId=new_preid;
		peer.fingerTable.prePort=new_preport;
		peer.fingerTable.preIP=new_preip;
	}
	//update pre's pre
	void updatePre(int new_preid, String new_preip,int new_preport, int pre_pre_id, String pre_pre_ip, int pre_pre_port){
		peer.fingerTable.preId=new_preid;
		peer.fingerTable.prePort=new_preport;
		peer.fingerTable.preIP=new_preip;
		
		peer.fingerTable.prepreId=pre_pre_id;
		peer.fingerTable.prepreIP=pre_pre_ip;
		peer.fingerTable.preprePort=pre_pre_port;
		
	}
	/**
	 * 
	 * @return peer's successor
	 */
	/*public int getSuc(){
	  return	peer.fingerTable.;
	}*/
	
	/***
	 * return closest finger preceding id
	 * @param id
	 * @return
	 */
	public String get_closest_preceding_finger(int id){
		for (int i=3; i>=0; i--){
			int node=peer.fingerTable.getSucTableElement(i);
			if (inRange(node,peer.getId(),id,true,true))
				return peer.fingerTable.getIp(i)+" "+node;
		}
		return peer.fingerTable.ip+" "+peer.fingerTable.port+" "+peer.getId();
	}
	/**
	 * Give an id, judge if it's in the range from low to high in clockwise
	 * @param id
	 * @param low
	 * @param high
	 * @param low_open
	 * @param high_open
	 * @return
	 */
	public static boolean inRange(int id, int low, int high, boolean low_open, boolean high_open){
		if (low<high)
		{if (low_open && high_open){
			if (id>low && id<high)
				return true;
			else
				return false;
		   }
		else if (!low_open && high_open){
			if (id>=low && id<high)
				return true;
			else
				return false;
			}
		else if (low_open && !high_open){
			if (id>low && id<=high)
				return true;
			else
				return false;
		}
		else{
			if (id>=low && id<=high)
				return true;
			else
				return false;
		}	
		}
		
		else{
			if (low_open && high_open){
				if (id>low || id<high)
					return true;
				else
					return false;
							
			}
			else if (!low_open && high_open){
				if (id>=low || id<high)
					return true;
				else
					return false;
			}
				
			else if (low_open && !high_open){
				if (id>low || id<=high)
					return true;
				else
					return false;
			}
			else {
				if (id>=low || id<=high)
					return true;
				else
					return false;
			}
			
		}
		
		
		
	}
	
	

	/*class JoinHandle {
		String dest_ip;
		int dest_port;
		
		JoinHandle( String dest_ip, int dest_port){
			this.dest_ip=dest_ip;
			this.dest_port=dest_port;
		}
		
		void updateTable()
		{
			
			int identifier=peer.getId();
	
			
		}
		
	}*/
	
	/***
	 * 
	 * @author jordan
	 *
	 */
	class GetHandle{

		
	}
	
	class LeaveHandle {
		
	}
	
	class SearchHandle{
		
	}
	
}