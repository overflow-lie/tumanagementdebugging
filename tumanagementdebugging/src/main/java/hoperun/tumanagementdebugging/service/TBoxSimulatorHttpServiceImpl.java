package hoperun.tumanagementdebugging.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import hoperun.adapter.comm.bean.TBoxMessage;
import hoperun.adapter.comm.convert.Serialize2TBoxMessage;
import hoperun.adapter.comm.util.TByteUtil;
import hoperun.loginfo.SelfLogger;
import hoperun.proxybusiness.business.ICallServiceInterface;
import hoperun.tutransmanagement.zotye.util.HttpUtil;
import hoperun.tutransmanagement.zotye.util.HttpUtil.HttpResponseContentType;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class TBoxSimulatorHttpServiceImpl implements ICallServiceInterface {

	private final String filter = "^/managementdebugginghttpservice/simulator/.*";

	@Override
	public HttpResponse callService(HttpRequest request, Map<String, String> rmap) {

		try {
			 String serviceName = getUriData(request.getUri(), "simulator");
			 switch (serviceName) {
			 	 case "gettboxmessage":
			 		 return getTBoxMessage(rmap.get("byteTBoxMessage"));
				 default:
					 break;
			}
		} catch (Exception e) {
			return returnProxyResponse("服务器产生了以下错误: " + e.getMessage(), HttpResponseStatus.valueOf(500));
		}

		return returnProxyResponse("未知的命令", HttpResponseStatus.valueOf(404));
	}

	private HttpResponse getTBoxMessage(String byteTBoxMessage) {

		TBoxMessage tm = null;
		String resultMessage = null;
		String status = "OK";
		try {
			tm = Serialize2TBoxMessage.convert(TByteUtil.parseHstring(byteTBoxMessage.replace(" ","")));
		} catch (Exception e) {
			e.printStackTrace();
			resultMessage ="tcp消息转换为Tbox失败!";
			status = "!OK";
		}
		
		int aid = tm.getFeature().getDispatch().getAid();

		switch (aid) {
			case 0x1: // 注册
				break;
			case 0x2:// 登陆
				break;
			case 0x3:// 登出
				break;
			case 0x4:// 重新登陆
				break;
			case 0x5:// 配置读取
				break;
			case 0x6:// 版本升级
				break;
			case 0x7:// 配置下发
				break;
			case 0xB:// 取消息心跳
				resultMessage = "取消息心跳";
				break;
			case 0xF1:// 车辆控制下发
				break;
			case 0xF2:// Can数据采集上报
				resultMessage = "Can数据采集上报";
				break;
			case 0xF3:// 故障状态上报
				break;
			case 0xF4:// 故障采集
				break;
			case 0xF5:// 仪表数据上报
				break;
			case 0xF6:// E-call服务
				break;
			default:
				resultMessage ="tcp消息转换为Tbox失败!"+"error aid, [ " + aid + " ]";
				SelfLogger.tempParam("error aid", "[ " + aid + " ]");
				break;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("header",tm.getFeature().getHeader());
		map.put("dispatcher", tm.getFeature().getDispatch());
		map.put("tboxMessage", tm);
		map.put("resultMessage", resultMessage);
		map.put("eventCreationTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(tm.getFeature().getDispatch().getEventCreationTime())));
		map.put("dispatchCreationTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(tm.getFeature().getDispatch().getDispatchCreationTime())));
		map.put("status",status);
		map.put("aid", "0x"+Integer.toHexString(aid));
		return returnProxyResponse(JSONObject.toJSON(map), HttpResponseStatus.OK);
	}

	@Override
	public String getThisUrlFilter() {
		return filter;
	}
	
	private String getUriData(String uri, String dataTypeStr) {
		String s[] = uri.split("\\?")[0].split("/");
		for (int i = s.length - 1; i >= 0; i--) {
			if (s[i].equalsIgnoreCase(dataTypeStr)) {
				// 得到下一个参数
				return s[i + 1];
			}
		}
		return null;
	}

	private FullHttpResponse returnProxyResponse(Object content, HttpResponseStatus stat) {
		return HttpUtil.getFullHttpResponse(content, HttpResponseContentType.proxy_json, stat);
	}

}
