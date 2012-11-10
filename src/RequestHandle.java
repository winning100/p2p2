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
	    Thread thread=null;
	    if (str[0].equals("joinMsg"))
	    	{
	    	JoinHandle joinHandle=new JoinHandle(Integer.parseInt(str[1]));
	    	try {
				joinHandle.processMsg();
			} catch (UnknownHostException e) {
				System.out.println("process join msg error");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("process join msg error");
				e.printStackTrace();
			}
	    	return;
	    	}
	    
	    else if (str[0].equals("findpre"))
    	{int preId=findPre(Integer.parseInt(str[1]));
    	   // System.out.println("server: "+1);
	    	PrintWriter pw=getWriter();
	    	pw.println(preId);
	    	//System.out.println("server: "+2);
	    	pw.println(peer.getId());
	    	//System.out.println("server: "+3);
	    	pw.println("localhost");
	    	//System.out.println("server: "+4);
	    	int preIdPort=preId+8000;
	    	pw.println(preId);
	    	
	    }
	    else if (str[0].equals("findsuc")){
	    	PrintWriter pw=getWriter();
	    	int sucId=findSuc(Integer.parseInt(str[1]));
	    	pw.println(sucId);
	    	pw.println(peer.getId());
	    	pw.println("localhost");
	    	pw.println(sucId);
	    }
	    else if (str[0].equals("")){
	    	
	    }
	    
	    
	}
	/**
	 * use fingerTable find out the predecessor for a given Id
	 * @param id
	 * @return -1 if error
	 */
	int findPre(int id){
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
	}
	
	int findSuc(int id){
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
	}
	
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
		
		public void processMsg() throws UnknownHostException, IOException{
			// the join msg has circulated the whole ring
			if (joinId==peer.getId())
			{   System.out.println("Node "+joinId+" join successfully");
				return;
			}
			peer.fingerTable.preId=joinId;
			updateTable();
			Socket socket=new Socket(Peer.DEFAULT_DEST_IP,peer.fingerTable.preId+8000);
			PrintWriter pw=getWriter(socket);
			//forward the msg to successor
			pw.println("joinMsg "+joinId);
			socket.close();
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
	
	class LeaveHandle extends Thread{
		
	}
	
	class SearchHandle extends Thread{
		
	}
	
}