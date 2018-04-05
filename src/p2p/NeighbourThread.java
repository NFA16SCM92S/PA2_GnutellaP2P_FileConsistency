package p2p;
import java.net.*;
import java.rmi.*;

//To manage Neighbour Peers
public class NeighbourThread extends Thread{
	private int fromNodeId, toNodeId, version, portNum, TTL;
	private String HostIP, fName, msgID, ops;
	private CollectQueryResult hitQuery = new CollectQueryResult();
	
	NeighbourThread(String msgID, String HostIP, int originNode, int portNum, String filename, int versionNumber, String ops) {
		this.msgID = msgID;
		this.HostIP = HostIP;
		this.fName = filename;
		this.fromNodeId = originNode;
		this.portNum = portNum;
		this.version = versionNumber;
		this.ops = ops;
	}
	NeighbourThread(String fName, String HostIP, int portNum, int fromNodeId, int toNodeId, String msgID, int TTL, String ops){
		this.fName = fName;
		this.HostIP = HostIP;
		this.portNum = portNum;
		this.fromNodeId = fromNodeId;
		this.toNodeId = toNodeId;
		this.msgID = msgID;
		this.TTL = TTL;
		this.ops = ops;
	}
	
	//Method to return Hit query result
	public CollectQueryResult getResult(){
		return hitQuery;
	}
	// creating connection using RMI
	@Override
	public void run() {
		SampleInterface nPeer=null;
		try {
			nPeer = (SampleInterface) Naming.lookup("rmi://" + HostIP + ":" + portNum + "/peerServer");		
			
			if (ops.equals("download")) {
				// if operation is download trigger query function
				hitQuery = nPeer.queryFunc(fName, fromNodeId, msgID, TTL);
			}
			else {
				//otherwise trigger Invalidation function
				nPeer.invalidateFunc(msgID, fromNodeId, fName, version);
			}		
			
		}
		// If node is not able to connect to other node according to the topology given in the confProp.properties file
		// catch the error and and throw exception 
		catch (MalformedURLException | RemoteException | NotBoundException e) {
			 System.out.println("\n***Exception Caught***\n This Node is not able to connect to Peer-" + toNodeId +"\n"+ e.getMessage());
		}
		
	}

}
