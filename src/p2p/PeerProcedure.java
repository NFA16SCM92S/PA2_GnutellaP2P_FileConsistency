package p2p;
import java.io.*;
import java.nio.file.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

// All Peer related Operations/Procedures are written under PeerProcedure class
public class PeerProcedure {
	public Boolean isPush;
	private int TTL = 3;
	private int messageCtr = 0;
	private int TTR;	
	private String dataDir;	
	private SampleInterface stub;
	private ArrayList<PeerConf> peerOutput;	
	public ArrayList<FileConf> downloadFiles = new ArrayList<FileConf>();
	public ArrayList<FileConf> localFiles = new ArrayList<FileConf>();
	public PeerConf getInfo= new PeerConf();	

	// Initializing Peer with Id and Port#
	PeerProcedure(int ID, int portNum) {
		this.getInfo.ID = ID;
		this.getInfo.portNum = portNum;
		this.getInfo.ipAddr = "127.0.0.1";
		this.dataDir = "data/" + ID;
		selectApproach();
		runAsServer(this.getInfo.ID, this.getInfo.portNum, this.dataDir);
		if (!isPush)
			autoUpFunc();
	}
	
	// Setting approach from properties file
	private void selectApproach() {
		InputStream input;
		try {
			Properties prop = new Properties();
			input = new FileInputStream("confProp.properties");
			prop.load(input);
			isPush = prop.getProperty("approach").equalsIgnoreCase("push");		
			TTR = Integer.parseInt(prop.getProperty("TTR"));
		} catch (IOException e) { e.printStackTrace();}

	}
		
	// Search in nearby neighbours for the file
	public ArrayList<PeerConf> searchFile(String fNameLookup) throws InterruptedException {
		List<Thread> threads = new ArrayList<Thread>();
		peerOutput = new ArrayList<PeerConf>();
		ArrayList<PeerConf> nPeer = new ArrayList<PeerConf>();
		ArrayList<NeighbourThread> threadingForNeighbour = new ArrayList<NeighbourThread>();
		String msgId = generateMessageId();
		
		//get the list of neighbours for the given peer
		neighboursList(nPeer, getInfo.ID);			
		for (PeerConf neighbourP: nPeer) {			
			NeighbourThread connection = new NeighbourThread(fNameLookup, neighbourP.ipAddr, neighbourP.portNum, getInfo.ID, neighbourP.ID, msgId,TTL, "download");
			Thread threadInstance = new Thread(connection);
			threadInstance.start();		
			threads.add(threadInstance);
			threadingForNeighbour.add(connection);
		}
		for (Thread thread: threads)
			thread.join();
		for (NeighbourThread neighbourTh: threadingForNeighbour) {
			CollectQueryResult hitQuery = (CollectQueryResult) neighbourTh.getResult();
			if (hitQuery.resultArr.size() > 0){
				peerOutput.addAll(hitQuery.resultArr);
			}
		}
		return peerOutput;
	}
	
	// Generate messages with ID
	public String generateMessageId(){
		++messageCtr;
		return System.currentTimeMillis()+ "-" + getInfo.ID + "-"+ messageCtr;
	}
		
	//Printing all neighbouring peers
	public void neighboursList(ArrayList<PeerConf> neighborPeers, int peerId) {
		InputStream input = null;

		try {
			// Read configuration file
			Properties prop = new Properties();
			input = new FileInputStream("confProp.properties");
			prop.load(input);
			for (String myString: prop.getProperty(peerId + ".neighboursTopo").split(",")) {
				PeerConf tempPeer = new PeerConf();
				tempPeer.ID = Integer.parseInt(myString);
				tempPeer.portNum = Integer.parseInt(prop.getProperty(myString + ".port"));
				tempPeer.ipAddr = prop.getProperty(myString + ".ip");
				neighborPeers.add(tempPeer);
			}
			System.out.println("");
			input.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	// To index all the files shared b/w Peers 
	public void getSharedFiles(String dataDir) {
		for (String file:  (new File(dataDir)).list())
			localFiles.add(new FileConf(file, getInfo, TTR));
	}

	//Using RMI to make the Peer run as a Server
	public void runAsServer(int peerId, int portNum, String dataDir) {
		try {
			LocateRegistry.createRegistry(portNum);
			getSharedFiles(dataDir+"/sharedDir");
			stub = new ImplementSampleInterface(dataDir + "/sharedDir",dataDir + "/downloadedFiles", peerId, portNum, this);
			Naming.rebind("rmi://localhost:" + portNum + "/peerServer", stub);
			System.out.println("Peer " + peerId + " has started at 127.0.0.1:" + portNum);	
			(new Thread(new ImplementWatcherService(this, dataDir + "/sharedDir"))).start();		
		} catch (Exception e) { System.out.println(e);}
	}
	
	//Next three Methods: Automatically update the files with expired TTR
	private void autoUpFunc() {
		new java.util.Timer().schedule( 
	        new java.util.TimerTask() {
	            @Override
	            public void run() {
	                updateFile();
	            }
	        }, 
	        TTR 
		);
	}
	
	public void updateFile() {
		for (FileConf f : downloadFiles) {		
    		SampleInterface peerServer;
			try {
				peerServer = (SampleInterface) Naming.lookup("rmi://localhost:" + f.myServer.portNum + "/peerServer");
				FileConf info = peerServer.fetchFileInfo(f.fName);
				
				if (info.fileLastModified > f.fileLastModified) {
					System.out.println("Updating the file from other peers....");
					downloadFileFromPort(f.myServer.portNum, f.fName);
				}
				System.out.println("File is already up-to-date!");
			
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
        }
	}
	
	//File Modification function
	public void modifyFunc(String filename) {
		try {
		    Files.write(Paths.get(dataDir + "/sharedDir/" + filename), " ".getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    System.out.println(e);
		}
	}
	
	// Next 3 Functions: to download the file
	public void downloadFunc(ArrayList<PeerConf> searchResult_Peers, String fName) {
		try {
			Scanner scan = new Scanner(System.in);
			System.out.println("\n Download file ?? (Y/N): ");
			switch (scan.next()) {
				case "y":
				case "Y":
					System.out.println("Please enter Peer you want to download the file from: ");
					// trigger download function to download the file
					startDownloading(searchResult_Peers, searchResult_Peers.get(Integer.parseInt(scan.next())).ID, fName);
					System.out.println("File has been downloaded!!");
					System.out.println("");
					break;
				case "n":
				case "N":
				System.out.println("Okay, Not downloading, returing to main Menu!");
				System.out.println("");
					break;
				default:
					System.out.println("Wrong Input");
					System.out.println("");
					break;
			}
		} catch (Exception e) { System.out.println(e.getMessage());}
	}

	public FileConf startDownloading(ArrayList<PeerConf> peerOutput, int peerId, String fName) {
		FileConf info = null;
		for (PeerConf pOut : peerOutput) {		
			if (peerId == pOut.ID) {	
				SampleInterface peerServer;
				try {
					peerServer = (SampleInterface) Naming.lookup("rmi://localhost:" + pOut.portNum + "/peerServer");
					info = peerServer.fetchFileInfo(fName);
					
					downloadFileFromPort(pOut.portNum, fName);
					info.newServer = pOut;
					downloadFiles.add(info);
					break;
					
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
				
			}
		}
		return info;
	}
	
	private void downloadFileFromPort(int port, String fName) {
		try {
			SampleInterface peerServer = (SampleInterface) Naming.lookup("rmi://localhost:" + port + "/peerServer");
			byte[] out = peerServer.fetchFile(fName); // RMI fuction - fetchFile
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dataDir  + "/downloadedFiles/" + fName));
			output.write(out, 0, out.length); // creating new file in downloadedFiles dir for the Peer
			
			output.flush();
			output.close();	
		} catch (NotBoundException | IOException e) { e.printStackTrace();}	
	}
	


}
