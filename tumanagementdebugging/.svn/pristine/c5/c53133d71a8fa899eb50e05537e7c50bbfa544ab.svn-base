package hoperun.tumanagementdebugging.util;

import com.google.common.base.Preconditions;

import hoperun.tumanagementdebugging.constant.Ring;

/**
 * 振铃服务工具类
 * 
 * @classname: RingUtil.java
 * @description:
 *
 * @author: pj
 * @version: V1.0
 * @createdate: 2016年4月8日 下午3:47:16
 */
public class RingUtil {
	/**
	 * 随机选择振铃服务器
	 * 
	 * @Title: getRandomRingServerUrl
	 * @Description:
	 * @param: @param
	 *             something
	 * @param: @return
	 * @return: String
	 * @throws:
	 * @author: pj
	 * @Date: 2016年4月8日 下午3:48:31
	 */
	public static String getRandomRingBasicUrl(String something) {
		Preconditions.checkArgument(something != null, "参数为null,无法获取振铃服务器Url");
		Preconditions.checkArgument(Ring.ringBasicUrl != null && Ring.ringBasicUrl.size() > 0, "无法从配置文件获取振铃服务器Url列表");
		return Ring.ringBasicUrl.get(Math.abs(something.hashCode() % Ring.ringBasicUrl.size()));
	}
}
