package hoperun.tumanagementdebugging.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class LogReaderUtil {
	private static Map<String,Object> map = new HashMap<>();
	private StringBuilder sb = new StringBuilder();
	
	public LogReaderUtil(){
		
	}
	
	public StringBuilder getSb() {
		return sb;
	}

	public void setSb(StringBuilder sb) {
		this.sb = sb;
	}

	private File logFile = null;
	private long lastTimeFileSize = 0; // 上次文件大小

	public LogReaderUtil(File logFile) {
		this.logFile = logFile;
//		this.lastTimeFileSize = logFile.length();
	}
	
	public String[] getLogs(){
		    String tmp = "";
			try {
				@SuppressWarnings("resource")
				RandomAccessFile randomFile = new RandomAccessFile(logFile, "r");
				randomFile.seek(lastTimeFileSize);
				while ((tmp = randomFile.readLine()) != null) {
					tmp = new String(tmp.toString().getBytes("ISO8859_1"),"UTF-8")+"\n";
					sb.append(tmp);
				}
				lastTimeFileSize = randomFile.length();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tmp = sb.toString();
			String[] logArr = tmp.split("\n");
			return logArr;
	}

	public static Map<String, Object> getMap() {
		return map;
	}

	public static void setMap(Map<String, Object> map) {
		LogReaderUtil.map = map;
	}
	
}
