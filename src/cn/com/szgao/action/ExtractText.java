package cn.com.szgao.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;
import cn.com.szgao.dao.impl.CollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.service.impl.CollectDataService;
import cn.com.szgao.util.CommonConstant;

public class ExtractText {
	private static Logger logger = LogManager.getLogger(ExtractText.class
			.getName());
	static ApplicationContext application = new ClassPathXmlApplicationContext(
			"classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory = (SessionFactory) application
			.getBean("sessionFactory");
	public static String[] BOOKCLASS = { "民事调解书", "民事裁定书", "民事判决书", "民事决定书",
			"刑事判决书", "刑事裁定书", "刑事决定书", "行政判决书", "行政决定书", "行政裁定书", "执行裁定书",
			"执行判决书", "执行决定书", "国家赔偿裁定书", "国家赔偿判决书", "国家赔偿决定书", "驳回申诉通知书",
			"决定书", "通知书" };// 文书类型
	public static String[] CAUSENUM = { "（２０","(2０","（2０","〔2０","[2０","【2０","(20", "（20", "〔20","［2", "[20", "【20","(19", "（19", "〔19", "[19", "【19" };
	public static String[] CAUSENUM2={"判决书字号","字号"};
	public static String[] CAUSENUM3={"第","字"};
	static long count = 0;// 总数量
	static long ERRORSUM = 0;// 出错数据
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
		MAPS.put("txt", "txt");
	}

	/**
	 * 裁判文书 更新couchbase 抓取HTML修改court
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		File filepor = new File("E:\\Company_File\\log4j-0924\\Java1\\batchImport.log");
		 if(filepor.exists()){
		 filepor.delete();//删除日志文件
		 }
		 filepor=null;
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		File file = new File("F:/DataSheet/DetailedPage/裁判文书/DistrictCourtHtm/贵州省/贵州省/册亨县人民法院/0e31e0a4-3e98-5467-9d6e-d436eb9f6274.html");
		Bucket bucket = CommonConstant.connectionCouchBase();
		try {
			show(file, bucket);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			file = null;
			bucket.close();
		}
		logger.info(count + ":数量");
		logger.info("所有文件总耗时"
				+ (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}

	/**
	 * 递归遍历html文件
	 * 
	 * @param file
	 * @throws
	 * @throws Exception
	 */
	private static void show(File file, Bucket bucket) throws Exception {
		CollectionDataDao dataDao = new CollectionDataDao();
		dataDao.setSf(sessionFactory); 
		CollectDataService service = new CollectDataService();
		service.setiCollectionDataDao(dataDao);
		String html = null;
		Map<String, List<String>> list = null;
		ArchivesVO arch = null;
		List<ArchivesVO> listarchs = null;
		listarchs = new ArrayList<ArchivesVO>();
		if (file.isFile()) {
			arch = new ArchivesVO();
			logger.info(file.getPath());
			String suffix = file.getName();
			suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
			suffix = MAPS.get(suffix);
			if (null == suffix) {
				return;
			}
			logger.info("网址:" + file.getPath());
			html = URLText.getText("file:///", file.getPath());
			if (html == null || "".equals(html)) {
				logger.info(file.getPath() + ":网页地址访问失败!");
				return;
			}
			html = ExtractText.getReplaceAll(html).trim();
			logger.info("所有内容：" + html);
			// html = ExtractText.getAll(html);
			list = ExtractthepeopleText.getPersonName(html);
			arch.setPlaintiff(ExtractthepeopleText.getKeyName(list, 1));
			logger.info("<<------------------------------------------------------>>");
			logger.info("原告相关人:" + arch.getPlaintiff());
			arch.setDefendant(ExtractthepeopleText.getKeyName(list, 2));
			logger.info("被告相关人:" + arch.getDefendant());
			arch.setCourtName(TextName.getAtherthe(html));
			// arch.setCourtName(ExtractText.getCourtName(html));
			logger.info("法院:" + arch.getCourtName());
			arch.setCaseCause(TextName.StringCause(html));
			logger.info("案由:" + arch.getCaseCause());
			arch.setApprovalDate(TextName.getConcludeDate(html));
			logger.info("审结日期:" + arch.getApprovalDate());
			arch.setApproval(TextName.getTheVerdictData(html));
			logger.info("判决结果:" + arch.getApproval());
			// arch.setSuitType(TextName.getRunningClass(html));
			arch.setSuitType(ExtractText.getSuitType(html));
			logger.info("文书类型:" + arch.getSuitType());
			if (!"".equals(ExtractText.getCaseNum(html))|| ExtractText.getCaseNum(html) != null) {
				arch.setCaseNum(ExtractText.getCaseNum(html));
			}
			if ("".equals(ExtractText.getCaseNum(html))|| ExtractText.getCaseNum(html) == null) {
				arch.setCaseNum(TextName.getSentenceNo3(html));
			} 
			logger.info("案号:" + arch.getCaseNum());
			arch.setTitle(TextName.getTitle2(file.getPath()));
			logger.info("标题:" + arch.getTitle());
			logger.info("<<------------------------------------------------------>>");
			arch.setUuid(file.getName().substring(0,
					file.getName().lastIndexOf(".")));
			listarchs.add(arch);
			logger.info("<<-------------------------------2----------------------->>");
			boolean result = ExtractText.updateJsonData(listarchs, bucket);
			logger.info("<<-------------------------result----------------------------->>"
					+ result);
			if (!result) {
				logger.info(file.getPath() + ":更新失败");
			}
			count += listarchs.size();
			logger.info("<<------------------------count------------------------------>>"
					+ count);
			listarchs = null;
			return;
		}
		logger.info("<<----------------------------end-------------------------->>");
		File[] files = file.listFiles();
		for (File fi : files) {
			if (fi.isFile()) {
				arch = new ArchivesVO();
				logger.info("---fi-----" + fi.getPath());
				String suffix = fi.getName();
				suffix = suffix.substring(suffix.indexOf(".") + 1,
						suffix.length());
				suffix = MAPS.get(suffix);
				if (null == suffix) {
					continue;
				}
				html = URLText.getText("file:///", fi.getPath());
				// logger.info("网址:" + fi.getPath());
				if (html == null || "".equals(html)) {
					logger.error(fi.getPath() + ":网页地址访问失败!");
					continue;
				}
				html = ExtractText.getReplaceAll(html);
				// html = ExtractText.getAll(html);
				// logger.info("所有内容:"+html);
				list = ExtractthepeopleText.getPersonName(html);
				// logger.info("所有相关人:" + list);
				arch.setPlaintiff(ExtractthepeopleText.getKeyName(list, 1));
				// logger.info("<<------------------------------------------------------>>");
				// logger.info("原告相关人:" + arch.getPlaintiff());
				arch.setDefendant(ExtractthepeopleText.getKeyName(list, 2));
				// logger.info("被告相关人:" + arch.getDefendant());
				arch.setCourtName(TextName.getAtherthe(html));
				// arch.setCourtName(ExtractText.getCourtName(html));
				// logger.info("法院:" + arch.getCourtName());
				arch.setCaseCause(TextName.StringCause(html));
				// logger.info("案由:" + arch.getCaseCause());
				arch.setApprovalDate(TextName.getConcludeDate(html));
				// logger.info("审结日期:" + arch.getApprovalDate());
				arch.setApproval(TextName.getTheVerdictData(html));
				// logger.info("判决结果:" + arch.getApproval());
				// arch.setSuitType(TextName.getRunningClass(html));
				arch.setSuitType(ExtractText.getSuitType(html));
				// logger.info("文书类型:" + arch.getSuitType());
				
				if (!"".equals(ExtractText.getCaseNum(html))|| ExtractText.getCaseNum(html) != null) {
					arch.setCaseNum(ExtractText.getCaseNum(html));
				}
				if ("".equals(ExtractText.getCaseNum(html))|| ExtractText.getCaseNum(html) == null) {
					arch.setCaseNum(TextName.getSentenceNo3(html));
				} 
				logger.info("案号:" + arch.getCaseNum());
				// arch.setTitle(TextName.getTitle2(fi.getPath()));
				arch.setTitle(ExtractText.getTitle(html));
				// logger.info("标题:" + arch.getTitle());
				// logger.info("<<------------------------------------------------------>>");
				arch.setUuid(fi.getName().substring(0,fi.getName().lastIndexOf(".")));
				listarchs.add(arch);
				if (listarchs.size() >= 2000) {
					boolean result = ExtractText.updateJsonData(listarchs,
							bucket);
					if (!result) {
						logger.info(fi.getPath() + ":更新失败");
					}
					count += listarchs.size();
					listarchs = null;
					listarchs = new ArrayList<ArchivesVO>();
				}

			} else if (fi.isDirectory()) {
				logger.info(fi.getName());
				show(fi, bucket);
			} else {
				continue;
			}
		}
		if (null != listarchs && listarchs.size() > 0) {
			boolean result = ExtractText.updateJsonData(listarchs, bucket);
			if (!result) {
				logger.info(":更新失败");
			}
			count += listarchs.size();
			listarchs = null;
			listarchs = new ArrayList<ArchivesVO>();
			arch = null;
		}
	}

	// 提取所有内容
	public static String getAll(String value) {
		int index = value.lastIndexOf("书记员") + 10;
		value = value.substring(0, index);
		return value;
	}

	// 提取标题
	public static String getTitle(String value) {
		try {
			int index = value.indexOf("书");
			if (index == -1) {
				return null;
			}
			return value.substring(0, index + 1);
		} catch (Exception e) {
			logger.error(e.getMessage() + ":获取标题失败");
		}
		return null;
	}

	// 提取文书类型
	public static String getSuitType(String value) {
		int index;
		for (String val : BOOKCLASS) {
			index = value.indexOf(val);
			if (index >= 0) {
				return val;
			}
		}
		return null;
	}

	// 提取法院名称
	public static String getCourtName(String value) {
		try {
			int idnex = value.indexOf("院");
			if (idnex == -1) {
				return null;
			}
			return value.substring(0, idnex + 1);
		} catch (Exception e) {
			logger.error("提取法院名称出错:" + e.getMessage());
		}
		return null;
	}

		//判断字符是否非数字
			public static boolean isDigit(String strNum) {  
			    Pattern pattern = Pattern.compile("[0-9]{1,}");  
			    Matcher matcher = pattern.matcher((CharSequence) strNum);  
			    return matcher.matches();  
			}
			 //获取案号
		    public static String getSplitCaseNum(String value){
			   	 try{
			   		 for(String val : CAUSENUM){
			   			int firstIndex= value.indexOf(val);
			   			if(firstIndex == -1){continue;}
			   			value = value.substring(firstIndex);
			   			int secondIndex = value.indexOf("号");
			   			if(secondIndex <=0){//当案号中没有“号”字的时候
			   				String spl = null;
			   				String value2 = null;
			   				secondIndex = value.indexOf("第");
			   				if(secondIndex <= 0 ){
			   					secondIndex = value.indexOf("字");
			   				}
			   				value2 = value.substring(0, secondIndex+1);
			   				value = value.substring(secondIndex+1);
			   				String[] split = value.split("");
			   				for(int i = 1 ; i < split.length; i++){
			   					spl = split[i];
			   					boolean result = ExtractText.isDigit(spl);
			   					if(!result){
			   						break;
			   					}
			   					value2 = value2+spl; //取"第"字后面的数字，一个个添加进去  
			   			}
			   				return value2;
			   			}
			   		value = value.substring(0, secondIndex+1);
			   		return value;
			   		 }
			   	} catch (Exception e) {
			   		logger.error("提取案号出错"+e.getMessage());
			   	}
			   	return null;
			   	}
		    public static String getSplitCaseNum2(String value){
				try {
					for(String val : BOOKCLASS){
						int firstIndex= value.lastIndexOf(val);
						if(firstIndex == -1){continue;}
						firstIndex = firstIndex + val.length();
						value = value.substring(firstIndex);
						int secondIndex = value.indexOf("号");
						if(secondIndex <=0){//当案号中没有“号”字的时候
							String value2 = null;
							String spl = null;
							secondIndex = value.indexOf("第");
							if(secondIndex <= 0 ){
								secondIndex = value.indexOf("字");
							}
							value2 = value.substring(0, secondIndex+1);
							value = value.substring(secondIndex+1);
							String[] split = value.split("");
						
							for(int i = 1 ; i < split.length; i++){
								spl = split[i];
								boolean result = ExtractText.isDigit(spl);
								if(!result){
									break;
									}
								value2 = value2+spl; //取"第"字后面的数字，一个个添加进去  
							}
							return value2;
						}
						value = value.substring(0, secondIndex+1);
						for(String val3 : CAUSENUM3){
							int lastIndex = value.indexOf(val3);
							if(lastIndex == -1){continue;}
							return value;
						}
					}
				} catch (Exception e) {
					logger.error("提取案号出错"+e.getMessage());
				}
				return null;
			}
		    
		    public static String getSplitCaseNum3(String value){

				for(String val : CAUSENUM2){
					int firstIndex= value.lastIndexOf(val);
					if(firstIndex == -1){continue;}
					firstIndex = firstIndex + val.length();
					value = value.substring(firstIndex);
					int secondIndex = value.indexOf("号");
					if(secondIndex <=0){//当案号中没有“号”字的时候
						String value2 = null;
						String spl = null;
						secondIndex = value.indexOf("第");
						if(secondIndex <= 0 ){
							secondIndex = value.indexOf("字");
						}
						value2 = value.substring(0, secondIndex+1);
						value = value.substring(secondIndex+1);
						String[] split = value.split("");
					
						for(int i = 1 ; i < split.length; i++){
							spl = split[i];
							boolean result = ExtractText.isDigit(spl);
							if(!result){
								break;
								}
							value2 = value2+spl; //取"第"字后面的数字，一个个添加进去  
						}
						value2 = replaceCaseNum1(value);
						return value2;
					}
					value = value.substring(0, secondIndex+1);
					value = replaceCaseNum1(value);
					return value;
				}
			
		    	return null;
		    }
		    //去掉案号中多余的字
		    public static String replaceCaseNum1(String value){
		    	int index1 = value.indexOf("共印");
				int index2 = value.indexOf("份");
				if(index1 > 0){
				String value3 = value.substring(index1, index2+1);
				value = value.replace(value3, "");
				}
		    	return value;
		    }
		   //按符號切割字符串
		    public static String getCaseNum(String value){
		    	String[] split = value.split("。");
		    	for(int i = 0 ; i < split.length; i++){
		    		value = getSplitCaseNum(split[i]);
		    		if(value != null && !"".equals(value)){
		    			return value;
		    		}
		    		value = getSplitCaseNum2(split[i]);
		    		if(value != null && !"".equals(value)){
		    			return value;
		    		}
		    		value = getSplitCaseNum3(split[i]);
		    		if(value != null && !"".equals(value)){
		    			return value;
		    		}
		    	}
		    	return null;
		    }

	// 取审结日期
	public static String getConcludeDate(String date) {
		String[] data = { "二〇", "一九", "二○", "二０", "二0", "二O", "二0", "二Ｏ", "二�",
				"20", "19" };
		int[] splt = { 9, 10, 11, 12 };
		String value;
		String[] datas;
		boolean result = false;
		try {
			for (int index = 0; index < data.length; index++) {
				if (date.lastIndexOf(data[index]) < 0) {
					continue;
				}
				value = date.substring(date.lastIndexOf(data[index]));
				for (int index2 = 0; index2 < splt.length; index2++) {
					datas = value.split("");
					String da = datas[splt[index2]];
					if ("日".equals(da)) {
						value = value.substring(0, splt[index2]);
						result = true;
						;
						break;
					}
				}
				if (result)
					return value == null ? null : value.replaceAll("�", "0");
			}
		} catch (Exception e) {
			logger.error("取审结日期出错:" + e.getMessage());
		} finally {
			datas = null;
			splt = null;
			data = null;
		}
		return null;
	}

	/**
	 * 裁判文书 抓取word，HTML修改court桶
	 */
	public static boolean updateJsonData(List<ArchivesVO> list, Bucket bucket)
			throws Exception {

		if (null == list || list.size() <= 0) {
			return false;
		}
		JsonDocument doc = null;
		JsonObject obj2 = null;
		com.google.gson.JsonObject json = null;
		Gson gson = new Gson();
		ArchivesVO archs = null;
		try {
			for (ArchivesVO arch : list) {
				// 查询数据
				doc = JsonDocument.create(arch.getUuid());
				obj2 = bucket.get(doc) == null ? null : bucket.get(doc).content();
				if (obj2 == null) {
					logger.info("匹配不到UUID:" + arch.getUuid());
					continue;
				}
				archs = new ArchivesVO();
				json = gson.fromJson(obj2.toString(),
						com.google.gson.JsonObject.class);
				archs = gson.fromJson(json, ArchivesVO.class);

				if (null != obj2.get("title") && !"".equals(obj2.get("title"))) {
					archs.setTitle(obj2.get("title").toString());// 标题
				}
				if (null != arch.getTitle() && !"".equals(arch.getTitle())) {
					archs.setTitle(arch.getTitle());
				}
				
				if (null != obj2.get("suitType") && !"".equals(obj2.get("suitType"))) {
					archs.setSuitType(obj2.get("suitType").toString());// 诉讼类型
				}
				if (null != arch.getSuitType() && !"".equals(arch.getSuitType())) {
					archs.setSuitType(arch.getSuitType());
				}
				
				if (null != obj2.get("caseNum") && !"".equals(obj2.get("caseNum"))) {
					archs.setCaseNum(obj2.get("caseNum").toString());// 案号
				}
				if (null != arch.getCaseNum() && !"".equals(arch.getCaseNum())) {
					archs.setCaseNum(arch.getCaseNum());
				}
				
				if (null != arch.getCourtName() && !"".equals(arch.getCourtName())) {
					archs.setCourtName(arch.getCourtName());
				}
				if (null != obj2.get("courtName") && !"".equals(obj2.get("courtName"))) {
					archs.setCourtName(obj2.get("courtName").toString());// 法院名
				}
				
				if (null != arch.getPlaintiff() && !"".equals(arch.getPlaintiff())) {
					archs.setPlaintiff(arch.getPlaintiff());
				}
				if (null != obj2.get("plaintiff") && !"".equals(obj2.get("plaintiff"))) {
					archs.setPlaintiff(obj2.get("plaintiff").toString());// 原告
				}
				
				if (null != arch.getDefendant() && !"".equals(arch.getDefendant())) {
					archs.setDefendant(arch.getDefendant());
				}
				if (null != obj2.get("defendant") && !"".equals(obj2.get("defendant"))) {
					archs.setDefendant(obj2.get("defendant").toString());// 被告
				}
				
				if (null != arch.getApproval() && !"".equals(arch.getApproval())) {
					archs.setApproval(arch.getApproval());
				}
				if (null != obj2.get("approval") && !"".equals(obj2.get("approval"))) {
					archs.setApproval(obj2.get("approval").toString());// 审批结果
				}
				
				if (null != arch.getApprovalDate() && !"".equals(arch.getApprovalDate())) {
					archs.setApprovalDate(arch.getApprovalDate());
				}
				if (null != obj2.get("approvalDate") && !"".equals(obj2.get("approvalDate"))) {
					archs.setApprovalDate(obj2.get("approvalDate").toString());// 审结日期
				}
				
				if (null != arch.getCaseCause() && !"".equals(arch.getCaseCause())) {
					archs.setCaseCause(arch.getCaseCause());
				}
				if (null != obj2.get("caseCause") && !"".equals(obj2.get("caseCause"))) {
					archs.setCaseCause(obj2.get("caseCause").toString());// 案由
				}
				
				if (null != arch.getSummary() && !"".equals(arch.getSummary())) {
					archs.setSummary(arch.getSummary());
				}
				if (null != obj2.get("summary") && !"".equals(obj2.get("summary"))) {
					archs.setSummary(obj2.get("summary").toString());// 摘要
				}
				
				if (null != obj2.get("detailLink") && !"".equals(obj2.get("detailLink"))) {
					archs.setDetailLink(obj2.get("detailLink").toString());// url
				}
				if (null != obj2.get("catalog") && !"".equals(obj2.get("catalog"))) {
					archs.setCatalog(obj2.get("catalog").toString());// 分类
				}
				if (null != obj2.get("publishDate") && !"".equals(obj2.get("publishDate"))) {
					archs.setPublishDate(getReplaceAllDate(obj2.get("publishDate").toString()));// 发布日期
				}
				if (null != obj2.get("province") && !"".equals(obj2.get("province"))) {
					archs.setProvince(obj2.get("province").toString());// 省
				}
				if (null != obj2.get("city") && !"".equals(obj2.get("city"))) {
					archs.setCity(obj2.get("city").toString());// 市
				}
				if (null != obj2.get("area") && !"".equals(obj2.get("area"))) {
					archs.setArea(obj2.get("area").toString());// 县
				}
				if (null != obj2.get("collectDate") && !"".equals(obj2.get("collectDate"))) {
					archs.setCollectDate(getReplaceAllDate(obj2.get("collectDate").toString()));// 采集时间
				}
				if (null != obj2.get("suitDate")&& !"".equals(obj2.get("suitDate"))) {
					archs.setSuitDate(obj2.get("suitDate").toString());// 起诉日期
				}
				String jsonss = gson.toJson(archs);
				doc = JsonDocument.create(arch.getUuid(),
						JsonObject.fromJson(jsonss));
				bucket.upsert(doc);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		} finally {
			gson = null;
			json = null;
			archs = null;
			obj2 = null;
			doc = null;
		}
		return true;
	}

	// 去掉无用字符
	public static String getReplaceAll(String value) {
		StringBuffer sb = null;
		if (value != null && !"".equals(value)) {
			// sb=new StringBuffer();
			value = value.replaceAll(",", "，");
			value = value.replaceAll("�", "O");
			value = value.replaceAll("[×,X,Ｘ,x,╳,＊,\\*]", "某");
			value = value.replaceAll(
							"[\n,\t,\r,\\s,&nbsp; ,：,“,”,・ ,:,<,/>,</,>,a-z,A-Z,-,+,=,},{,.,#,\",',-,%,^,*]","");
			value = getSpecialStringALL(value);
			value = value.trim();
			sb = new StringBuffer();
			sb.append(value);
		}
		return sb == null ? null : sb.toString();
	}

	// 去掉特殊字符
	public static String getSpecialStringALL(String value) {
		if (null == value || "".equals(value)) {
			return null;
		}
		char[] chs = value.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (char c : chs) {
			if (((int) c) != 12288 && ((int) c) != 160) {
				sb.append(String.valueOf(c));
			}
		}
		return sb.toString();
	}

	/**
	 * 统一日期格式
	 * @param value
	 * @return
	 */
	 public static String getReplaceAllDate(String value){
		 StringBuffer sb=null;				  
		   if(value!=null&&!"".equals(value)){
			    value=value.replaceAll("[（,）,(,),【,】,{,},<,>]","");
			    value=value.replaceAll("[-,-,/,\",年,月]","-");
			    value=value.replaceAll("[:,：]", ":");
			    value = value.replace("]", "");
				value = value.replace("[", "");
			    value=value.trim();
				sb=new StringBuffer();
				sb.append(value);
		   }
		 return sb==null?null:sb.toString();
	 }
}
