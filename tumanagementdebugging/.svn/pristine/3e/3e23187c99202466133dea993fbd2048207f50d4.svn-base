package hoperun.tumanagementdebugging.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;

import hoperun.loginfo.SelfLogger;
import hoperun.proxybusiness.business.ICallServiceInterface;
import hoperun.tutransmanagement.zotye.bean.ManagementServiceCallbackBean;
import hoperun.tutransmanagement.zotye.bean.ManagementServiceCallbackHeadBean;
import hoperun.tutransmanagement.zotye.bean.TBoxBean;
import hoperun.tutransmanagement.zotye.bin.ManagmentSqlSessionFactory;
import hoperun.tutransmanagement.zotye.dao.ServiceLoginDao;
import hoperun.tutransmanagement.zotye.service.mapi.ManagementHttpService;
import hoperun.tutransmanagement.zotye.util.FJSONUtil;
import hoperun.tutransmanagement.zotye.util.HttpUtil;
import hoperun.tutransmanagement.zotye.util.HttpUtil.HttpResponseContentType;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Tu信息调试统计Http业务入口
 * 
 * @classname: ManagementdebuggingHttpService.java
 * @description:
 *
 * @author: pj
 * @version: V1.0
 * @createdate: 2016年1月11日 下午4:12:54
 */
public class ManagementdebuggingHttpService implements ICallServiceInterface {

	private final String filter = "^/managementdebugginghttpservice/service/.*$";
	private static final String[] dontNeedBidServiceNames = { "gettboxinfo", "getvinbybid",
			"getservicehandleeventresult", "getringsrvstatus" ,"getringtasklog" ,"getvinlike" ,"getmonitorlog" ,"call" ,"getmonitorlogdetail"};
	private static final String[] dontNeedVinServiceNames = { "getvinbybid", "getringsrvstatus" ,"getvinlike" ,"getmonitorlog" ,"call" ,"getmonitorlogdetail"};

	@Override
	public HttpResponse callService(HttpRequest arg0, Map<String, String> rmap) {
		rmap=compatibleConvert(rmap);//兼容请求参数名称大小写
		try (SqlSession session = ManagmentSqlSessionFactory.getSession()) {

			String serviceName = getUriData(arg0.getUri(), "service");
			ManagementServiceCallbackBean mscallback = new ManagementServiceCallbackBean();
			mscallback.setHead(new ManagementServiceCallbackHeadBean());
			ServiceLoginDao login = session.getMapper(ServiceLoginDao.class);
			TBoxBean tbox = null;// 得到需要操作的tbox信息
			String bid = rmap.get("bid");
			String vin = rmap.get("vin");
			if (isNeedBidService(serviceName)) {
				Preconditions.checkArgument(bid != null,
						String.format("Tu信息调试统计管理系统---当前业务命令: %s的bid不能为空", serviceName));
			}
			if (isNeedVinService(serviceName)) {
				Preconditions.checkArgument(vin != null,
						String.format("Tu信息调试统计管理系统---当前业务命令: %s的vin不能为空", serviceName));
				Preconditions.checkArgument(rmap.get("vin").length()<=17, String.format("vin :%s格式错误", rmap.get("vin")));
				System.err.println(rmap.get("vin").length());
			}
			if (!StringUtils.isBlank(bid) && !"getvinbybid".equals(serviceName)) {
				try {
					tbox = login.findOneTboxBybid(bid);// 当前Tbox信息
				} catch (org.apache.ibatis.exceptions.TooManyResultsException e) {
					String reason = "数据库内部数据重复错误,Expected one result (or null) to be returned by selectOne(), but found:one more";
					SelfLogger.errorInfo(null, null, null, reason, tbox);
					mscallback.getHead().setStatus("1");
					mscallback.getHead().setReason(reason);
					return returnProxyResponse(JSONObject.toJSONString(mscallback),
							HttpResponseStatus.TOO_MANY_REQUESTS);
				}
			} else if (!StringUtils.isBlank(vin)) {
				tbox = login.findOneTboxByVin(vin);
				bid = tbox.getBid();
			}
			// 当前执行的服务名称
			if ((tbox == null || tbox.getUuid() == null) && isNeedBidService(serviceName)) {
				SelfLogger.errorInfo("Management消息同步接口: ", null, null, null, "Bid未定义!");
				mscallback.getHead().setStatus("-1");
				mscallback.getHead().setReason("Bid Not Found");
				return returnProxyResponse(JSONObject.toJSONString(mscallback), HttpResponseStatus.NOT_FOUND);
			}

			String commond = getUriData(arg0.getUri(), "service");

			try {
				String responBody = null;
				SelfLogger.tempParam(String.format("Tu信息调试统计Http业务入口,当前业务命令: %s", commond));
				switch (commond) {
				case "gettboxinfo": // 查询TBOX信息详细信息
					try {
						responBody = ManagementHttpService.getTBoxInfo(rmap);
					} catch (Exception e) {
						SelfLogger.errorException(e, "", "", "gettboxinfo error: " + e.getMessage());
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "getvinbybid": // 查询bid(十进制/十六进制)/VIN绑定关系
					JSONObject jo=null;
					try {
						String shex = rmap.get("hex");
						Preconditions.checkArgument(StringUtils.isNotBlank(shex),
								String.format("hex :%s参数错误,hex代表进制信息,为必填项", shex));
						Preconditions.checkArgument("16".equals(shex) || "10".equals(shex),
								String.format("hex :%s参数错误,hex代表进制信息,只支持10进制或者16进制", shex));
						vin= ManagementdebuggingHttpServiceImpl.getVinByBid(bid, shex);
						jo=setHead("0","成功");
						FJSONUtil.setJSONValue(jo, "vin", vin);
					} catch (Exception e) {
						SelfLogger.errorException(e, "", "", "getvinbybid error: " + e.getMessage());
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(jo.toJSONString(), HttpResponseStatus.OK);
				case "getservicehandleeventresult": // 查询登陆/注册 事件结果
					try {
						rmap.put("bid", bid);
						responBody = ManagementdebuggingHttpServiceImpl
								.getServiceHandleEventResult(rmap, bid);
					} catch (Exception e) {
						SelfLogger.errorException(e, "", "", "getservicehandleeventresult error: " + e.getMessage());
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "getringsrvstatus": // 检测所有振铃服务器 可用性信息
					try {
						responBody = ManagementdebuggingHttpServiceImpl.getRingSrvStatus(rmap);
					} catch (Exception e) {
						SelfLogger.errorException(e, "", "", "getringsrvstatus error: " + e.getMessage());
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "getringtasklog": // 检测某个vin的振铃任务日志
					try {
						responBody = ManagementdebuggingHttpServiceImpl.getRingTaskLog(rmap);
					} catch (Exception e) {
						SelfLogger.errorException(e, "", "", "getringtasklog error: " + e.getMessage());
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "getvinlike": // 模糊查询vin
					try {
						responBody = ManagementdebuggingHttpServiceImpl.getVinLike(rmap);
					} catch (Exception e) {
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "getmonitorlog": // 查询基本服务错误日志信息
					try {
						responBody = ManagementdebuggingHttpServiceImpl.getMonitorLog(rmap);
					} catch (Exception e) {
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "getmonitorlogdetail": // 查询详细服务错误日志信息
					try {
						responBody = ManagementdebuggingHttpServiceImpl.getMonitorLogDetail(rmap);
					} catch (Exception e) {
						return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				case "call": // managementDebugging振铃接口
					try {
						responBody = ManagementdebuggingHttpServiceImpl.call(rmap);
					} catch (Exception e) {
						JSONObject joCall=JSONObject.parseObject(ManagementdebuggingHttpServiceImpl.CallResponseBodyModelString);
						return returnProxyResponse(setHead(joCall,"1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
					}
					return returnProxyResponse(responBody, HttpResponseStatus.OK);
				default:
					SelfLogger.errorInfo("Tu信息调试统计管理系统: ", null, null, null, "接口未定义!");
					mscallback.getHead().setStatus("-1");
					mscallback.getHead().setReason("Interface Not Found");
					return returnProxyResponse(JSONObject.toJSONString(mscallback), HttpResponseStatus.NOT_FOUND);
				}
			} catch (Exception e) {
				e.printStackTrace();
				SelfLogger.errorInfo("Tu信息调试统计管理系统,执行错误", null, null, e.getMessage(), tbox);
				return returnProxyResponse(JSONObject.toJSONString(mscallback), HttpResponseStatus.EXPECTATION_FAILED);
			}
		} catch (Exception e) {
			return returnProxyResponse(setHead("1", e.getMessage()), HttpResponseStatus.BAD_REQUEST);
		}

	}

	/**
	 * 返回Proxy标准接口的默认返回 Response
	 * 
	 * @param content
	 * @param stat
	 * @return
	 */
	private FullHttpResponse returnProxyResponse(Object content, HttpResponseStatus stat) {
		return HttpUtil.getFullHttpResponse(content, HttpResponseContentType.proxy_json, stat);
	}

	/**
	 * 获取url内dataType
	 * 
	 * @param uri
	 * @return
	 */
	private String getUriData(String uri, String dataTypeStr) {

		/*
		 * 直接根据URL 进行分析.因为在前面已经校验过url直接截取判断
		 */
		String s[] = uri.split("\\?")[0].split("/");
		for (int i = s.length - 1; i >= 0; i--) {
			if (s[i].equalsIgnoreCase(dataTypeStr)) {
				// 得到下一个参数
				return s[i + 1];
			}
		}
		return null;

	}

	/**
	 * 参数大小写兼容
	 * 
	 * @Title: compatibleConvert
	 * @Description:
	 * @param: @param
	 *             map
	 * @param: @return
	 * @return: Map<String,String>
	 * @throws:
	 * @author: pj
	 * @Date: 2015年6月6日 下午3:55:02
	 */
	public static Map<String, String> compatibleConvert(Map<String, String> map) {
		Map<String, String> rmap = new HashMap<String, String>();
		Set<Map.Entry<String, String>> list = map.entrySet();
		for (Map.Entry<String, String> me : list) {
			rmap.put(me.getKey().toString().toLowerCase(), me.getValue().toString());
		}
		return rmap;
	}

	/**
	 * 判断服务是否需要bid或者vin的信息
	 * 
	 * @Title: isNeedBidService
	 * @Description:
	 * @param: @param
	 *             serviceName
	 * @param: @return
	 * @return: boolean
	 * @throws:
	 * @author: pj
	 * @Date: 2015年7月23日 下午12:03:48
	 */
	public static boolean isNeedBidService(String serviceName) {
		for (int i = 0; i < dontNeedBidServiceNames.length; i++) {
			if (serviceName.equals(dontNeedBidServiceNames[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNeedVinService(String serviceName) {
		for (int i = 0; i < dontNeedVinServiceNames.length; i++) {
			if (serviceName.equals(dontNeedVinServiceNames[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getThisUrlFilter() {
		return filter;
	}

	/**
	 * 
	 * 
	 * @Title: setHead
	 * @Description:
	 * @param: @param
	 *             jo
	 * @param: @param
	 *             status
	 * @param: @param
	 *             reason
	 * @param: @return
	 * @return: JSONObject
	 * @throws:
	 * @author: pj
	 * @Date: 2015年12月3日 下午12:12:08
	 */
	public static JSONObject setHead(JSONObject jo, String status, String reason) {
		jo = FJSONUtil.setJSONValue(jo, "head/status", status);
		jo = FJSONUtil.setJSONValue(jo, "head/reason", reason);
		return jo;
	}

	public static JSONObject setHead(String status, String reason) {
		JSONObject jo = JSONObject.parseObject("{\"head\":{\"reason\":\"\",\"status\":\"\"}}");
		jo = FJSONUtil.setJSONValue(jo, "head/status", status);
		jo = FJSONUtil.setJSONValue(jo, "head/reason", reason);
		return jo;
	}
}
