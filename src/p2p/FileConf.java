package p2p;

import java.io.Serializable;

public class FileConf implements Serializable{
	
	private static final long versionID = 1L;
	public String checkFState, fName;
	public int versionNum, TTR;
	public PeerConf myServer;
	public PeerConf newServer;
	public long fileLastModified;
	public FileConf(String s, PeerConf nPeer, int ttr) {
		fName = s;
		versionNum = 0;
		myServer = nPeer;
		checkFState = "Valid";
		TTR = ttr;
		fileLastModified = System.currentTimeMillis();
	}
	
	public FileConf(FileConf file) {
		this.fName = file.fName;
		this.versionNum = file.versionNum;
		this.TTR = file.TTR;
		this.myServer = file.myServer;
		this.checkFState = file.checkFState;
		this.fileLastModified = file.fileLastModified;
	}

	public void incrementVersionNum() {
		versionNum += 1;
	}
	//Auto generated constructor stub
	public FileConf() {

	}

	
}
