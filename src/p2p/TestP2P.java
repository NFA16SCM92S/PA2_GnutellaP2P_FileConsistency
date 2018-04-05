package p2p;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.rmi.*;

public class TestP2P {
	//Declaring static variables
	static int testClient = 1;
	static int opsCount = 100;
	static int  peersCount =10;
	static int peersCountNew = 10;
	static int opsCountNew = 2;
	static int testClientNew = 1;

	public static void main(String[] args) {
		
		System.out.println("\n\n*********Testing Modify Func********");
		ArrayList<PeerProcedure> testPeerNew = new ArrayList<PeerProcedure>();
		for(int k=testClientNew; k< peersCountNew; k++)
			testPeerNew.add(new PeerProcedure(k+1, 9000+k+1));
			testingModifyFunc(testPeerNew);
			

		System.out.println("\n\n*********Testing Search Func********");
		ArrayList<PeerProcedure> testPeer = new ArrayList<PeerProcedure>();
		for(int i=0; i< testClient; i++)
			testPeer.add(new PeerProcedure(i+1, 9000+i+1));	
			testingSearchFunc(testPeer);

	}

	// To test the modifying Func
	static void testingModifyFunc(ArrayList<PeerProcedure> testPeerNew) {
		
		long startN=System.currentTimeMillis();
		for(int i=0;i<opsCountNew;i++) {
			int index = ThreadLocalRandom.current().nextInt(testClient, peersCount);
			PeerProcedure getPeer = testPeerNew.get(index);
			FileConf info = getPeer.localFiles.get(ThreadLocalRandom.current().nextInt(0, getPeer.localFiles.size()));
			System.out.println("Modify the file: "+ info.fName);
			getPeer.modifyFunc(info.fName);
		}
		
		long endN=System.currentTimeMillis();
		System.out.println("\nTotal time taken by Modify test Operation ("+ testClientNew +"): "+ (double)(endN-startN)/opsCountNew);
		System.out.println("\n*********************End of Modify testcase***********************************");
	}
	// To test the search Func
	static void testingSearchFunc(ArrayList<PeerProcedure> testPeer) {	
		int i = 0;
		int t = 0;
		try {
			long beginOps=System.currentTimeMillis();
			for(int j=0; j < opsCount; j++) {
				int index = ThreadLocalRandom.current().nextInt(0, testClient);
				PeerProcedure getPeer = testPeer.get(index);				
				String fNameLookup = "p1." + ((j%10)+1) + ".txt";
				System.out.println("Look for file: "+ fNameLookup);
				ArrayList<PeerConf> peerOutput = getPeer.searchFile(fNameLookup);
				if (peerOutput.size() > 0) {
					index = ThreadLocalRandom.current().nextInt(0, peerOutput.size());
					FileConf downloadedinfo = getPeer.startDownloading(peerOutput, peerOutput.get(index).ID, fNameLookup);	
					SampleInterface peerServer = (SampleInterface) Naming.lookup("rmi://localhost:" + downloadedinfo.myServer.portNum + "/peerServer");
					FileConf modifiedFileInfo = peerServer.fetchFileInfo(fNameLookup);
					if (modifiedFileInfo.fileLastModified > downloadedinfo.fileLastModified)
						i++;
					t++;
				}
			}	
			long finishOps=System.currentTimeMillis();
			System.out.println("\nTime taken by the Search test Operation ("+ testClient +"): "+ (double)(finishOps-beginOps)/opsCount);
			System.out.println("\n********************End of Search testcase************************************");
			System.exit(0);
			
		} 
		catch (InterruptedException | MalformedURLException | RemoteException | NotBoundException e) { e.printStackTrace(); }
	}


}
