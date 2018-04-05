package p2p;
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

//Implementing interface SampleInterface for RMI
public class ImplementSampleInterface extends UnicastRemoteObject implements SampleInterface {
	
	private String myDownDir, mySDir; // <mySDir>/<ID>/<myDownDir>
	private int ID, currNodePort; // Peer ID + Port num
	private static final long versionID = 1L;	
	private PeerProcedure currNode;
	private ArrayList<String> interfaceMsg;

	// constructor parameters initialization
	ImplementSampleInterface(String dataDir, String downloadDir, int ID, int currNodePort, PeerProcedure currNode) throws RemoteException {
		super();
		this.mySDir = dataDir;
		this.myDownDir = downloadDir;
		this.ID = ID;
		this.currNodePort=currNodePort;
		this.currNode = currNode;
		this.interfaceMsg=new ArrayList<String>();
	}
	//RMI method to download file
	public synchronized byte[] fetchFile(String filename) throws RemoteException {
		byte[] bytesArr = null;	
		String fileNamePath = mySDir + "/" + filename;
		try {
			bytesArr = new byte[(int) (new File(fileNamePath)).length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileNamePath));
			input.read(bytesArr, 0, bytesArr.length);
			input.close();
		} catch (Exception e) {	System.out.println(e.getMessage());}		
		return bytesArr;
	}
	
	public synchronized FileConf fetchFileInfo(String filename) throws RemoteException {
		return queryCurrNode(filename);
	}

	//RMI func for invalidation
	public void invalidateFunc(String msgID, int originNode, String filename, int versionNumber) throws RemoteException {
		
		synchronized(this){
			if (interfaceMsg.contains(msgID)){ return; }	
		} 		
		interfaceMsg.add(msgID);
		FileConf info = queryCurrNode(filename);
		if(info != null){
			info.checkFState = "invalidate";
			System.out.println("Invalidate the file: "+ filename +" from Peer-" + ID);
		}	
		ArrayList<PeerConf> nPeer = new ArrayList<PeerConf>();
		ArrayList<NeighbourThread> peerThreads = new ArrayList<NeighbourThread>();
		currNode.neighboursList(nPeer, ID);	
		List<Thread> threads = new ArrayList<Thread>();
		for(PeerConf neighbouringPeer : nPeer){
			if (neighbouringPeer.ID == originNode)
				continue;		
			NeighbourThread ths = new NeighbourThread(msgID, neighbouringPeer.ipAddr, originNode, neighbouringPeer.portNum, filename, versionNumber, "invalidate");
			Thread ts = new Thread(ths);
			ts.start();
			threads.add(ts);
			peerThreads.add(ths);
		}
		
		try {
			for (Thread thread: threads)
				thread.join();
		} catch (InterruptedException e) { 	e.printStackTrace();}
			
	}

	// RMI function for search query
	public CollectQueryResult queryFunc(String fName, int fromNodeId, String msgID, int TTL) throws RemoteException {
		
		ArrayList<PeerConf> foundArr = new ArrayList<PeerConf>();
		ArrayList<PeerConf> nPeer = new ArrayList<PeerConf>();
		ArrayList<FileConf> fileInfo = new ArrayList<FileConf>();
		ArrayList<String> path = new ArrayList<String>();
		CollectQueryResult peerQueryOut = new CollectQueryResult();
		ArrayList<NeighbourThread> peerThreads = new ArrayList<NeighbourThread>();
		
		synchronized(this){
			if (interfaceMsg.contains(msgID)){
				System.out.println("Request for Peer-"+ID+" coming from Peer-"+fromNodeId+" Action: Do nothing --> this is a duplicate request, request already addressed with MsgId: " + String.valueOf(msgID));
				return peerQueryOut;	
			}
			if (TTL==0) {
				return peerQueryOut;	
			}
		} 
		interfaceMsg.add(msgID);
		System.out.println("Request for Peer-"+ID+" coming from Peer-"+fromNodeId+" Action: Search file on localhost and send request to the neighbouring peers, MsgID: " + String.valueOf(msgID));
		FileConf info = queryCurrNode(fName);
		if(info != null){
			System.out.println("Success: File found in localhost");
			PeerConf myObj = new PeerConf();
			myObj.ipAddr = "localhost";
			myObj.ID = ID;
			myObj.portNum = currNodePort;
			foundArr.add(myObj);		
			fileInfo.add(info);
		}

		currNode.neighboursList(nPeer, ID);
		if (nPeer.size() == 0)
			path.add(Integer.toString(ID));
		
		TTL--;
		List<Thread> threads = new ArrayList<Thread>();
		for(PeerConf neighbouringPeer : nPeer){
			if (neighbouringPeer.ID == fromNodeId)
				continue;
			NeighbourThread ths = new NeighbourThread(fName, neighbouringPeer.ipAddr, neighbouringPeer.portNum,ID, neighbouringPeer.ID, msgID, TTL, "download");  
			Thread ts = new Thread(ths);
			ts.start();
			threads.add(ts);
			peerThreads.add(ths);
		}
			
		try {
			for (Thread thread: peerThreads)
				thread.join();
		} catch (InterruptedException e) { e.printStackTrace(); }

		
		for (NeighbourThread peerThread: peerThreads){
			CollectQueryResult myObj =  peerThread.getResult();
			if(myObj.resultArr.size()>0)
				foundArr.addAll(myObj.resultArr);
			
			for (int count=0;count<myObj.pathArr.size();count++)
				path.add(ID + myObj.pathArr.get(count));
		}
		
		if (path.size()==0)
			path.add(Integer.toString(ID));
		peerQueryOut.resultArr.addAll(foundArr);
		peerQueryOut.pathArr.addAll(path);	
		return peerQueryOut;
	}

	//Query current peer for file
	private FileConf queryCurrNode(String filename) {
		for (FileConf info: currNode.localFiles)
			if (info.fName.equals(filename)) {
				return info;
			}
		return null;
	}
	private FileConf queryCurrForDownload(String filename) {
		for (FileConf info: currNode.downloadFiles)
			if (info.fName.equals(filename)) {
				return info;
			}
		return null;
	}
	

}
