import java.io.IOException;

/***
 * 
 *Wrapper initiate peer with parameters 
 *
 */

public class Wrapper{

	
public static void main(String []args){
	
	int port=8888;
	Peer peer=null;
	try {
		peer=new Peer(8888);
	} catch (IOException e) {
		e.printStackTrace();
		System.out.println("the port "+port+" is in use, try another one");
		//System.out.println("input a new port:");
	}
}	
	
	
}