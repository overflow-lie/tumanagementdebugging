package hoperun.tumanagementdebugging.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;

import hoperun.proxybusiness.business.ICallServiceInterface;
import hoperun.tumanagementdebugging.dao.RepoDao;
import hoperun.tutransmanagement.zotye.bin.ManagmentSqlSessionFactory;
import hoperun.tutransmanagement.zotye.util.HttpUtil;
import hoperun.tutransmanagement.zotye.util.HttpUtil.HttpResponseContentType;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class ManagementRepoHttpService implements ICallServiceInterface{
	private final String filter = "^/managementdebugginghttpservice/repo/.*$";

	@Override
	public HttpResponse callService(HttpRequest request,
			Map<String, String> requestMap){

		try{
			String commod = getUriData(request.getUri(), "repo");
			switch(commod){
				case "gettboxactivelist":
					return getTBoxActiveList(requestMap.get("startTime"),
							requestMap.get("endTime"));
				case "getbasicrepoinfo":
					return getBasicRepoInfo();

				default:
					break;
			}
		} catch(Exception e){
			return returnProxyResponse("服务器产生了以下错误: " + e.getMessage(),
					HttpResponseStatus.valueOf(500));
		}
		return returnProxyResponse("未知的命令", HttpResponseStatus.valueOf(404));
	}

	private String getUriData(String uri, String dataTypeStr){

		/*
		 * 直接根据URL 进行分析.因为在前面已经校验过url直接截取判断
		 */
		String s[] = uri.split("\\?")[0].split("/");
		for(int i = s.length - 1; i >= 0; i--){
			if(s[i].equalsIgnoreCase(dataTypeStr)) {
				// 得到下一个参数
				return s[i + 1];
			}
		}
		return null;

	}

	@Override
	public String getThisUrlFilter(){
		return filter;
	}

	/**
	 * 
	 * @Title: getTBoxActiveList
	 * @Description:
	 * @param: @param
	 *             startTime
	 * @param: @param
	 *             endTime
	 * @param: @return
	 * @return: HttpResponse
	 * @throws:
	 * @author: jnad
	 * @Date: 2016年3月20日 下午3:06:15
	 */
	public HttpResponse getTBoxActiveList(String startTime, String endTime){

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");
		Map<String, Object> re = new HashMap<>();

		//检查输入数据是否符合要求
		if(StringUtils.isNotBlank(startTime)) {
			try{
				format.parse(startTime);
			} catch(ParseException e){
				Preconditions.checkArgument(true, "当前传入开始时间%s不正确!", startTime);
			}
		} else{
			startTime = null;
		}
		if(StringUtils.isNotBlank(endTime)) {
			try{
				format.parse(endTime);
			} catch(ParseException e){
				Preconditions.checkArgument(true, "当前传入结束时间%s不正确!", endTime);
			}
		} else{
			endTime = null;
		}

		//查询DB 返回报表信息
		try(SqlSession session = ManagmentSqlSessionFactory.getSession()){
			RepoDao repo = session.getMapper(RepoDao.class);
			re.put("list", repo.getTBoxActivetion(startTime, endTime));
			re.put("status", "OK");
		}

		return returnProxyResponse(JSONObject.toJSONString(re), HttpResponseStatus.OK);
	}

	private FullHttpResponse returnProxyResponse(Object content,
			HttpResponseStatus stat){
		return HttpUtil.getFullHttpResponse(content,
				HttpResponseContentType.proxy_json, stat);
	}

	/**
	 * 获取当前Management的基本配置
	 * 
	 * @Title: getBasicRepoInfo
	 * @Description:
	 * @param: @return 共有多少数据,未注册 已注册 与 已登录分别多少,昨日累计振铃数量(一次就激活的,重试激活的,超过三次才激活的)
	 * @return: HttpResponse
	 * @throws:
	 * @author: jnad
	 * @Date: 2016年3月20日 下午3:12:30
	 */
	public HttpResponse getBasicRepoInfo(){
		// 已经录入VIN数据量
		//
		return null;
	}

}
