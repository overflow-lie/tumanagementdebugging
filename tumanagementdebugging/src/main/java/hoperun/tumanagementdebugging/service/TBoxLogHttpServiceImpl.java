package hoperun.tumanagementdebugging.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSONObject;

import hoperun.proxybusiness.business.ICallServiceInterface;
import hoperun.tumanagementdebugging.util.SortLinkUpdateRequestTest;
import hoperun.tutransmanagement.zotye.util.HttpUtil;
import hoperun.tutransmanagement.zotye.util.HttpUtil.HttpResponseContentType;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class TBoxLogHttpServiceImpl implements ICallServiceInterface {

	private final String filter = "^/zotyetuadapterwebtask/tboxlog/.*";

	public HttpResponse callService(HttpRequest request, Map<String, String> requestMap) {

		try {
			String serviceName = getUriData(request.getUri(), "tboxlog");
			switch (serviceName) {
			case "starttask":
				return startTask(requestMap);
			case "getlog":
				return getLogs(requestMap);
			case "offtask":
				return offtask(requestMap);
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return returnProxyResponse("服务器产生了以下错误: " + e.getMessage(), HttpResponseStatus.valueOf(500));
		}

		return returnProxyResponse("未知的命令", HttpResponseStatus.valueOf(404));
	}

	private HttpResponse startTask(Map<String, String> requestMap) {
		
		final String ip = requestMap.get("ip");
		final String port = requestMap.get("port");
		final String bid = requestMap.get("bid");

		final String status = "OK";
		Map<String,Object> resultMap = new HashMap<>();

		if(ip==null||port==null||bid==null){
			resultMap.put("status","!"+status);
			resultMap.put("errorMsg", "请输入完整信息");
			return returnProxyResponse(JSONObject.toJSON(resultMap),HttpResponseStatus.OK );
		}
		
		try{
			// 发送初始化消息给服务器
			SortLinkUpdateRequestTest sortLinkUpdateRequestTest = new SortLinkUpdateRequestTest(bid,port,ip);
			sortLinkUpdateRequestTest.begin();

		}catch(Exception e){
			e.printStackTrace();
		}
		
		return returnProxyResponse(JSONObject.toJSON(resultMap),HttpResponseStatus.OK );
	}

	private HttpResponse offtask(Map<String, String> requestMap) {
		
		final String ip = requestMap.get("ip");
		final String port = requestMap.get("port");
		final String bid = requestMap.get("bid");
		final String logCacheName = bid+"_"+port+"_"+ip;
		final String status = "OK";
		
		Map<String,Object> resultMap = new HashMap<>();
        
		SortLinkUpdateRequestTest.getTcf().closeTcpClient(logCacheName);
		SortLinkUpdateRequestTest.getLogCache().invalidate(logCacheName);
		resultMap.put("status", status);
		resultMap.put("errorMsg", "已结束测试");
		return returnProxyResponse(JSONObject.toJSON(resultMap),HttpResponseStatus.OK );
	}

	private HttpResponse getLogs(Map<String, String>  requestMap) {

		final String ip = requestMap.get("ip");
		final String port = requestMap.get("port");
		final String bid = requestMap.get("bid");
		final String logCacheName = bid+"_"+port+"_"+ip;
		Map<String,Object> resultMap = new HashMap<>();
		try {
			List<String> list = SortLinkUpdateRequestTest.getLogCache().get(logCacheName);
			resultMap.put("log", list);
			resultMap.put("status", "OK");
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		return returnProxyResponse(JSONObject.toJSON(resultMap),HttpResponseStatus.OK );
	}

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
