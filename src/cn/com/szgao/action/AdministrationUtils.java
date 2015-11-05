package cn.com.szgao.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通过企业名称和法院名称获得行政区划
 * @author xiongchangyi
 * @version V1.0
 */
public class AdministrationUtils {	
	//工具的主方法
	public  String[] utils(String doString) {
		if(null == doString)
		{
			return null;
		}		
		//需要处理的字符串
		doString = doString.replace(" ", "");
		// 包含法院   
		if(doString.contains("法院"))
		{
			return court(doString);//法院数据
		}
		//公司
		else
		{
			return enterp(doString);//企业名录数据, 结果数组有可能为空
		}
	}
	/**
	 * 处理企业名录
	 * 提取省、市、县
	 * @param doString：需要处理的字符串；utils：工具类对象
	 * @Vesion 1.0
	 * @author xiongchangyi
	 * @since 2015-6-12
	 */
	public  String[] enterp(String doString)
	{
		if(null == doString || "".equals(doString))
		{
			return null;
		}
		//将名称分成2截	 这里可能的漏洞是分公司、与没有分公司的情况，需要验证是否可行	
		int length = doString.length();
		String oneString = doString.substring(0, length/2);
		String twoString = doString.substring(length/2);	
		Map<Integer,String> resultMap = subCompanyDo(oneString,twoString);//提取省市县区
		//处理结果的数组：result[0]存储省   result[1]存储地级市     result[2]存储县
		return doProvinceCityCountry(resultMap);
	}
	/**
	 * 在企业名称、法院名称上提取的行政区
	 * 去获得完整的省、市、县
	 * @param resultMap： 需要完整的行政处
	 * @return
	 */
	public  String[] doProvinceCityCountry(Map<Integer,String> resultMap)
	{
		DataUtils utils = new DataUtils();
		//处理结果的数组：result[0]存储省   result[1]存储地级市     result[2]存储县
		String result[] = new String[3];
		String province_result = null;
		String city_result = null;
		String country_result = null;
		//处理省、市、县
		if(null!=resultMap)
		{
			province_result = resultMap.get(1);
			city_result = resultMap.get(2);
			country_result = resultMap.get(3);
			
			//有县无地级市，则查地级市，附带判断省是否存在
			if(null == city_result && null != country_result)
			{
				//获得地级市ID
				int city_id = DataUtils.xianJiShiXianQuMap.get(country_result);
				//通过地级市ID，查询地级市名称
				//查询【地级市名称和省ID】,cityNameAndProvinceIdMap
				Map<String,Integer> map = utils.listCityProvinceIdByCityId(city_id);
				if(null != map && map.size() != 0)
				{
					Set<String> set = map.keySet();
					Iterator<String> it = set.iterator(); 
					if(it.hasNext())
					{
						city_result = it.next();
					}
					//无省，则查省
					if(null == province_result)
					{
						//获得省ID
						int province_id = map.get(city_result);
						//通过省ID查询省名称
						String province_name = utils.listProvinceNameByProvinceId(province_id);
						if(null != province_name)
						{
							province_result = province_name;
						}
					}
				}
			}
			// 有市，县有则有，无则无法查
			// 有市无省
			if(null != city_result && null == province_result)
			{
				// 通过市名称，查询省ID
				int province_id = DataUtils.province_city_map.get(city_result);
				//通过省ID查询省名称
				String province_name = utils.listProvinceNameByProvinceId(province_id);
				if(null != province_name)
				{
					province_result = province_name;
				}
			}
			System.out.println("省："+province_result + "\t" +"市："+ city_result+"\t"+"县：" + country_result);
		}
		result[0] = province_result;// result[0]存储省
		result[1] = city_result;    // result[1]存储地级市
		result[2] = country_result; // result[2]存储县
		utils = null;
		return result;
	}
	
	/**
	 * 提取省、地级市、县【县级市、县、旗、区】
	 * @param oneString  企业名称字符串的前半部分
	 * @param twoString  企业名称字符串的后半部分
	 * @return map:  key=1是省，key=2是地级市，key=3是县
	 * @Vesion 1.0
	 * @author xiongchangyi
	 * @since 2015-6-12
	 */
	public  Map<Integer,String> subCompanyDo(String oneString,String twoString)
	{
		Map<Integer,String> resultMap = new HashMap<Integer,String>();//省的key是1，市的key是2，县的key是3
		List<String> shengList = new ArrayList<String>();
		shengList.addAll(DataUtils.shengMap.keySet());
		
		List<String> shiList = new ArrayList<String>();
		shiList.addAll(DataUtils.shiMap.keySet());
		
		List<String> xianList = new ArrayList<String>();
		xianList.addAll(DataUtils.xianJiShiXianMap.keySet());
		
		//遍历省，在第2截里面找【省】
		for(int i = 0;i<shengList.size();i++)
		{
			String tempSheng = shengList.get(i);
			if(twoString.contains(tempSheng+"路"))//避免  陕西路
			{
				if(twoString.contains(DataUtils.shengMap.get(tempSheng)))
				{
					resultMap.put(1,DataUtils.shengMap.get(tempSheng));//省
					break;
				}
			}
			else if(twoString.contains(tempSheng))
			{
				resultMap.put(1,DataUtils.shengMap.get(tempSheng));//省
				break;
			}			
		}
		
		//遍历地级市 ，在第2截里面找【地级市】
		for(int i = 0; i < shiList.size(); i++)
		{
			String tempShi = shiList.get(i);
			if(twoString.contains(tempShi+"路"))//避免类似  "天津路"  的情况
			{
				//使用类似  "天津市"去判断
				if(twoString.contains(DataUtils.shiMap.get(tempShi)))
				{
					resultMap.put(2,DataUtils.shiMap.get(tempShi));  //地级市
					break;
				}
			}
			else if(twoString.contains(tempShi))
			{
				resultMap.put(2,DataUtils.shiMap.get(tempShi));  //地级市
				break;
			}	
		}		
		if(null == resultMap.get(2))
		{
			//遍历县级市、县、旗，在第2截里面找 【县级市、县、旗】
			for(int i = 0; i < xianList.size(); i++)
			{
				String tempXian = xianList.get(i);			
				if(twoString.contains(tempXian))
				{
					//需要往前推一个字，避免出现处理：北京市密云县人民法院    结果：省：null 市：北京市	县：云县
					int index = twoString.indexOf(tempXian);
					String temp = twoString.substring(0, index);
					if(temp.length() > 0)//避免 云县人民法院
					{
						String sub = twoString.substring(index-1, index);//云县，再往前推一个字
						if(null != DataUtils.xianJiShiXianMap.get(sub+tempXian))
						{
							resultMap.put(3,DataUtils.xianJiShiXianMap.get(sub+tempXian)); //县、县级市、旗
							break;
						}
					}
					resultMap.put(3,DataUtils.xianJiShiXianMap.get(tempXian)); //县、县级市、旗
					break;
				}	
			}	
			
			//无县级市、县、旗，则找区
			if(null == resultMap.get(3))
			{
				//遍历区			
				if(null == resultMap.get(3))
				{
					for(int i = 0; i < DataUtils.quList.size(); i++)
					{
						String tempArea = DataUtils.quList.get(i);
						if(twoString.contains(tempArea))
						{
							//需要往前推一个字，避免出现处理：北京市密云县人民法院    结果：省：null 市：北京市	县：云县
							int index = twoString.indexOf(tempArea);
							String temp = twoString.substring(0, index);
							if(temp.length() > 0)//避免 云县人民法院
							{
								String sub = twoString.substring(index-1, index);//云县，再往前推一个字
								if(null != DataUtils.xianJiShiXianMap.get(sub+tempArea))
								{
									resultMap.put(3,DataUtils.xianJiShiXianMap.get(sub+tempArea)); //县、县级市、旗
									break;
								}
							}
							resultMap.put(3,tempArea);
							break;
						}
					}
				}
			}
		}
		//第2截里面，没有找到 【省、市、县】且一个都没没找到，则才去找【第1截】
		if(null == resultMap.get(1) && null == resultMap.get(2) && null == resultMap.get(3))
		{
			//在第1截里面找【省】
			for(int i = 0;i<shengList.size();i++)
			{
				String tempSheng = shengList.get(i);
				if(oneString.contains(tempSheng+"路"))//避免  陕西路
				{					
					if(oneString.contains(DataUtils.shengMap.get(tempSheng)))//拿全面去判断是否存在
					{
						resultMap.put(1,DataUtils.shengMap.get(tempSheng));//省
						break;
					}
				}
				else if(oneString.contains(tempSheng))
				{
					resultMap.put(1,DataUtils.shengMap.get(tempSheng));//省
					break;
				}			
			}
			
			//在第1截里面找【地级市】
			for(int i = 0; i < shiList.size(); i++)
			{
				String tempShi = shiList.get(i);
				if(oneString.contains(tempShi+"路"))//避免类似  "天津路"  的情况
				{
					//使用类似  "天津市"去判断
					if(oneString.contains(DataUtils.shiMap.get(tempShi)))
					{
						resultMap.put(2,DataUtils.shiMap.get(tempShi));  //地级市
						break;
					}
				}
				if(oneString.contains(tempShi))
				{
					resultMap.put(2,DataUtils.shiMap.get(tempShi));  //地级市
					break;
				}	
			}
			if(null == resultMap.get(2))
			{
				//在第1截里面找【县、县级市、旗】
				if(null == resultMap.get(3))
				{
					for(int i = 0; i < xianList.size(); i++)
					{
						String tempXian = xianList.get(i);
						if(oneString.contains(tempXian))
						{
							//需要往前推一个字，避免出现处理：北京市密云县人民法院    结果：省：null 市：北京市	县：云县
							int index = oneString.indexOf(tempXian);
							String temp = oneString.substring(0, index);
							if(temp.length() > 0)//避免 云县人民法院
							{
								String sub = oneString.substring(index-1, index);//云县，再往前推一个字
								if(null != DataUtils.xianJiShiXianMap.get(sub+tempXian))
								{
									resultMap.put(3,DataUtils.xianJiShiXianMap.get(sub+tempXian)); //县、县级市、旗
									break;
								}
							}
							resultMap.put(3,DataUtils.xianJiShiXianMap.get(tempXian)); //县、县级市、旗
							break;
						}	
					}	
				}
				
				//【无】县级市、县、旗，则找【区】
				if(null == resultMap.get(3))
				{
					//遍历区
					for(int i = 0; i < DataUtils.quList.size(); i++)
					{
						String tempArea = DataUtils.quList.get(i);
						if(oneString.contains(tempArea))
						{
							//需要往前推一个字，避免出现处理：北京市密云县人民法院    结果：省：null 市：北京市	县：云县
							int index = oneString.indexOf(tempArea);
							String temp = oneString.substring(0, index);
							if(temp.length() > 0)//避免 云县人民法院
							{
								String sub = oneString.substring(index-1, index);//云县，再往前推一个字
								if(null != DataUtils.xianJiShiXianMap.get(sub+tempArea))
								{
									resultMap.put(3,DataUtils.xianJiShiXianMap.get(sub+tempArea)); //县、县级市、旗
									break;
								}
							}
							resultMap.put(3,tempArea);
							break;
						}
					}
				  }
			}
		}		
		//返回的 resultMap有可能为空
		return resultMap;
	}
	
	/**
	 * 法院
	 */
	public  String[] court(String doString)
	{
		//result[0]是省，result[1]是市， result[3]是县
		String result[] = new String[3];
		Map<Integer,String> resultMap = new HashMap<Integer,String>();
		
		List<String> shengList = new ArrayList<String>();
		shengList.addAll(DataUtils.shengMap.keySet());
		
		List<String> shiList = new ArrayList<String>();
		shiList.addAll(DataUtils.shiMap.keySet());
		
		List<String> xianList = new ArrayList<String>();
		xianList.addAll(DataUtils.xianJiShiXianMap.keySet());
		
		//遍历省，在第2截里面找【省】
		for(int i = 0;i<shengList.size();i++)
		{
			String tempSheng = shengList.get(i);
			if(doString.contains(tempSheng))
			{
				resultMap.put(1,DataUtils.shengMap.get(tempSheng));//省
				break;
			}			
		}
		
		//遍历地级市 ，在第2截里面找【地级市】
		for(int i = 0; i < shiList.size(); i++)
		{
			String tempShi = shiList.get(i);
			if(doString.contains(tempShi))
			{
				resultMap.put(2,DataUtils.shiMap.get(tempShi));  //地级市
				break;
			}	
		}		
				
		//遍历县级市、县、旗，找 【县级市、县、旗】
		for(int i = 0; i < xianList.size(); i++)
		{
			String tempXian = xianList.get(i);			
			if(doString.contains(tempXian))
			{
				//需要往前推一个字，避免出现处理：北京市密云县人民法院    结果：省：null 市：北京市	县：云县
				int index = doString.indexOf(tempXian);
				String temp = doString.substring(0, index);
				if(temp.length() > 0)//避免 云县人民法院
				{
					String sub = doString.substring(index-1, index);//云县，再往前推一个字
					if(null != DataUtils.xianJiShiXianMap.get(sub+tempXian))
					{
						resultMap.put(3,DataUtils.xianJiShiXianMap.get(sub+tempXian)); //县、县级市、旗
						break;
					}
				}
				resultMap.put(3,DataUtils.xianJiShiXianMap.get(tempXian)); //县、县级市、旗
				break;
			}	
		}	
		
		//无县级市、县、旗，则找区
		if("".equals(result[2])||null == result[2])
		{
			for(int i = 0; i < DataUtils.quList.size(); i++)
			{
				String tempArea = DataUtils.quList.get(i);
				if(doString.contains(tempArea))
				{
					//需要往前推一个字，避免出现处理：北京市密云县人民法院    结果：省：null 市：北京市	县：云县
					int index = doString.indexOf(tempArea);
					String temp = doString.substring(0, index);
					if(temp.length() > 0)//避免 云县人民法院
					{
						String sub = doString.substring(index-1, index);//云县，再往前推一个字
						if(DataUtils.quList.contains(sub+tempArea))
						{
							resultMap.put(3,sub+tempArea); //县、县级市、旗
							break;
						}
					}
					resultMap.put(3,tempArea);
					break;
				}
			}
		}
		return doProvinceCityCountry(resultMap);
	}
	/**
	 * 初始化省、市县数据
	 */
	public void initData()
	{
		//查询行政区数据
		DataUtils utils = new DataUtils();
		//查询【省】，shengMap
		utils.listProvince();
		//查询【地级市】, shiMap
		utils.listCity();
		//查询【县级市】, xianJiShiMap
		utils.listAllShortCityName();
		//查询【县】 , xianMap
		utils.listAllCountry();
		//查询【区】 , quMap
		utils.listAllCountry();
		//查询【县级市、县、旗】, xianJiShiXianMap
		utils.listXianJiShiXian();
		//查询【县级市、县、旗】,xianJiShiXianQuMap
		utils.listXianJiShiXianQu();
		//查询【市名称】和【省的ID】,province_city_map
		utils.listProvinceCity();
		//查询所有区，不包括  县、旗、县级市
		utils.listAllArea();
	}
}
