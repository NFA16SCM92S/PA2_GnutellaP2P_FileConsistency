package p2p;
import java.util.*;

public class MainP2P {	
	// Supressing resource warnings coming with java generics.
	@SuppressWarnings("resource")
	public static void main(String args[]) {
		if (args.length!=2) {
			System.out.println("Usage: ant -Darg0=<PeerID> -Darg1=<Port#> run");
			System.exit(0);
		}	
		PeerProcedure getPeer = new PeerProcedure(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		try {
			Scanner scan = new Scanner(System.in);
			while (true) {

				//Displaying menu for PUSH and PULL approaches
				if (getPeer.isPush){
					System.out.println("******** Gnutella Style P2P System ********");
					System.out.println("======== Approach: PUSH ========");
					System.out.println("Please choose any operation from below options: ");
					System.out.println("1 -> To Search and Download a file from Peer");
					System.out.println("2 -> To Modify a file");
					System.out.println("X -> Done - EXIT now!!");	
				}
				else if (!getPeer.isPush){
					System.out.println("******** Gnutella Style P2P System ********");
					System.out.println("======== Approach: PULL ========");
					System.out.println("Please choose any operation from below options: ");
					System.out.println("1 -> To Search and Download a file from Peer");
					System.out.println("2 -> To Modify a file");
					System.out.println("3 -> To Automatically update the Peers");
					System.out.println("X -> Done - EXIT now!!");
				}
				switch (scan.next()) {
					// case 1-> shows search results for the file and displays the peers where the file exists to download
					case "1":
						System.out.println("Please enter the file name to look for:");
						String fNameLookup = scan.next();
						ArrayList<PeerConf> peerOutput = getPeer.searchFile(fNameLookup);
						System.out.println("" + fNameLookup + (peerOutput.size() == 0 ? " Sorry, file doesn't exist!": " - file exists on below peers"));
						for (PeerConf pOut: peerOutput)
							System.out.println(peerOutput.indexOf(pOut) + " -> Peer" + pOut.ID + " with IP:Port# = 127.0.0.1:" + pOut.portNum);
						// If the file found on the any of the peers, pass the peer name to download procedure
						if (peerOutput.size()>0)
							getPeer.downloadFunc(peerOutput, fNameLookup);
						break;

					//case 2-> to modify a given file					
					case "2":
						System.out.println("Please enter file name you wish to modify:");
						String modifyFuncName = scan.next();
						getPeer.modifyFunc(modifyFuncName);
						break;

					// case 3-> In case of Pull approach, auto update File on the peers	
					case "3":
						if (!getPeer.isPush)
						System.out.println("Updating the file from other peers from MainP2p....");
							getPeer.updateFile();
						break;

					// case 4-> to exit from the program
					case "x":	
					case "X":
						System.out.println("Exiting......");
						System.exit(1);
					// default case -> Wrong Input
					default:
						System.out.println("Wrong Input");
						break;
				}
			}
		}
		catch (Exception e) { System.out.println(e.getMessage()); }
	}
}
