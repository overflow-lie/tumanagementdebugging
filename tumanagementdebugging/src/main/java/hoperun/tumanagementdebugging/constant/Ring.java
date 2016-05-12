package hoperun.tumanagementdebugging.constant;

import java.util.List;

import com.google.common.collect.Lists;


/**
 * 振铃服务器常量类
 * @classname:		Ring.java
 * @description:	
 *
 * @author:			pj
 * @version:		V1.0
 * @createdate:		2016年4月8日 下午3:37:43
 */
public class Ring {
	/**
	 * 振铃服务器列表
	 */
	public static final List<String> ringBasicUrl=Lists
			.newArrayList(TuDebuggingCanstant.getConstant()
					.getMapProperties("ring.basicurl").split(","));
	/**
	 * 振铃接口Url
	 */
	public static final String callByPhoneNumberIFUrl="http://{ip}:{port}/call/phone/{phoneNumber}";
}
