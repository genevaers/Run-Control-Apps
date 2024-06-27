package org.genevaers.utilities;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPSession {

	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.test.pm.FTPSession");
	
	private FTPClient ftp = new FTPClient();

	private String host = "";
	private String userid = "";
	private String password = "";

	public FTPSession(String host) {
		this.host = host;
		this.password = System.getenv("TSO_PASSWORD");
	}
	
	public void connect() throws IOException {
		
		String replyText;
		// Connect to the server

		ftp.connect(host);
		replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, replyText);
		int rc = ftp.getReplyCode();
		if (rc != 220) {
			throw new IOException("Failed to connect to host " + host + ", rc " + rc);			
		}
		
		// Login to the server

		ftp.login(userid, password);
		replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, replyText);
		rc = ftp.getReplyCode();
		if (rc != 230) {
			throw new IOException("Failed to log in using given credentials for " + userid + ", rc " + rc);
		}
	}
	
	public String getHost() { return host; }
	public String getUser() { return userid; }
	public String getPassword() { return password; }
	
	public boolean isConnected() {
		return ftp.isConnected();
	}
	
	public void disconnect() throws IOException {
		ftp.quit();		
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, replyText);
	}
	
	public void setASCII() throws IOException {
		if(ftp.isConnected()) {
		ftp.setFileType(FTP.ASCII_FILE_TYPE);		
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Set ASCII mode, " + replyText);
		}
	}

	public void setBinary() throws IOException {
		if(ftp.isConnected()) {
		ftp.setFileType(FTP.BINARY_FILE_TYPE);		
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Set Binary mode, ", replyText);
		}
	}
	
	public void setDCB(String space, String primary, String sec, String recfm, String lrecl) throws IOException {
		
		String dcbcommand = "recfm=" + recfm + " lrecl=" + lrecl + " blksize=0 "+ space + " pri=" + primary + " sec=" + sec + " retpd trail";
		ftp.site(dcbcommand);
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Setting dcb parms " +dcbcommand+ ", " + replyText);
		int rc = ftp.getReplyCode();
		if (rc != 200) {
			throw new IOException("Failed to set dcb parms , rc " + rc);
		}		
	}

	public void setRDW() throws IOException {
		ftp.site("RDW");
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Set RDW " + replyText);
		int rc = ftp.getReplyCode();
		if (rc != 200) {
			throw new IOException("Failed to set dcb parms , rc " + rc);
		}		
	}

	public void setLRECL(String lrecl) throws IOException {
		
		String lreclcommand = "lrecl=" + lrecl;
		ftp.site(lreclcommand);
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Setting lerecl " +lreclcommand+ ", " + replyText);
		int rc = ftp.getReplyCode();
		if (rc != 200) {
			throw new IOException("Failed to set lrecl , rc " + rc);
		}		
	}

	public void getFile(String srcFile, File destFile) throws IOException {
		try (FileOutputStream out = new FileOutputStream(destFile);)  {
			ftp.retrieveFile(srcFile, out);
			out.close();
			int rc = ftp.getReplyCode();								
			String replyText = ftp.getReplyString().trim();
			logger.log(Level.FINE, "Transfer file " + destFile.getName() + ", " + replyText);
			if (rc != 226 && rc != 250) {
				throw new IOException("Couldn't transfer file " + srcFile + ", rc " + rc + " " + replyText);					
			}						
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "getFile failed %s", e.getMessage());
		}
	}
	
	public void setUserAndPassword(String user, String pwd) throws IOException {
		if (user.length() > 0) {
			userid = user;
			if (pwd.isEmpty() && password.isEmpty()) {
				throw new IOException("Password not set for user " + user);
			}
		}
	}

	public boolean passwordIsEmpty() {
		return password.isEmpty();
	}
	public void getDirectory(String srcDir, File destDir) throws IOException {
		destDir.mkdirs();
		ftp.changeWorkingDirectory(srcDir);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Change to directory " + srcDir + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't change to source directory " + srcDir + ", rc " + rc);
		}
		FTPFile[] result = ftp.listFiles(".");
		for (int i = 0; i < result.length; i++) {
			FTPFile file = result[i];
			if (file.isFile()) {
				File destFile = new File(destDir.getAbsolutePath() + File.separator + file.getName());
				FileOutputStream out = new FileOutputStream(destFile);
				ftp.retrieveFile(file.getName(), out);
				rc = ftp.getReplyCode();								
				replyText = ftp.getReplyString().trim();
				logger.log(Level.FINE, "Transfer file " + file.getName() + ", " + replyText);
				if (rc != 226 && rc != 250) {
					throw new IOException("Couldn't transfer file " + file.getName() + ", rc " + rc);					
				}				
			}
			else if (file.isDirectory()) {
				String csrcDir = srcDir + "/" + file.getName();
				File cdestDir = new File(destDir.getAbsolutePath() + File.separator + file.getName());
				getDirectory(csrcDir, cdestDir);
			}
		}
		
	}

	public void putDirectory(File srcDir, String destDir) throws IOException {
		ftp.changeWorkingDirectory(destDir);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Change to directory " + destDir + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't change to destination directory " + destDir + ", rc " + rc);
		}
		File[] results = srcDir.listFiles();
		for (int i = 0; i < results.length; i++) {
			File file = results[i];
			if (file.isFile()) {
				FileInputStream in = new FileInputStream(file);
				ftp.storeFile(file.getName(), in);
				rc = ftp.getReplyCode();				
				replyText = ftp.getReplyString().trim();
				logger.log(Level.FINE, "Transfer file " + file.getName() + ", " + replyText);
				if (rc != 226 && rc != 250) {
					throw new IOException("Couldn't transfer file " + file.getName() + ", rc " + rc);					
				}
			}
		}
	}
	
	public void mkPDS(String destPDS) throws IOException {

		int pos = destPDS.lastIndexOf('.');
		if (pos == -1 || !destPDS.matches("^//[a-zA-Z0-9\\.]+$")) {
			throw new IOException("Invalid PDS format" + destPDS);
		}
		String prefix = destPDS.substring(0, pos);
		String dir = destPDS.substring(pos+1, destPDS.length()); 
		ftp.changeWorkingDirectory(prefix);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Change to directory " + prefix + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't change to destination directory " + prefix + ", rc " + rc);
		}					
		ftp.mkd(dir);
		rc = ftp.getReplyCode();
		replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Make directory " + destPDS + ", " + replyText);
		if (rc != 257 ) {
			if (rc == 521) {
				//throw new IOException("PDS already exists, rc " + rc);				
			} else {
				throw new IOException("Couldn't make destination directory " + destPDS + ", rc " + rc);
			}
		}		
		
	}
	
	public void putPDS(File srcDir, String destDir) throws IOException {
		
		mkPDS(destDir);
		ftp.changeWorkingDirectory(destDir);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Change to directory " + destDir + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't change to destination directory " + destDir + ", rc " + rc);
		}
		File[] results = srcDir.listFiles();
		for (int i = 0; i < results.length; i++) {
			File file = results[i];
			if (file.isFile()) {
				// make sure filename has extension removed 
				String name = file.getName();
				if (name.contains(".")) {
					name = name.substring(0,name.indexOf('.'));
				}
				FileInputStream in = new FileInputStream(file);
				ftp.storeFile(name, in);
				rc = ftp.getReplyCode();				
				replyText = ftp.getReplyString().trim();
				logger.log(Level.FINE, "Transfer file " + name + ", " + replyText);
				if (rc != 226 && rc != 250) {
					throw new IOException("Couldn't transfer file " + name + ", rc " + rc);					
				}
			}
		}
	}
	
	public void deleteDatasetsUnder(String qual) throws IOException {
		
		int pos = qual.lastIndexOf('.');
		if (pos == -1 || !qual.matches("^//[a-zA-Z0-9\\.\\*]+$")) {
			throw new IOException("Invalid qualifier format" + qual);
		}
		
		ftp.changeWorkingDirectory(qual);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Change to directory " + qual + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't change to destination directory " + qual + ", rc " + rc);
		}
		
		ftp.site("listsubdir");
		replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Setting list sub dir to true, " + replyText);
		rc = ftp.getReplyCode();
		if (rc != 200) {
			throw new IOException("Failed to set list sub dir , rc " + rc);
		}
		
		FTPFile files[] = ftp.listFiles();
		for (FTPFile file : files) {
			String name = file.getName(); 
			deleteFile(name);
		}
		
		ftp.site("nolistsubdir");
		replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Setting list sub dir to false, " + replyText);
		rc = ftp.getReplyCode();
		if (rc != 200) {
			throw new IOException("Failed to set list sub dir , rc " + rc);
		}
		
	}
	
	public void deleteFile(String file) throws IOException {
		ftp.deleteFile(file);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Delete file " + file + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't delete file " + file + ", rc " + rc);
		}
	}
	
	public void putFile(File srcFile, String destDir) throws IOException {
		ftp.changeWorkingDirectory(destDir);
		int rc = ftp.getReplyCode();
		String replyText = ftp.getReplyString().trim();
		logger.log(Level.FINE, "Change to directory " + destDir + ", " + replyText);
		if (rc != 250 ) {
			throw new IOException("Couldn't change to destination directory " + destDir + ", rc " + rc);
		}

		try (FileInputStream in = new FileInputStream(srcFile);) {
			ftp.storeFile(srcFile.getName(), in);
			rc = ftp.getReplyCode();				
			replyText = ftp.getReplyString().trim();
			logger.log(Level.FINE, "Transfer file " + srcFile.getName() + ", " + replyText);
			if (rc != 226 && rc != 250) {
				throw new IOException("Couldn't transfer file " + srcFile.getName() + ", rc " + rc);					
			}
		} catch( FileNotFoundException e) {
			logger.log(Level.SEVERE, "putFile failed %s", e.getMessage());
		}
		
	}
	
	public void putDataset(File srcFile, String dataset) throws IOException {
		try (FileInputStream in = new FileInputStream(srcFile);) {
			ftp.storeFile(dataset, in);
			int rc = ftp.getReplyCode();
			String replyText = ftp.getReplyString().trim();
			logger.log(Level.FINE, "Transfer file " + srcFile.getName() + ", " + replyText);
			if (rc != 226 && rc != 250) {
				throw new IOException("Couldn't transfer file " + srcFile.getName() + ", rc " + rc);
			}
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "putDataset failed %s", e.getMessage());
		}

	}
	
}
