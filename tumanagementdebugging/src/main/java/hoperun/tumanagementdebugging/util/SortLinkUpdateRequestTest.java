package hoperun.tumanagementdebugging.util;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;

import hoperun.adapter.comm.bean.TBoxMessage;
import hoperun.adapter.comm.convert.Serialize2TBoxMessage;
import hoperun.adapter.comm.util.Any2ByteUtil;
import hoperun.adapter.comm.util.TByteUtil;
import hoperun.adapter.comm.util.TCPMsgSplitUtil;
import hoperun.proxy.client.tcp.bean.ClientChannle;
import hoperun.proxy.client.tcp.bin.TcpClientFactory;
import hoperun.proxy.client.tcp.client.service.BasicMessageRecviceServiceHandle;

public class SortLinkUpdateRequestTest extends BasicMessageRecviceServiceHandle
		implements Runnable{

	public SortLinkUpdateRequestTest(){}

	private byte[] b;
	private int sleep;
	
	private static String IP_adapter;
	private static String PORT_adapter;

	private static String statue= "OK";
	
	private static List<String> logList;
	
	private static TcpClientFactory tcf = new TcpClientFactory();
	
	private static String logCacheName;
	
	private static LoadingCache<String, List<String>> logCache = LogCacheFactory.getLogCache();
	/*
	 * 登陆需要修改的东西
	 */
//	private static String bid = "9880833";
	private static String bid;
	
	public static List<String> getLogList(){
		try {
			return logCache.get(logCacheName);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public static String getStatue() {
		return statue;
	}

	public static void setStatue(String statue) {
		SortLinkUpdateRequestTest.statue = statue;
	}

	public SortLinkUpdateRequestTest(int sleep, byte[] b){
		this.b = b;
		this.sleep = sleep;
	}
	
	public SortLinkUpdateRequestTest(String bId,String port,String ip){
		bid = bId;
		PORT_adapter = port;
		IP_adapter=ip;
		logCacheName=bId+"_"+port+"_"+ip;
	}
	
// private static String IP_adapter = "124.202.152.26";
// private static String PORT_adapter = "8260";

//	private static String tbox_AES = "FFFFFFFFFFFFFFFC5";
//	private static String sn = "ABCDEFGHIJKLMNOPQRSTUVWXYZ132";
//	private static String vin = "LJ8B5D3D2FD125618";
//	private static String vin = "FFFFFFFFFFFFFFF11";
//	private static String tbox_r16 = LangUtil.getRandomString(16); // tbox随机数
	
	public static TcpClientFactory getTcf() {
		return tcf;
	}


	public void begin(){
		
		logCache.put(logCacheName, getLogList());
		
		SortLinkUpdateRequestTest.setStatue("OK");
		SortLinkUpdateRequestTest mr = new SortLinkUpdateRequestTest();
		ClientChannle channle = new ClientChannle();
		channle.setChannleName(logCacheName);
		
		channle.setIp(IP_adapter);
		channle.setPort(Integer.valueOf(PORT_adapter));

		channle.setClientLinkNum(1);
		channle.setClientServiceHandlePath("hoperun.tumanagementdebugging.util.SortLinkUpdateRequestTest");
		tcf.startTcpClient(channle);
		//tcf.closeTcpClient("test");
		//tcf.closeTcpClient(tcf.getTcpChannle(""));

		// 发送初始化消息给服务器
		byte[] msg = mr.normal_Hartbeat();

		// 假如需要通过Adapter中转的话,需要"hex"一次数据
		msg = TByteUtil.getHex(msg).replace(" ", "")
				.replace(new String(new byte[]{0x0a}), "").getBytes();
		getLogList().add(TByteUtil.getHex(msg).replace(" ", "")
				.replace(new String(new byte[]{0x0a}), ""));
		tcf.sendMessage(logCacheName, msg);
		
	}

	public String getLogCacheName() {
		return logCacheName;
	}

	public static LoadingCache<String, List<String>> getLogCache() {
		return logCache;
	}

	public static void setLogCache(LoadingCache<String, List<String>> logCache) {
		SortLinkUpdateRequestTest.logCache = logCache;
	}

	/**
	 * 正常心跳消息
	 * 
	 * @Title: versionUpgrade_Hartbeat
	 * @Description:
	 * @param: @return
	 * @return: byte[]
	 * @throws:
	 * @author: pj
	 * @Date: 2015年5月13日 下午7:09:04
	 */
	private byte[]  normal_Hartbeat(){
		TBoxMessage tm = null;
		// String s = new String(new byte[]{0, 6});

		tm = getTMmodel("" + bid, // bid
				"0", // mf
				"" + 0x0b, // aid
				System.currentTimeMillis() / 1000, // evencreationTime
				"1", // mid
				"0", // result
				new byte[]{(byte) 0xFF, 0x00}, // serviceData
				new byte[]{0, 0});// messageCounter
		try{
			System.out.println("[正常心跳消息],等待6秒重新尝试获取下行消息");
			getLogList().add("[正常心跳消息],等待6秒重新尝试获取下行消息");
			Thread.sleep(6 * 1000);
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		return Serialize2TBoxMessage.convert(tm);

	}

	@Override
	public byte[] callback(byte[] msg){
		TByteUtil.printHex(msg);
		getLogList().add(TByteUtil.getHex(msg,msg.length));
		getLogList().add("得到消息,尝试转换为TboxMessageBean\n");
		getLogList().add("将适配器消息解析正常消息\n");
		
		System.out.println("得到消息,尝试转换为TboxMessageBean");
		System.out.println("将适配器消息解析正常消息");
		
		msg = TByteUtil.parseHstring(new String(msg));
		TBoxMessage tm = Serialize2TBoxMessage.convert(msg);
		if(tm==null){
			getLogList().add("解析失败："+TByteUtil.getHex(msg));
			System.exit(1);
		}
		
		byte rmsg[] = null;
		if(isCommond(tm, 0, 0x2b, 1)) { // 通讯维持 下行消息
			rmsg = send_Hartbeat(tm);
		}
		if(isCommond(tm, 0, 0xb, 2)) { // 心跳消息ACK
			rmsg = normal_Hartbeat();
		}

		if(isCommond(tm, 1, 0xf5, 1)) { // 仪表req
			rmsg = send_VHSUpdate_2(tm);
			System.out.println("得到仪表req");
		}
		if(isCommond(tm, 1, 0xf5, 2)) { // 控制req
			getLogList().add("得到仪表ACK");
			System.out.println("得到仪表ACK");
			//rmsg = normal_Hartbeat();
		}

		if(isCommond(tm, 1, 0xf1, 1)) { // 控制req
			rmsg = send_RemoteOperation_2(tm);
			System.out.println("得到控制req");
		}

		if(isCommond(tm, 1, 0xf1, 2)) { // 控制req
			System.out.println("得到控制ACK");
			//rmsg = normal_Hartbeat();
		}

		if(rmsg != null) {
			// 假如需要通过Adapter中转的话,需要"hex"一次数据
			rmsg = TByteUtil.getHex(rmsg).replace(" ", "")
					.replace(new String(new byte[]{0x0a}), "").getBytes();
		}
		getLogList().add(TByteUtil.getHex(rmsg));
		tm.getFeature().getDispatch().setDispatchCreationTime(System.currentTimeMillis()/1000);
		return rmsg;
	}

	private byte[] send_VHSUpdate_2(TBoxMessage tm){
		System.out.println("设置延迟发送仪表ACK 1s");
		getLogList().add("设置延迟发送仪表ACK 1s");
		new Thread(new SortLinkUpdateRequestTest(2, send_VHSUpdate_3(tm)),"vhs-2")
				.start();

		System.out.println("设置延迟发送心跳 2s");
		getLogList().add("设置延迟发送心跳 2s");
		new Thread(new SortLinkUpdateRequestTest(3, normal_Hartbeat()),"vhs-3").start();

		tm.getFeature().getDispatch().setMid(2);
		
		return Serialize2TBoxMessage.convert(tm);

	}

	private byte[] send_VHSUpdate_3(TBoxMessage tm){
		tm.getFeature().getDispatch().setMid(3);

		// byte[] serviceData = new byte[0];
		String serviceData_s = "00 F1" + // operation
				"55 00 63 aa"// 数据采集时间
				+ "01 02 ae a5" + "40 07 27 0e" + "00 00 00 00" + "2a" // 01定位成功，45000000,120000000,正北，42
				+ "0b b8 00 2a" // 发动机转速 3000,车速 42
				+ "14"// 未怠速，空调 关，驻车制动0，发动机 on，档位 0x04
				+ "00"// srs, tbox, ems, eps, sas, tcu, hud, bcm 没有故障
				+ "3f" // epb, icu 没有故障， 乘客和驾驶员安全带状态 bulked，防盗报警 invalid
				+ "00"// 安全气囊， no crash
				+ "00 00 00 00" // 胎压
				+ "00"// 四车窗 closed
				+ "01"// 天窗 closed ，四车门，后门 closed，司机侧门锁 lock
				+ "e8"// 乘客侧门锁 lock，后两门锁lock，发动机舱盖 closed，后备箱锁 lock
				+ "80"// 钥匙位置 on，左右转状态off，远近光灯off，前后雾灯off
				+ "00 00 04 00"// 车辆总里程1024
				+ "24 0e 00 20"// 燃油余量36，电池电压 14，上报周期油耗32
				+ "18 18 58"// 车辆环境温度 24，车内温度24，发动机冷却液温度88
				+ "ff"// 燃油报警 normal，机油压力指示灯，充电状态指示灯，停车指示灯状态，保养灯状态 off
				+ "ff"// ecall触发模式，无效
				+ "00"// tu未连接手机，tu配置版本为0，tu不使用内置GPS
				+ "03"// tu激活状态，激活，但服务不可用
				+ "00"// 车辆状态类型
				+ "00"// 头灯未开启，不在预处理中，不在充电中，充电枪未插入
				+ "01 00"// 灯泡故障格式 1，灯泡故障1 left-turn-any
				;

		byte[] serviceData = TByteUtil.parseHstring(serviceData_s);
		tm.getFeature().getDispatch().setDispatchCreationTime(System.currentTimeMillis()/1000);
		tm.setServiceData(serviceData);
		return Serialize2TBoxMessage.convert(tm);
	}

	private byte[] send_RemoteOperation_2(TBoxMessage tm){
//		System.out.println("设置延迟发送控制ACK 1s");
		getLogList().add("设置延迟发送控制ACK 1s");
		new Thread(new SortLinkUpdateRequestTest(1, send_RemoteOperation_3(tm)),"control-3")
				.start();

//		System.out.println("设置延迟发送控制END 2s");
		getLogList().add("设置延迟发送控制END 2s");
		new Thread(new SortLinkUpdateRequestTest(2, send_RemoteOperation_4(tm)),"control-4")
				.start();

//		System.out.println("设置延迟发送心跳 3s");
		logList.add("设置延迟发送心跳 3s");
		new Thread(new SortLinkUpdateRequestTest(3, normal_Hartbeat()),"control-hartbate").start();

		tm.getFeature().getDispatch().setMid(2);
		return Serialize2TBoxMessage.convert(tm);

	}

	private byte[] send_RemoteOperation_3(TBoxMessage tm){
		tm.getFeature().getDispatch().setMid(3);

		// byte[] serviceData = new byte[0];
		String serviceData_s = "00 00" // Operation 操作符的位置
				+ "55 00 63 aa"// 数据采集时间
				+ "01 02 ae a5" + "40 07 27 0e" + "00 00 00 00" + "2a" // 01定位成功，45000000,120000000,正北，42
				+ "0b b8 00 2a" // 发动机转速 3000,车速 42
				+ "14"// 未怠速，空调 关，驻车制动0，发动机 on，档位 0x04
				+ "00"// srs, tbox, ems, eps, sas, tcu, hud, bcm 没有故障
				+ "3f" // epb, icu 没有故障， 乘客和驾驶员安全带状态 bulked，防盗报警 invalid
				+ "00"// 安全气囊， no crash
				+ "00 00 00 00" // 胎压
				+ "00"// 四车窗 closed
				+ "01"// 天窗 closed ，四车门，后门 closed，司机侧门锁 lock
				+ "e8"// 乘客侧门锁 lock，后两门锁lock，发动机舱盖 closed，后备箱锁 lock
				+ "80"// 钥匙位置 on，左右转状态off，远近光灯off，前后雾灯off
				+ "00 00 04 00"// 车辆总里程1024
				+ "24 0e 00 20"// 燃油余量36，电池电压 14，上报周期油耗32
				+ "18 18 58"// 车辆环境温度 24，车内温度24，发动机冷却液温度88
				+ "FF"// 燃油报警 normal，机油压力指示灯，充电状态指示灯，停车指示灯状态，保养灯状态 off
				+ "ff"// ecall触发模式，无效
				+ "00"// tu未连接手机，tu配置版本为0，tu不使用内置GPS
				+ "03"// tu激活状态，激活，但服务不可用
				+ "00"// 车辆状态类型
				+ "00"// 头灯未开启，不在预处理中，不在充电中，充电枪未插入
				+ "01 00"// 灯泡故障格式 1，灯泡故障1 left-turn-any
				;
		byte[] serviceData = TByteUtil.parseHstring(serviceData_s);
		serviceData[0] = tm.getServiceData()[0];
		serviceData[1] = tm.getServiceData()[1];
		tm.getFeature().getDispatch().setDispatchCreationTime(System.currentTimeMillis()/1000);
		tm.setServiceData(serviceData);
		return Serialize2TBoxMessage.convert(tm);
	}

	private byte[] send_RemoteOperation_4(TBoxMessage tm){
		tm.getFeature().getDispatch().setMid(4);

		// byte[] serviceData = new byte[0];
		String serviceData_s = "00 F1" + // operation
				"01" + // 操作状态
				"55 00 63 aa"// 数据采集时间
				+ "01 02 ae a5" + "40 07 27 0e" + "00 00 00 00" + "2a" // 01定位成功，45000000,120000000,正北，42
				+ "0b b8 00 2a" // 发动机转速 3000,车速 42
				+ "14"// 未怠速，空调 关，驻车制动0，发动机 on，档位 0x04
				+ "00"// srs, tbox, ems, eps, sas, tcu, hud, bcm 没有故障
				+ "3f" // epb, icu 没有故障， 乘客和驾驶员安全带状态 bulked，防盗报警 invalid
				+ "00"// 安全气囊， no crash
				+ "00 00 00 00" // 胎压
				+ "00"// 四车窗 closed
				+ "01"// 天窗 closed ，四车门，后门 closed，司机侧门锁 lock
				+ "e8"// 乘客侧门锁 lock，后两门锁lock，发动机舱盖 closed，后备箱锁 lock
				+ "80"// 钥匙位置 on，左右转状态off，远近光灯off，前后雾灯off
				+ "00 00 04 00"// 车辆总里程1024
				+ "24 0e 00 20"// 燃油余量36，电池电压 14，上报周期油耗32
				+ "18 18 58"// 车辆环境温度 24，车内温度24，发动机冷却液温度88
				+ "FF"// 燃油报警 normal，机油压力指示灯，充电状态指示灯，停车指示灯状态，保养灯状态 off
				+ "ff"// ecall触发模式，无效
				+ "00"// tu未连接手机，tu配置版本为0，tu不使用内置GPS
				+ "03"// tu激活状态，激活，但服务不可用
				+ "00"// 车辆状态类型
				+ "00"// 头灯未开启，不在预处理中，不在充电中，充电枪未插入
				+ "01 00"// 灯泡故障格式 1，灯泡故障1 left-turn-any
				;
		byte[] serviceData = TByteUtil.parseHstring(serviceData_s);
		serviceData[0] = tm.getServiceData()[0];
		serviceData[1] = tm.getServiceData()[1];
		tm.getFeature().getDispatch().setDispatchCreationTime(System.currentTimeMillis()/1000);

		tm.setServiceData(serviceData);
		return Serialize2TBoxMessage.convert(tm);
	}

	private byte[] send_Hartbeat(TBoxMessage tm){
		byte b[] = tm.getServiceData();

		int time = Any2ByteUtil.byte2int(b[0]);
		getLogList().add("[通讯维持消息] 当前服务器要求客户端休眠" + time + "秒,开始沉睡");
//		SelfLogger.tempParam("[通讯维持消息] 当前服务器要求客户端休眠" + time + "秒,开始沉睡");
		try{
			Thread.sleep(time * 1000);
		} catch(InterruptedException e){
			e.printStackTrace();
		}

		return normal_Hartbeat();
	}

	/**
	 * 判断是否是标记的命令信息
	 * 
	 * @Title: iscommond
	 * @Description:
	 * @param: @param
	 *             tm
	 * @param: @param
	 *             mf
	 * @param: @param
	 *             aid
	 * @param: @param
	 *             mid
	 * @param: @return
	 * @return: boolean
	 * @throws:
	 * @author: jnad
	 * @Date: 2015年5月12日 下午3:53:56
	 */
	private boolean isCommond(TBoxMessage tm, int mf, int aid, int mid){
		if(tm.getFeature().getHeader().getMessageFlag() == mf
				&& tm.getFeature().getDispatch().getAid() == aid
				&& tm.getFeature().getDispatch().getMid() == mid) {
			return true;
		}
		return false;

	}

	public static TBoxMessage getTMmodel(String bid, String messageFlag,
			String aid, long evencreationTime, String mid, String result,
			byte[] serviceData, byte[] messageCount){
		String messageModel = "74426F780000000004D200550063AA02030003003E000000000000313233343536373839303132333435363738393031323334353637383941313233343536373839304243454131370000000000000000000000000000000025";

		/**
		 * 组装消息
		 */
		TBoxMessage tm = Serialize2TBoxMessage
				.convert(TByteUtil.parseHstring(messageModel));
		tm.getFeature().getHeader().setBid(Long.valueOf(bid));
		tm.getFeature().getHeader()
				.setMessageFlag(Integer.valueOf(messageFlag));// 管理
		tm.getFeature().getDispatch().setAid(Integer.valueOf(aid));// 登陆
		tm.getFeature().getDispatch()
				.setEventCreationTime(Long.valueOf(evencreationTime));
		tm.getFeature().getDispatch().setMid(Integer.valueOf(mid));// loginReq
		tm.getFeature().getDispatch().setResult(Integer.valueOf(result));
		tm.getFeature().getDispatch().setSecurityVersion(0);// 不加密
		tm.setServiceData(serviceData);
		tm.getFeature().getDispatch().setServiceDataLength(serviceData.length);

		tm.getFeature().getDispatch().setMessageCounter(messageCount);

		// 设置校验位
		tm.setCheckSum(TCPMsgSplitUtil.getXOR4bytes(
				TCPMsgSplitUtil.splitMid(Serialize2TBoxMessage.convert(tm), 1,
						Serialize2TBoxMessage.convert(tm).length - 1)));

		return tm;
	}

	@Override
	public void run(){
		byte[] msg = null;
		try{
			Thread.sleep(sleep * 1000);
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		getLogList().add("-开始异步发送数据-");
//		System.out.println("-开始异步发送数据-");
		// 假如需要通过Adapter中转的话,需要"hex"一次数据
		msg = TByteUtil.getHex(b).replace(" ", "")
				.replace(new String(new byte[]{0x0a}), "").getBytes();
		
		tcf.sendMessage(logCacheName, msg);

	}

}
