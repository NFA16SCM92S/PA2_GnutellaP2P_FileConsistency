package p2p;
import java.rmi.*;

public interface SampleInterface extends Remote {	
	public byte[] fetchFile(String filename) throws RemoteException;
	public FileConf fetchFileInfo(String filename) throws RemoteException;	
	public CollectQueryResult queryFunc(String filename, int fromPeerId, String msgId, int TTL) throws RemoteException;
	public void invalidateFunc(String msgID, int originNode, String filename, int versionNumber) throws RemoteException;
	
}
