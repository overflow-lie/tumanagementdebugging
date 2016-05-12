package hoperun.tumanagementdebugging.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import hoperun.proxy.server.tcp.common.util.PropertiesUtil;
import hoperun.util.StringUtil;

public class TuDebuggingCanstant {
	private static TuDebuggingCanstant constant = null;
	private static final Map<String, String> constantMap = new HashMap<String, String>();
	private static final String PROPERTIESNAME = "tcpserver.properties";
	public TuDebuggingCanstant() {

		/* 配置信息Properties */
		Properties pu = PropertiesUtil.loadProperties(PROPERTIESNAME);
		Set<Map.Entry<Object, Object>> list = pu.entrySet();

		for(Map.Entry<Object, Object> me: list){
			constantMap.put(me.getKey().toString(), me.getValue().toString());
		}

	
	}
	public static TuDebuggingCanstant getConstant(){
		if (constant==null) {
			constant= new TuDebuggingCanstant();
		}
		return constant;
	}
	/**
	 * 获取配置信息的某一参数,无法找到返回""
	 * 
	 * @Title: getMapProperties
	 * @Description:
	 * @param: @param key
	 * @param: @return
	 * @return: String
	 * @throws:
	 * @author: jnad
	 * @Date: 2015年5月6日 下午5:57:25
	 */
	public String getMapProperties(String key){
		if(TuDebuggingCanstant.constant == null){
			constant = new TuDebuggingCanstant();
		}
		return constantMap.get(key) == null? "" : constantMap.get(key);
	}
	/**
	 * 获取一个int的value
	 * 
	 * @Title: getMapPropertiesByInteger
	 * @Description:
	 * @param: @param key
	 * @param: @return
	 * @return: int
	 * @throws:
	 * @author: jnad
	 * @Date: 2015年5月14日 下午6:08:30
	 */
	public int getMapPropertiesByInt(String key){
		if(TuDebuggingCanstant.constant == null){
			constant = new TuDebuggingCanstant();
		}
		String v = constantMap.get(key);
		if(StringUtils.isBlank(v)){
			return 0;
		}
		return StringUtil.toInt(v);
	}
}
