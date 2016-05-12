package hoperun.tumanagementdebugging.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface RepoDao{
	/**
	 * 基于开始时间和结束时间得到列表
	 * 
	 * @Title: getTBoxActivetion
	 * @Description:
	 * @param: @param
	 *             startTime
	 * @param: @param
	 *             endTime
	 * @param: @return
	 * @return: List<Map<String,String>>
	 * @throws:
	 * @author: jnad
	 * @Date: 2016年3月20日 下午3:16:20
	 */
	public List<Map<String, String>> getTBoxActivetion(
			@Param("startTime") String startTime,
			@Param("endTime") String endTime);

	/**
	 * 获取当前T-Box表数量,此数量表示已经录入至Management多少个数据
	 * 
	 * @Title: getTBoxRegistionInfoNum
	 * @Description:
	 * @param: @return
	 * @return: long
	 * @throws:
	 * @author: jnad
	 * @Date: 2016年3月20日 下午3:14:47
	 */
	public List<Map<String,String>> getTBoxRegistionInfoNum();

}
