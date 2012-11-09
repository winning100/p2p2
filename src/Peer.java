import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Peer{
	
private	int port;
private int identifier;
private ServerSocket serverSocket;
private Socket socket;

public static Map<Integer,Integer> idToPort;


public static String DEFAULT_DEST_IP="localhost";
public static int DEFAULT_DEST_PORT=8000;


FingerTable fingerTable;
//public static int PEER_SERVER_PORT

/**
 * 
 * @param initPort
 * @throws IOException
 * the exception has to be captured and prompt user to input another port number
 * 
 */
Peer (int initPort, int identifier) {

	port=initPort;
	this.identifier=identifier;
	fingerTable=new FingerTable(this.identifier,4);  //4 is the table size
	idToPort=new HashMap<Integer,Integer>();
	for (int i=0;i<16; i++){
		idToPort.put(i, 9000+i);
	}
}


public void display(){
	fingerTable.display();
}

public int getPort(){
	return port;
}


public void setPort(int newPort){
	port=newPort;
}
	

public int getId(){
	return identifier;
}

}


class FingerTable{
	
	int identifier;
	int tableSize;
	int successor;
	int predecessor;
	int succ_succ;
	int prede_prede;
	ArrayList <Integer> table;    // store successor's Node Id
	Map<String, Boolean> fileList;
	Map<Integer,String> nodeIpMap; //convert nodeId to ip address.
	
	public FingerTable(int identifier, int tableSize){
		this.identifier=identifier;
		this.tableSize=tableSize;
		table=new ArrayList<Integer>();
		fileList=new HashMap<String, Boolean>();
		init();
	}
	
	
	public String getIp(int i){
		if (nodeIpMap.containsKey(i))
			return nodeIpMap.get(i);
		
		return null;
	}
	
	public void init(){
		
		if (identifier==0)       // node 0 has to be the first node and owns all the files at the beginning
		{
		successor=0;
		predecessor=0;
		succ_succ=0;
		prede_prede=0;
			
		for (int i=0; i<tableSize; i++)
			table.add(0);
		
		 for (int i=0; i<16; i++)
			fileList.put(String.valueOf(i), true);
	    }
		else{
		 for (int i=0; i<16; i++)
			 fileList.put(String.valueOf(i), false);
		}
	}
	
	//according it's own table to set up a finger table for 
	int getPredecessorId (int id){
	
		int pre=0;
		
		return pre;
		
	}
	
	int getSuccessorId(int id){
		
		int suc=0;
		
		return suc;
		
	}
	
	public void display(){
		int k=0;
		for (int i=0; i<tableSize; i++){
			System.out.println("successor: "+successor+" predecessor: "+predecessor);
			System.out.println(i+"   "+"from "+(identifier+Math.pow(2, i))%16+" to "+(identifier+Math.pow(2, i+1))%16+"  "+table.get(i));
			//k=(int) Math.pow(2, i);
		}
	}
	
}
	
