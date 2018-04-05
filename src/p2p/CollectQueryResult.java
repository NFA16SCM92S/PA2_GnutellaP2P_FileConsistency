package p2p;
import java.io.*;
import java.util.*;

//COllecting Hit Query Result class
public class CollectQueryResult implements Serializable {
	private static final long versionID = 1L;
	public ArrayList<PeerConf> resultArr = new ArrayList<PeerConf>();	
	public ArrayList<String> pathArr = new ArrayList<String>();
}
