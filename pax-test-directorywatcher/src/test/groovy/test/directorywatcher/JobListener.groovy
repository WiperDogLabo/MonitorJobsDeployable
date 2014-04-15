package test.directorywatcher;

import java.io.File;
import java.io.IOException;

import jcifs.smb.SmbFile;

import org.wiperdog.directorywatcher.Listener;

public class JobListener implements Listener{
	String directory;	
	boolean isLocalFile = true;
	private String username;
	private String password;
	private String host;

	public JobListener(String directory){
		this.directory = directory;
	}
	public JobListener(String directory,String host,String username,String password){
		this.host = host;
		this.directory = directory;
		this.username = username;
		this.password = password;
		
	}
	public String getDirectory() {
		return directory;
	}

	public long getInterval() {
		return 0;
	}

	public boolean filterFile(File file) {
		if(file.getName().endsWith(".job")){
			return true;
		}
		return false;
	}

	public boolean notifyModified(File target) throws IOException {
		System.out.println("File " + target.getPath() + "added");
		return true;
	}

	public boolean notifyAdded(File target) throws IOException {
		System.out.println("File " + target.getPath() + "added");
		return false;
	}

	public boolean notifyDeleted(File target) throws IOException {
		System.out.println("File " + target.getPath() + "added");
		return false;
	}
	public boolean filterFile(SmbFile file) {
		if(file.getName().endsWith(".job")){
			return true;
		}
		return false;
		
	}
	public boolean notifyModified(SmbFile target) throws IOException {
		System.out.println("File " + target.getPath() + "modified");
		return true;
	}
	public boolean notifyAdded(SmbFile target) throws IOException {
		System.out.println("File " + target.getPath() + "added");
		return false;
	}
	public boolean notifyDeleted(SmbFile target) throws IOException {
		
		return false;
	}
	public String getUsername() {
		
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}

	public String getHost() {
		return this.host;
	}
}