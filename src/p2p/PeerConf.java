package p2p;
import java.io.Serializable;
// PeerConf class -> gives versionID, ID, port# and ipAddr
public class PeerConf implements Serializable {
	private static final long versionID = 1L;
	public int ID;	
	public String ipAddr;
	public int portNum;
}
