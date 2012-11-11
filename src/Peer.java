import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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
public static String FILE_DIRECTORY="file/";


FingerTable fingerTable;
//public static int PEER_SERVER_PORT

/**
 * 
 * @param initPort
 * @throws Exception 
 * @throws IOException
 * the exception has to be captured and prompt user to input another port number
 * 
 */
Peer (int initPort, int identifier) throws Exception {

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

/**
 * may not be used
 * @return
 * @throws SocketException
 */
public static String getLocalIp() throws SocketException

{	Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

    while (interfaces.hasMoreElements()){
    	NetworkInterface current = interfaces.nextElement();
    	System.out.println(current);
    	if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
    		Enumeration<InetAddress> addresses = current.getInetAddresses();
    		while (addresses.hasMoreElements()){
    			InetAddress current_addr = addresses.nextElement();
    			if (current_addr.isLoopbackAddress()) continue;
    			if (current_addr instanceof Inet4Address)
    				  return current_addr.getHostAddress();
    }
}
    return "-1";

}


class FingerTable{
	
	int identifier;
	int tableSize;
	
	String ip;
	int port;
	
	int preId;
	int sucId;
	
	String preIP;
	int prePort;
	
	String sucIP;
	int sucPort;
	
	ArrayList <Integer> sucTable;    // store successor's Node Id
	ArrayList <String>  ipTable;
	//ArrayList<Integer> preTable;
	Map<String, Boolean> keyList;
	Map<Integer,String> nodeIpMap; //convert nodeId to ip address.
	
	public FingerTable(int identifier, int tableSize) throws Exception{
		this.identifier=identifier;
		this.tableSize=tableSize;
		sucTable=new ArrayList<Integer>();
		ipTable=new ArrayList<String>();
		port=8000+identifier;
		ip=getLocalIp();
		//preTable=new ArrayList<Integer>();
		keyList=new HashMap<String, Boolean>();
		//nodeIpMap=new HashMap<Integer,String>();
		init();
	}
	
	public String getIp(int i){
		return ipTable.get(i);
	}
	public int getSucTableElement(int i){
		return sucTable.get(i);
	}
	
	/*public int getPreTableElement(int i){
		return preTable.get(i);
	}*/
	
	/**
	 * 
	 * @param i
	 * @return something like 192.168.1.3:8000
	 */
	
	
	public void init(){
		//succ_succ=0;
				//prede_prede=0;
				//nodeIpMap.put(0,getLocalIp()+":"+ getPort());	
				
		for (int i=0; i<tableSize; i++)  //init finger table
		{	sucTable.add(0); 
		    ipTable.add(" ");
			
			}
		
		
		if (identifier==0)       // node 0 has to be the first node and owns all the files at the beginning
		{
			for (int i=0; i<tableSize; i++){
				ipTable.set(i, ip+" "+port);
			}
			sucId=0;
			preId=0;
			sucIP=ip;
			preIP=ip;
			sucPort=Peer.DEFAULT_DEST_PORT;
			prePort=Peer.DEFAULT_DEST_PORT;
		 for (int i=0; i<16; i++)   // all the files contained in node 0 at first.
			keyList.put(String.valueOf(i), true);
	    }
		
		else{         //for other nodes
			sucId=-1;
			preId=-1;
		 for (int i=0; i<16; i++)
			 keyList.put(String.valueOf(i), false);
		}
	}
	
	//according it's own table to set up a finger table for 
	int getPreId (){
	
		return preId;
		
	}
	
	int getSuccessorId(){
		
		return sucId;
		
	}
	
	public void display(){
		int k=0;
		System.out.println(getId()+"'s fingerTable");
		System.out.println("successor: "+sucId+" "+sucIP+" "+sucPort+" predecessor: "+preId+" "+preIP+" "+prePort);
		for (int i=0; i<tableSize; i++){
			
			System.out.println(i+"   "+"from "+(identifier+Math.pow(2, i))%16+" to "+(identifier+Math.pow(2, i+1))%16+"  "+sucTable.get(i)+"  "+ipTable.get(i));
			//k=(int) Math.pow(2, i);
		}
	}
	
}}
	
