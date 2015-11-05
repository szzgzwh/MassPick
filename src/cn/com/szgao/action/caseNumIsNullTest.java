package cn.com.szgao.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.util.CommonConstant;

public class caseNumIsNullTest {
	private static Logger logger = LogManager.getLogger(caseNumIsNullTest.class
			.getName());
	public static String[] BOOKCLASS = { "民事调解书", "民事裁定书", "民事判决书", "民事决定书",
			"刑事判决书", "刑事裁定书", "刑事决定书", "行政判决书", "行政决定书", "行政裁定书", "执行裁定书",
			"执行判决书", "执行决定书", "国家赔偿裁定书", "国家赔偿判决书", "国家赔偿决定书", "驳回申诉通知书",
			"决定书", "通知书" };// 文书类型
	public static String[] CAUSENUM = { "(2０","（2０","〔2０","[2０","【2０","(20", "（20", "〔20", "[20", "【20","(19", "（19", "〔19", "[19", "【19" };
	public static String[] CAUSENUM2={"０","0","1","2","3","4","5","6","7","8","9"};
	static long count = 0;
	static long ERRORSUM = 0;
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File filepor = new File(
				"E:\\Company_File\\log4j-0915\\Java1\\batchImport.log");
		if (filepor.exists()) {
			filepor.delete();
		}
		filepor = null;
		PropertyConfigurator
				.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		String filePath = "E:\\Company_File\\log4j-0914\\Java2\\batchImport.txt";
		Bucket bucket = CommonConstant.connectionCouchBase();
		try {
			readTxtFile(filePath, bucket);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info(count + ":数量");
			logger.info("所有文件总耗时"
					+ (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
		}

	}

	public static void readTxtFile(String filePath, Bucket bucket) {
		try {
			String encoding = "utf-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				List<String> list = new ArrayList<String>();
				while ((lineTxt = bufferedReader.readLine()) != null) {
					lineTxt = caseNumIsNullTest.split(lineTxt);
//					logger.info(lineTxt);
					if (lineTxt == null) {
						continue;
					}
					list.add(lineTxt);
				}
				show(list, bucket);
				read.close();
			} else {
				logger.info("找不到指定的文件");
			}
		} catch (Exception e) {
			logger.info("读取文件内容出错");
			e.printStackTrace();
		}
	}

	/**
	 * 获取文件中的路径
	 * 
	 * @param value
	 * @return
	 */
	public static String split(String value) {
		if (value == null && "".equals(value)) {
			return null;
		}
		int index = "URL路径：（".length();
		int index1 = value.indexOf("URL路径：（") + index;
		if (index1 <= 7) {
			return null;
		}
		value = value.substring(index1, value.length() - 2);
		return value;
	}

	//
	public static void show(List<String> lis, Bucket bucket) {
		try {
			String html = null;
			Map<String, List<String>> list = null;
			ArchivesVO arch = null;
			List<ArchivesVO> listarchs = null;
			listarchs = new ArrayList<ArchivesVO>();
			for (String list1 : lis) {
				File file = new File(list1);
				arch = new ArchivesVO();
				logger.info(file.toString());
				html = URLText.getText("file:///", file.toString());// 根据路径抓取HTML内容
				if (html == null || "".equals(html)) {
					logger.info("内容为空！");
					continue;
				}
				// logger.info("所有内容：" + html);
				html = ExtractthepeopleText.getReplaceAll(html);
				list = ExtractthepeopleText.getPersonName(html);
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
				 arch.setSuitType(caseNumIsNullTest.getSuitType(html));
				// logger.info("文书类型:" + arch.getSuitType());
				if (!"".equals(TextName.getSentenceNo3(html))|| TextName.getSentenceNo3(html) != null) {
					arch.setCaseNum(TextName.getSentenceNo3(html));
				} 
				if ("".equals(TextName.getSentenceNo3(html))|| TextName.getSentenceNo3(html) == null) {
					arch.setCaseNum(caseNumIsNullTest.getCaseNum(html));
				}
				logger.info("案号:" + arch.getCaseNum());
				arch.setTitle(TextName.getTitle2(file.toString()));
				// logger.info("标题:" + arch.getTitle());
				arch.setUuid(file.getName().substring(0,
						file.getName().lastIndexOf(".")));
				// logger.info("UUID为：" + arch.getUuid());
				listarchs.add(arch);
				arch = null;
				if (listarchs.size() >= 2000) {
					boolean result = caseNumIsNullTest.updateJsonData(
							listarchs, bucket);
					if (!result) {
						logger.info(file.getPath() + ":更新失败");

					}
					count += listarchs.size();
					listarchs = null;
					listarchs = new ArrayList<ArchivesVO>();
				}
			}
			if (listarchs.size() >= 0 && listarchs != null) {
				boolean result = caseNumIsNullTest.updateJsonData(listarchs,
						bucket);
				if (!result) {
					logger.info("更新失败!");

				}
				count += listarchs.size();
				listarchs = null;
				listarchs = new ArrayList<ArchivesVO>();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
   				secondIndex = value.indexOf("第");
   				String value2 = value.substring(0, secondIndex+1);
   				value = value.substring(secondIndex+1);
   				String[] split = value.split("");
   				String spl = null;
   				for(int i = 1 ; i < split.length; i++){
   					spl = split[i];
   					boolean result = caseNumIsNullTest.isDigit(spl);
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


	public static String getCaseNum(String value) {
		String[] split = value.split("。");
		for (int i = 0; i < split.length; i++) {
			value = getSplitCaseNum(split[i]);
			if (value != null && !"".equals(value)) {
				return value;
			}
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

	// 去掉无用字符
	public static String getReplaceAll(String value) {
		StringBuffer sb = null;
		if (value != null && !"".equals(value)) {
			value = value.replaceAll(",", "，");
			value = value.replaceAll("�", "O");
			value = value.replaceAll("[×,X,Ｘ,x,╳,＊,\\*]", "某");
			value = value
					.replaceAll(
							"[\n,\t,\r,\\s,&nbsp; ,：,“,”,・ ,:,<,/>,</,>,a-z,A-Z,-,+,=,},{,.,#,\",',-,%,^,*]",
							"");
			value = getSpecialStringALL(value);
			value = value.trim();
			sb = new StringBuffer();
			sb.append(value);
		}
		return sb == null ? "" : sb.toString();
	}

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
	 * 
	 * @param value
	 * @return
	 */
	public static String getReplaceAllDate(String value) {
		StringBuffer sb = null;
		if (value != null && !"".equals(value)) {
			value = value.replaceAll("[日,（,）,(,),【,】,{,},<,>]", "");
			value = value.replaceAll("[-,-,/,\",年,月]", "-");
			value = value.replaceAll("[:,：]", ":");
			value = value.replace("]", "");
			value = value.replace("[", "");
			value = value.trim();
			sb = new StringBuffer();
			sb.append(value);
		}
		return sb == null ? null : sb.toString();
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

				if (null != arch.getTitle() && !"".equals(arch.getTitle())) {
					archs.setTitle(arch.getTitle());
				}
				if (null != obj2.get("title") && !"".equals(obj2.get("title"))) {
					archs.setTitle(obj2.get("title").toString());// 标题
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
}
