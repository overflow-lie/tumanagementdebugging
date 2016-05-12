package hoperun.tumanagementdebugging.bin;

import hoperun.proxyserver.bin.ProxyServerBootstrap;

/**
 * Tu信息调试统计管理系统启动类
 * @classname:		DebuggingBootStrap.java
 * @description:	
 *
 * @author:			pj
 * @version:		V1.0
 * @createdate:		2016年4月7日 下午2:50:48
 */
public class DebuggingBootStrap {

	public static void main(String[] args) {

		System.out.println("[DebuggingBootStrap] 启动Httpserver");
		new Thread(new Runnable() {
			public void run() {
				ProxyServerBootstrap.begin();
			}
		}).start();

	}

}
