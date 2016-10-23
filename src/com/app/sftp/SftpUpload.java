package com.app.sftp;

import java.util.HashMap;
import java.util.Map;
import com.jcraft.jsch.ChannelSftp;

public class SftpUpload {
		
	public void upLoad(String path)
	{
		  Map<String, String> sftpDetails = new HashMap<String, String>();
          // 设置主机ip，端口，用户名，密码
        sftpDetails.put(SftpConstants.SFTP_REQ_HOST, "10.9.167.55");
        sftpDetails.put(SftpConstants.SFTP_REQ_USERNAME, "root");
        sftpDetails.put(SftpConstants.SFTP_REQ_PASSWORD, "arthur");
        sftpDetails.put(SftpConstants.SFTP_REQ_PORT, "22");
        
        String src = path; // 本地文件名
        String dst = "/home/omc/ylong/sftp/HB-SnagIt1001.rar"; // 目标文件名
              
        SftpChannel channel = new SftpChannel();
        
        ChannelSftp chSftp;
		try {
			  chSftp = channel.getChannel(sftpDetails, 60000);
			  chSftp.put(src, dst, ChannelSftp.OVERWRITE);
	        chSftp.quit();
	        channel.closeChannel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}
