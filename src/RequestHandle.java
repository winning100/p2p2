import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/***
 * 
 * handle each request
 *
 */
class RequestHandle extends Thread{
	Socket socket;
	Peer peer;
	
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
			cmd_rec=br.readLine();
		} catch (IOException e) {
			System.out.println("receive command error");
			e.printStackTrace();
			return;
		}
		
		
	    System.out.println("cmd_rec is "+cmd_rec);
	    
	    String []str=cmd_rec.split(" ");
	    //int requested_id=Integer.parseInt(str[1]);
	    //Thread thread=null;
	    if (str[0].equals("joinMsg"))
	    	{
	    	JoinHandle joinHandle=new JoinHandle(Integer.parseInt(str[1]));
	    	
	    	}
	    
	    //send back suc
	    else if (str[0].equals("yoursuc")){
	    	PrintWriter pw=getWriter();
	    	pw.println(peer.fingerTable.sucId);
	    	
	    }
	    else if (str[0].equals("closestprecedingfinger")){
	    	int id=Integer.parseInt(str[1]);
	    	int k=get_closest_preceding_finger(id);
	    	PrintWriter pw=getWriter();
	    	pw.println(k);
	    }
	    else if (str[0].equals("newSuc")){
	    	int new_sucid=Integer.parseInt(str[1]);
	    	updateSuc(new_sucid);
	    }
	    else if (str[0].equals("newPre")){
	    	int new_preid=Integer.parseInt(str[1]);
		    	updatePre(new_preid);
	    }
	    else if (str[0].equals("update_node_finger_table")){
	    	int id=Integer.parseInt(str[1]);
	    	int rank=Integer.parseInt(str[2]);
	    	try {
				update_finger_table(id,rank);
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    else if (str[0].equals("new_node")){
	    	int new_id=Integer.parseInt(str[1]);
	    	try {
				new_node_come(new_id);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    }
	    else if (str[0].equals("")){
	    	
	    }
	    
	    
	}
	
	void new_node_come(int id) throws UnknownHostException, IOException{
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
			}
		}
		
		String cmd="new_node "+id;
		Socket socket=new Socket(Peer.DEFAULT_DEST_IP, 8000+peer.fingerTable.sucId);
		PrintWriter pw=getWriter(socket);
		pw.println(cmd);
		socket.close();
	}
	
	
	
	void update_finger_table(int id,int rank) throws UnknownHostException, IOException{
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
	}
	
	void updateSuc(int new_sucid){
		peer.fingerTable.sucId=new_sucid;
	}
	void updatePre(int new_preid){
		peer.fingerTable.preId=new_preid;
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
	public int get_closest_preceding_finger(int id){
		for (int i=3; i>=0; i--){
			int node=peer.fingerTable.getSucTableElement(i);
			if (inRange(node,peer.getId(),id,true,true))
				return node;
		}
		return peer.getId();
	}
	
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
	
	
	/**
	 * use fingerTable find out the predecessor for a given Id
	 * @param id
	 * @return -1 if error
	 */
	/*int findPre(int id){
		int localId=peer.getId();
		
		int range1=(localId+1)%16;
		int range2=(localId+2)%16;
		int range3=(localId+4)%16;
		int range4=(localId+12)%16;
		
		if (range1 > range2)
		{
			if ( id >= range1 || id < range2)
				return peer.fingerTable.getPreTableElement(0);
		}
		else
		{
			if ( id>=range1 && id < range2)
				return peer.fingerTable.getPreTableElement(0);
		}
		
		
		if (range2 > range3)
		{
			if ( id >= range2 || id < range3)
				return peer.fingerTable.getPreTableElement(1);
		}
		else
		{
			if ( id>=range2 && id < range3)
				return peer.fingerTable.getPreTableElement(1);
		}
		
		if (range3 > range4)
		{
			if ( id >= range3 || id < range4)
				return peer.fingerTable.getPreTableElement(2);
		}
		else
		{
			if ( id>=range3 && id < range4)
				return peer.fingerTable.getPreTableElement(2);
		}
		
		if (range4 > range1)
		{
			if ( id >= range4 || id < range1)
				return peer.fingerTable.getPreTableElement(3);
		}
		else
		{
			if ( id>=range4 && id < range1)
				return peer.fingerTable.getPreTableElement(3);
		}
		
		
		return -1;
	}*/
	
	
	
	/*ArrayList<Integer> setFingerTable(int id){
		ArrayList<Integer> newTable=new ArrayList<Integer>();
		
	}*/
	
	/**
	 * 
	 * @author jordan
	 *
	 */
	class JoinHandle {
		int joinId;
		
		JoinHandle( int joinId){
			this.joinId=joinId;
		}
		
		
		
		
		void updateTable()
		{
			
			int identifier=peer.getId();
			
			//find successor of identifer+1, identifier+2,
			//identifier+4, identifier+8           %16
			
			/*for (int i=0; i<4; i++){
			    if (i!=3)
			    {
			    	if (joinId>=(identifier+Math.pow(2, i)%16) || joinId<(identifier+Math.pow(2, i+1))){
			    		int current_suc=peer.fingerTable.table.get(i);
			    		int joinIdDistance=Math.abs(joinId-current_suc);
			    		int originDistance=Math.abs(current_suc-peer.getId());
			    		if (originDistance==0)
			    			originDistance=16;
			    		
			    		if (originDistance > joinIdDistance)
			    			peer.fingerTable.table.set(i, joinId);
			    	}
			    		
			    }
				
			  //if ( ((peer.getId()+Math.pow(2, i))%16))	
			}*/
			//"from "+(identifier+Math.pow(2, i))%16+" to "+(identifier+Math.pow(2, i+1))%16
			
			
			
		}
		
	}
	
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