package cn.com.szgao.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.SessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.com.szgao.dao.impl.CollectionDataDao;
import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.service.impl.CollectDataService;
import cn.com.szgao.util.CommonConstant;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;

public class ExtractionHtml {
	public static String[] DATESTATUS = { "提交时间", "提交日期", "发布时间", "发布日期","关注时间",
		"编辑时间", "编辑日期", "发表时间", "录入时间", "更新时间","作者点击","点击时间", "【关闭窗口】","大中小","大小","字体", "点击率", "浏览",
		"点击数", "∣法律社区","发表于", "阅读", "点击", "日期", "作者", "时间", "今天是" };
	public static String[] DEFENDANT = { "被上诉人", "被执行人", "被申诉人", "被申请人",
			"被申请执行人", "原审被告人", "原审原告", "罪犯", "被告", "赔偿义务机关", "一审被告", "二审被上诉人"};
	public static String[] PLAINTIFF = { "第三人", "诉讼代理人", "辩护人", "上诉人", "申诉人",
			"申请执行人", "申请人", "执行人", "原审被告", "赔偿请求人", "原公诉机关", "公诉机关", "执行机构",
			"原告", "复议机关", "申请复议人", "一审原告", "委托代理人", "法定代表人", "起诉人", "移送执行机构",
			"二审上诉人", "原审第三人", "负责人", "抗诉机关", "申请再审人", "委托代理", "四被上诉人委托代理人",
			"两上诉人的委托代理人" };
	public static String[] KEYWORDKE = { "申请再审人", "被上诉人", "二审被上诉人", "原审被告人",
			"原审第三人", "二审上诉人", "一审被告", "一审原告", "被申请人", "赔偿请求人", "被告人", "原告",
			"执行机构", "被申请人", "申请执行人", "申请人", "辩护人", "被申请执行人", "赔偿请求人", "赔偿义务机关",
			"原公诉机关", "抗诉机关", "公诉机关", "复议机关", "委托代理人", "委托代理", "特别授权代理",
			"四被上诉人委托代理人", "两上诉人的委托代理人", "移送执行机构", "诉讼代理人", "法定代表人", "申请复议人",
			"被上诉人", "被申诉人", "被执行人", "反诉被告", "反诉原告", "原审被告", "原审原告", "执行人",
			"负责人", "上诉人", "起诉人", "申诉人", "被告人", "原告人", "被告", "原告", "罪犯", "第三人" };
	public static String[] THEVERDICT = { "裁定如下", "决定如下", "判决如下", "协议如下","处理意见如下",
			"调解协议", "如下协议", "决定" };
	public static String[] CAUSE = { "驳回申诉通知", "赔偿决定书","提起公诉","提出公诉","提起上诉","提出上诉","提起诉讼","提出诉讼","提起行政诉讼","提出行政诉讼","争议一案", "纠纷一案", "通告一案",
		"违法一案", "执行一案", "赔偿一案", "劫罪一案", "确认一案", "涉嫌",  "危险驾驶","诈骗", "盗窃", "死亡", "强奸", "聚众斗殴", "寻衅滋事", "贩卖毒品", "运输毒品", "故意伤害",
		"涉嫌诽谤", "抢劫", "绑架", "勒索", "杀人", "纠纷", "非法拘禁", "运输毒品", "破坏电力设备","一案", "违法", "非法", "犯罪" };
	
	public static String[] BOOKCLASS = {"准许强制执行裁定书","民事附带刑事判决书","强制医疗决定书","指定管辖决定书","非诉行政执行裁定书","行政审查裁定书","不予受理案件决定书","暂予监外执行决定书",
		"民事调解书判决书","强制医疗决定书","准予撤诉决定书","刑事附带民事判决书","刑事附带民事调解书","案件执行结束通知书","减刑假释文书","口头撤诉裁定笔录","行政文书","商事文书","普通民事文书","普通刑事文书",
		"民事调解书", "民事裁定书", "民事判决书", "民事决定书","刑事判决书", "刑事裁定书", "刑事决定书", "行政判决书", "行政决定书", "行政裁定书", "执行裁定书","普通执行文书","普通行政文书",
			"执行判决书", "执行决定书", "国家赔偿裁定书", "国家赔偿判决书", "国家赔偿决定书", "驳回申诉通知书","调解书","决定书", "通知书","判决书","裁定书","民事","刑事","行政","执行"};// 文书类型
	
	public static String[] CAUSENUM = { "（２０", "(2０", "（2０", "〔2０", "[2０",
			"【2０", "(20", "（20", "〔20", "［2", "[20", "【20", "(19", "（19",
			"〔19", "[19", "【19" };
	public static String[] CAUSENUM2 = { "判决书字号", "字号" };
	public static String[] CAUSENUM3 = { "第", "字" };
	static long count = 0;// 总数量
	static long ERRORSUM = 0;// 出错数据
	static Map<String, String> MAPS = new HashMap<String, String>();
	static {
		MAPS.put("html", "html");
		MAPS.put("htm", "htm");
		MAPS.put("txt", "txt");
		MAPS.put("doc", "doc");
	}
	public static String[] charset = {"utf-8","gbk","gb2312","gb18030","big5"};
	public static  String[] ERCOEDING={"й","෨","Ժ","ۼ","ҩ","ල","ɷ","ص","δ","ġ","Ϊ","ط","Ϣ","ȡ","Ӫ","ã","","Դ","ڲ","Ѱ","�"};
	private static Logger logger = LogManager.getLogger( ExtractionHtml.class.getName());
	static ApplicationContext application = new ClassPathXmlApplicationContext( "classpath:\\cn\\com\\szgao\\config\\applicationContext.xml");
	static SessionFactory sessionFactory = (SessionFactory) application.getBean("sessionFactory");

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		File filepor = new File("E:\\Company_File\\log4j-1104\\Java1\\batchImport.log");
		if (filepor.exists()) {
			filepor.delete();// 删除日志文件
		}
		filepor = null;
		PropertyConfigurator.configure("F:\\work\\WorkSpace_Eclipse\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		long da = System.currentTimeMillis();
		File file = new File("F:/DataSheet/DetailedPage/裁判文书/DistrictCourtHtm/HTML2/湖南省");
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
		logger.info("所有文件总耗时" + (((System.currentTimeMillis() - da) / 1000) / 60) + "分钟");
	}
	//
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
		String variable = null;
		String html = null;
		Map<String, List<String>> list = null;
		ArchivesVO arch = null;
		List<ArchivesVO> listarchs = null;
		Document doc;
		listarchs = new ArrayList<ArchivesVO>();
		int i =0;
		if (file.isFile()) {
			arch = new ArchivesVO();
			String suffix = file.getName();
			suffix = suffix.substring(suffix.indexOf(".") + 1, suffix.length());
			suffix = MAPS.get(suffix);
			if (null == suffix) {
				return;
			}
			logger.info("网址:" + file.getPath());
			for(String val : charset){//匹配不同编码格式
			doc = Jsoup.parse(file, val);
			html = doc.body().text();//取页面body标签中所有内容
			logger.info("-----------------------所有内容："+html);
			boolean Garbled = getErrorCode(html);//判断编码是否错误
			if (Garbled == false) {
				logger.info(val + "编码错误！！！");
				i++;
				if(i==5){
					html = null;
				}
				continue;
			}
			i=0;
			variable = getReplaceAll(doc.title());
			if(variable != null && !"".equals(variable)){
				arch.setTitle(variable.trim()); // 标题 √
			}
			logger.info("标题:" + arch.getTitle());
			break;
			}
			if(html == null || "".equals(html)){
				logger.info("内容为空的HTML页面："+file.getPath());
				}
			html =  getReplaceAll(html).trim();
			logger.info("所有内容：" + html);
			list = ExtractthepeopleText.getPersonName(html);
			arch.setPlaintiff( getKeyName(list, 1)); // 原告相关人 √
			logger.info("<<------------------------------------------------------>>");
			logger.info("原告相关人:" + arch.getPlaintiff());
			arch.setDefendant( getKeyName(list, 2)); // 被告相关人 √
			logger.info("被告相关人:" + arch.getDefendant());
			variable =  getAtherthe(html);
			if(variable != null && !"".equals(variable)){
				arch.setCourtName(variable); // 法院 √
			}
			if(variable == null || "".equals(variable)){
			arch.setCourtName( getCourtName(html)); // 法院 √
			}
			logger.info("法院:" + arch.getCourtName());
			arch.setCaseCause( StringCause(html)); // 案由 √
			logger.info("案由:" + arch.getCaseCause());
			arch.setApprovalDate( getConcludeDate(html)); // 审结日期 √
			logger.info("审结日期:" + arch.getApprovalDate());
			arch.setApproval( getTheVerdictData(html)); // 判决结果 √
			logger.info("判决结果:" + arch.getApproval());
			arch.setCatalog( getCatalog(html)); // 文书类型 √
			logger.info("文书类型:" + arch.getCatalog());
			variable = getCaseNum(html);
			if (!"".equals(variable) && variable != null) {
				arch.setCaseNum(variable); // 案号 √
			}
			if ("".equals(variable) || variable == null) {
				arch.setCaseNum(getSentenceNo3(html)); // 案号 √
			}
			logger.info("案号:" + arch.getCaseNum());
			logger.info("<<------------------------------------------------------>>");
			arch.setUuid(file.getName().substring(0, file.getName().lastIndexOf(".")));
			listarchs.add(arch);
			boolean result =  updateJsonData(listarchs, bucket);
			if (!result) {
				logger.info(file.getPath() + ":更新失败");
			}
			count += listarchs.size();
			logger.info("<<------------------------count------------------------------>>"+ count);
			listarchs = null;
			return;
		}
		File[] files = file.listFiles();
		for (File fi : files) {
			if (fi.isFile()) {
				arch = new ArchivesVO();
				String suffix = fi.getName();
				suffix = suffix.substring(suffix.indexOf(".") + 1,
						suffix.length());
				suffix = MAPS.get(suffix);
				if (null == suffix) {
					return;
				}
				logger.info("网址:" + fi.getPath());
				for(String val : charset){	//匹配不同编码格式
					doc = Jsoup.parse(fi, val);
					html = doc.body().text();
//					html = doc.text();
//					html = getDataAll(html);
					boolean Garbled = getErrorCode(html);//判断编码是否错误
					if (Garbled == false) {
						i++;
							if(i==5){ html = null; }	//判断编码格式都不匹配的时候赋予空值
						continue;
						}
					i=0;
					variable = getReplaceAll(doc.title());
					if(variable != null && !"".equals(variable)){	//防止title标签为空的情况
					arch.setTitle(variable.trim()); // 标题 √
					}
					break;
				}
				if(html == null || "".equals(html)){
					logger.info("内容为空的HTML页面："+fi.getPath());
					continue;
					}
				html =  getReplaceAll(html).trim();//所有内容去掉特殊字符√
				
				list = ExtractthepeopleText.getPersonName(html);
				
				arch.setPlaintiff( getKeyName(list, 1)); // 原告相关人√
				logger.info("<<------------------------------------------------------>>");
				arch.setDefendant( getKeyName(list, 2)); // 被告相关人√
				
				arch.setCatalog( getCatalog(html)); // 文书类型 √
				
				variable =  getAtherthe(html);
				if(variable != null && !"".equals(variable)){
					arch.setCourtName(variable); // 法院 √
				}
				if(variable == null || "".equals(variable)){
				arch.setCourtName( getCourtName(html)); // 法院 √
				}
				
				arch.setCaseCause( StringCause(html)); // 案由 √
				
				arch.setApprovalDate( getConcludeDate(html)); // 审结日期√
				
				arch.setApproval( getTheVerdictData(html)); // 判决结果√
				
				variable = getCaseNum(html);
				if (!"".equals(variable) && variable != null) {
					arch.setCaseNum(variable); // 案号 √
				}
				if ("".equals(variable)|| variable == null) {
					arch.setCaseNum(getSentenceNo3(html)); // 案号 √
				}
				arch.setUuid(fi.getName().substring(0,fi.getName().lastIndexOf(".")));
				showData(arch); // 打印所有截取字段
				listarchs.add(arch);
				if (listarchs.size() >= 2000) {
					boolean result =  updateJsonData(listarchs,bucket);
					if (!result) {
						logger.info(fi.getPath() + ":更新失败1");
					}
					count += listarchs.size();
					logger.info("<<------------------------count------------------------------>>" + count);
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
			boolean result =  updateJsonData(listarchs, bucket);
			if (!result) {
				logger.info(":更新失败2");
			}
			count += listarchs.size();
			listarchs = null;
			arch = null;
			return;
		}
	}
	
	public static void showData(ArchivesVO arch) {
		logger.info("UUID:" + arch.getUuid());
		logger.info("原告相关人:" + arch.getPlaintiff());
		logger.info("被告相关人:" + arch.getDefendant());
		logger.info("法院:" + arch.getCourtName());
		logger.info("审结日期:" + arch.getApprovalDate());
		logger.info("文书类型:" + arch.getCatalog());
		logger.info("案号:" + arch.getCaseNum());
		logger.info("标题:" + arch.getTitle());
		logger.info("案由:" + arch.getCaseCause());
		logger.info("判决结果:" + arch.getApproval());
	}

	/**
	 * 裁判文书 抓取word，HTML修改court桶
	 */
	public static boolean updateJsonData(List<ArchivesVO> list, Bucket bucket)
			throws Exception {
		//
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
				doc = JsonDocument.create(arch.getUuid()); // 获取ID
				obj2 = bucket.get(doc) == null ? null : bucket.get(doc)
						.content();
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

				if (null != obj2.get("caseNum")
						&& !"".equals(obj2.get("caseNum"))) {
					archs.setCaseNum(obj2.get("caseNum").toString());// 案号
				}
				if (null != arch.getCaseNum() && !"".equals(arch.getCaseNum())) {
					archs.setCaseNum(arch.getCaseNum());
				}

				if (null != obj2.get("courtName") && !"".equals(obj2.get("courtName"))) {
					archs.setCourtName(obj2.get("courtName").toString());// 法院名
				}
				if (null != arch.getCourtName() && !"".equals(arch.getCourtName())) {
					archs.setCourtName(arch.getCourtName());
				}
				if (null != obj2.get("catalog") && !"".equals(obj2.get("catalog"))) {
					archs.setCatalog(obj2.get("catalog").toString());// 分类
				}
				if (null != arch.getCatalog() && !"".equals(arch.getCatalog())) {
					archs.setCatalog(arch.getCatalog());
				}
				if (null != obj2.get("plaintiff") && !"".equals(obj2.get("plaintiff"))) {
					archs.setPlaintiff(obj2.get("plaintiff").toString());// 原告
				}
				if (null != arch.getPlaintiff() && !"".equals(arch.getPlaintiff())) {
					archs.setPlaintiff(arch.getPlaintiff());
				}

				if (null != obj2.get("defendant") && !"".equals(obj2.get("defendant"))) {
					archs.setDefendant(obj2.get("defendant").toString());// 被告
				}
				if (null != arch.getDefendant() && !"".equals(arch.getDefendant())) {
					archs.setDefendant(arch.getDefendant());
				}

				if (null != obj2.get("approval") && !"".equals(obj2.get("approval"))) {
					archs.setApproval(obj2.get("approval").toString());// 审批结果
				}
				if (null != arch.getApproval() && !"".equals(arch.getApproval())) {
					archs.setApproval(arch.getApproval());
				}

				if (null != obj2.get("approvalDate") && !"".equals(obj2.get("approvalDate"))) {
					archs.setApprovalDate(obj2.get("approvalDate").toString());// 审结日期
				}
				if (null != arch.getApprovalDate() && !"".equals(arch.getApprovalDate())) {
					archs.setApprovalDate(arch.getApprovalDate());
				}
				
				if (null != obj2.get("caseCause") && !"".equals(obj2.get("caseCause"))) {
					archs.setCaseCause(obj2.get("caseCause").toString());// 案由
				}
				if (null != arch.getCaseCause() && !"".equals(arch.getCaseCause())) {
					archs.setCaseCause(arch.getCaseCause());
				}

				if (null != arch.getSummary() && !"".equals(arch.getSummary())) {
					archs.setSummary(arch.getSummary());
				}
				if (null != obj2.get("summary")
						&& !"".equals(obj2.get("summary"))) {
					archs.setSummary(obj2.get("summary").toString());// 摘要
				}

				if (null != obj2.get("detailLink") && !"".equals(obj2.get("detailLink"))) {
					archs.setDetailLink(obj2.get("detailLink").toString());// url
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
					archs.setCollectDate(getReplaceAllDate(obj2.get(
							"collectDate").toString()));// 采集时间
				}
				if (null != obj2.get("suitDate") && !"".equals(obj2.get("suitDate"))) {
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

	//判断是否存在乱码
    public static boolean getErrorCode(String value){
    	if(value == null || "".equals(value)){return false;}
    	for(String val:ERCOEDING){
    		int index = value.lastIndexOf(val);
    		if(index <= 0){
    			continue;
    		}
    		return false;
    	}
    	return true;
    }
	// 提取文书类型
	public static String getCatalog(String value) {
		if(value == null || "".equals(value)){return null;}
		int index;
		for (String val : BOOKCLASS) {
			index = value.indexOf(val);
			if (index >= 0) {
				return val;
			}
		}
		return null;
	}

	// 取文书编号
	public static String getSentenceNo3(String valthml) {
		try {
			if (null == valthml || "".equals(valthml)) {
				return null;
			}
			String[] valueSipt = valthml.split("。");
			int index = 0;
			for (String val : valueSipt) {
				index = getDateIndex2(val);
				if (index >= 0) {
					valthml = val;
					break;
				}
			}
			if (index == -1) {
				return null;
			}
			index = valthml.lastIndexOf("书");
			if (index == -1) {
				return null;
			}
			valthml = valthml.substring(index + 1, valthml.length());
			index = valthml.indexOf("号");
			if (index == -1) {
				return null;
			}
			return valthml.substring(0, index + 1);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	// 判断字符是否非数字
	public static boolean isDigit(String strNum) {
		Pattern pattern = Pattern.compile("[0-9]{1,}");
		Matcher matcher = pattern.matcher((CharSequence) strNum);
		return matcher.matches();
	}

	// 根据符号+年份获取案号
	public static String getSplitCaseNum(String value) {
		if(value == null || "".equals(value)){return null;}
		try {
			for (String val : CAUSENUM) {
				int firstIndex = value.lastIndexOf(val);
				if (firstIndex == -1) {
					continue;
				}
				value = value.substring(firstIndex);
				int secondIndex = value.indexOf("号");
				if (secondIndex <= 0) {// 当案号中没有“号”字的时候
					String spl = null;
					String value2 = null;
					secondIndex = value.indexOf("第");
					if (secondIndex <= 0) {
						secondIndex = value.indexOf("字");
					}
					value2 = value.substring(0, secondIndex + 1);
					value = value.substring(secondIndex + 1);
					String[] split = value.split("");
					for (int i = 1; i < split.length; i++) {
						spl = split[i];
						boolean result =  isDigit(spl);
						if (!result) {
							break;
						}
						value2 = value2 + spl; // 取"第"字后面的数字，一个个添加进去
					}
					return value2;
				}
				value = value.substring(0, secondIndex + 1);
				return value;
			}
		} catch (Exception e) {
			logger.error("提取案号出错" + e.getMessage());
		}
		return null;
	}

	// 根据文书类型截取获得案号
	public static String getSplitCaseNum2(String value) {
		if(value == null || "".equals(value)){return null;}
		try {
			for (String val : BOOKCLASS) {
				int firstIndex = value.lastIndexOf(val);
				if (firstIndex == -1) {
					continue;
				}
				firstIndex = firstIndex + val.length();
				value = value.substring(firstIndex);
				int secondIndex = value.indexOf("号");
				if (secondIndex <= 0) {// 当案号中没有“号”字的时候
					String value2 = null;
					String spl = null;
					secondIndex = value.indexOf("第");
					if (secondIndex <= 0) {
						secondIndex = value.indexOf("字");
					}
					value2 = value.substring(0, secondIndex + 1);
					value = value.substring(secondIndex + 1);
					String[] split = value.split("");

					for (int i = 1; i < split.length; i++) {
						spl = split[i];
						boolean result =  isDigit(spl);
						if (!result) {
							break;
						}
						value2 = value2 + spl; // 取"第"字后面的数字，一个个添加进去
					}
					return value2;
				}
				value = value.substring(0, secondIndex + 1);
				for (String val3 : CAUSENUM3) {
					int lastIndex = value.indexOf(val3);
					if (lastIndex == -1) {
						continue;
					}
					return value;
				}
			}
		} catch (Exception e) {
			logger.error("提取案号出错" + e.getMessage());
		}
		return null;
	}

	// 特殊处理获取案号
	public static String getSplitCaseNum3(String value) {
		if(value == null || "".equals(value)){return null;}
		for (String val : CAUSENUM2) {
			int firstIndex = value.lastIndexOf(val);
			if (firstIndex == -1) {
				continue;
			}
			firstIndex = firstIndex + val.length();
			value = value.substring(firstIndex);
			int secondIndex = value.indexOf("号");
			if (secondIndex <= 0) {// 当案号中没有“号”字的时候
				String value2 = null;
				String spl = null;
				secondIndex = value.indexOf("第");
				if (secondIndex <= 0) {
					secondIndex = value.indexOf("字");
				}
				value2 = value.substring(0, secondIndex + 1);
				value = value.substring(secondIndex + 1);
				String[] split = value.split("");

				for (int i = 1; i < split.length; i++) {
					spl = split[i];
					boolean result =  isDigit(spl);
					if (!result) {
						break;
					}
					value2 = value2 + spl; // 取"第"字后面的数字，一个个添加进去
				}
				value2 = replaceCaseNum1(value);
				return value2;
			}
			value = value.substring(0, secondIndex + 1);
			value = replaceCaseNum1(value);
			return value;
		}

		return null;
	}

	// 去掉案号中多余的字
	public static String replaceCaseNum1(String value) {
		if(value == null || "".equals(value)){return null;}
		int index1 = value.indexOf("共印");
		int index2 = value.indexOf("份");
		if (index1 > 0) {
			String value3 = value.substring(index1, index2 + 1);
			value = value.replace(value3, "");
		}
		return value;
	}

	// 按符號切割字符-获取案号
	public static String getCaseNum(String value) {
		if(value == null || "".equals(value)){return null;}
		String[] split = value.split("。");
		for (int i = 0; i < split.length; i++) {
			value = getSplitCaseNum(split[i]);
			if (value != null && !"".equals(value)) {
				return value;
			}
			value = getSplitCaseNum2(split[i]);
			if (value != null && !"".equals(value)) {
				return value;
			}
			value = getSplitCaseNum3(split[i]);//
			if (value != null && !"".equals(value)) {
				return value;
			}
		}
		return null;
	}

	// 取审结日期
	public static String getConcludeDate(String date) {
		if(date == null || "".equals(date)){return null;}
		String[] data = {"二〇", "一九", "二○", "二０", "二0", "二O", "二0", "二Ｏ", "二�","20","19"};
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

	// 提取判决结果
	public static String getTheVerdictData(String value) {
		if(value == null || "".equals(value)){return null;}
		try {
			String text = null;
			int index = 0;
			int index2 = 0;
			for (String key : THEVERDICT) {
				index = value.lastIndexOf(key);
				if (index >= 0) {
					index2 = value.indexOf("审判长");
					if(index2<index){
						index2 = value.length();
					}
					text = value.substring(index, index2);
					index = text.lastIndexOf("公告");
					if (index >= 0){
						text = text.substring(0, index);
					}
					text = text.substring(0, text.lastIndexOf("。"));
					if (null != text)
						return text;
				}
			}

		} catch (Exception e) {
			logger.error("提取判决结果出错:" + e.getMessage());
		}
		return null;
	}

	// 去掉无用字符
	public static String getReplaceAll(String value) {
		if(value == null || "".equals(value)){return null;}
		StringBuffer sb = null;
		if (value != null && !"".equals(value)) {
			value = value.replaceAll(",", "，");
			value = value.replaceAll("�,o,O", "〇");
			value = value.replaceAll("[×,X,Ｘ,x,╳,＊,\\*]", "某");
			value = value.replaceAll("[\n,\t,\r,\\s,&nbsp; ,：,“,” ,:,<,/>,</,>,-,+,=,},{,#,\",',-,%,^,*]","");	//a-z,A-Z,没有去掉字母
//			value = value.replaceAll("[\n,\t,\r,\\s,&nbsp; ,：,“,”,・ ,:,<,/>,</,>,a-z,A-Z,-,+,=,},{,.,#,\",',-,%,^,*]","");	//去掉所有字母
			value = getSpecialStringALL(value);
			value = value.trim();
			sb = new StringBuffer();
			sb.append(value);
		}
		return sb == null ? "" : sb.toString();
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
	 * 
	 * @param value
	 * @return
	 */
	public static String getReplaceAllDate(String value) {
		StringBuffer sb = null;
		if (value != null && !"".equals(value)) {
			value = value.replaceAll("[（,）,(,),【,】,{,},<,>]", "");
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

	// 根据关键字提取人
	public static String getKeyName(Map<String, List<String>> map, int status) {
		if (map == null || "".equals(map)) {
			return null;
		}
		String[] keys = null;
		if (status == 1)
			keys =  PLAINTIFF;
		else
			keys =  DEFENDANT;
		Set<String> setNames = null;
		List<String> list = null;
		String[] vals = null;
		for (String key : keys) {
			list = map.get(key);
			if (null != list) {
				for (String val : list) {
					if (null == setNames) {
						setNames = new HashSet<String>();
					}
					if (val.indexOf("、") >= 0) {
						vals = val.split("、");
						for (String va : vals) {
							setNames.add(va);
						}
					} else
						setNames.add(val);
				}
			}
		}
		if (setNames == null || setNames.size() == 0) {
			return null;
		}
		StringBuffer sb = null;
		for (String val : setNames) {
			if (sb == null)
				sb = new StringBuffer(val);
			else
				sb.append("、").append(val);
		}
		return sb == null ? null : sb.toString();
	}
	
	public static String replaceCourtName(String value){
		if (value != null && !"".equals(value)) {
			for(String val : DATESTATUS){
				value = value.replaceAll(val,"");
			}
		value = value.replaceAll("[（,）,(,),【,】,{,},<,>,★,?,0-9,a-z,A-Z,!,！,#,$,%,&,*,/,\",／,|,、]", "");
		return value;
		}
		return value;
	}
	
	// 法院提取--提取法院名称方法1
	public static String getCourtName(String value){
		if(value == null || "".equals(value)){return null;}
		value = getDataAll(value);
		if(value == null || "".equals(value)){return null;}
		String[] valuesplit = value.split("。");
		String courtName = null;
		int index ;
		try {
			for(String val : valuesplit){
				index = val.indexOf("书");
				if(index == -1){
					index = val.indexOf("号");
					if(index == -1){
						index = val.indexOf("第");
					}
				}
				courtName = value.substring(0,index+1);
				courtName = courtName.replaceAll("[0-9,\\-,:,_,：]", "");
				index = courtName.indexOf("法院");
				courtName = courtName.substring(0, index+2);
				courtName = replaceCourtName(courtName);
				if(courtName != null && !"".equals(courtName)){
					if(courtName.length()>=4){
						return courtName;
					}
				}
			}
		} catch (Exception e) {
			logger.error("提取法院名称出错:" + e.getMessage());
		}
		return null;
	}
	// 法院提取--提取法院名称方法2
	public static String getAtherthe(String value) {
		if(value == null || "".equals(value)){return null;}
		String gatherthe = null;
		String[] gatherthes = null;
		StringBuffer sb = null;
		String[] valuesplit = value.split("。");
		// int index=0;
		try {
			for (String val : valuesplit) {
				// index=getDateIndex2(val);
				// if(index>=0){
				value = val;
				break;
				// }
			}
			// if(index==-1){return null;}
			gatherthe = value.substring(0, value.length());
			int index = value.indexOf("书");
			if(index == -1){
			 index = value.indexOf("号");
			}
			gatherthe = gatherthe.substring(0, gatherthe.indexOf(index) + 1);
			gatherthes = gatherthe.split("[0-9,\\-,:,_,：]");
			sb = new StringBuffer();
			for (String val : gatherthes) {
				if (null != val)
					sb.append(val);
			}
			if (sb.toString().lastIndexOf("}") >= 0) {
				gatherthe = sb.toString().substring(sb.toString().lastIndexOf("}") + 1,sb.toString().length());
				sb = new StringBuffer(gatherthe);
			}
			gatherthe = getAthertheReplace(sb.toString().substring(0,
					sb.toString().lastIndexOf("法院") + 2));
			gatherthe = replaceCourtName(gatherthe);
			if(gatherthe != null && !"".equals(gatherthes)){
				if(gatherthe.length()>=4){
			return gatherthe;
				}
			}
		} 
		catch (Exception e) {
//			logger.error("提取法院名称出错:" + e.getMessage());
		} 
		finally {
			sb = null;
			gatherthes = null;
		}
		return null;
	}

	// 法院提取--过滤法院
	public static String getAthertheReplace(String value) {
		if(value == null || "".equals(value)){return null;}
		int index = value.indexOf("法院");
		int index1 = value.lastIndexOf("法院");
		if (index != index1) {
			value = value.substring(index + 2, index1 + 2);
		}
		return value;
	}

	// 法院提取--取第一段
	public static int getDateIndex2(String value) {
		if(value == null || "".equals(value)){return -1;}
		int index = 0;
		for (String date : DATESTATUS) {
			index = value.indexOf(date);
			if (index >= 0) {
				return index + date.length();
			}
		}
		return -1;
	}

	// 取第一段
	public static int getDateIndex(String value) {
		if(value == null || "".equals(value)){return -1;}
		int index = 0;
		value = value.substring(0, value.indexOf("。"));
		for (String date : DATESTATUS) {
			index = value.indexOf(date);
			if (index >= 0) {
				return index + date.length();
			}
		}
		return -1;
	}

	// 提取案由
	public static String StringCause(String value) {
		if(value == null || "".equals(value)){return null;}
		value = getDataAll(value);
		try {
			int indx = value.lastIndexOf("书记员");
			if(indx > 0 ){
				value = value.substring(0, indx);
			}
			int index = 0;
			String firTxt = null;
			String lastxt = null;
			int count = 0;
			for (String val : CAUSE) {
				index = value.indexOf(val);
				if (index >= 0) {
					if (val.equals("驳回申诉通知") || val.equals("赔偿决定书")) {
						int index2 = getDateIndex(value);
						if (index == -1) {
							return null;
						}
						firTxt = value.substring(index2, value.length());
						firTxt = firTxt.substring(firTxt.indexOf("号") + 1,
								firTxt.length());
						value = firTxt;
						if (val.equals("赔偿决定书"))
							val = "国家赔偿";
					}
					index = value.indexOf(val);
					if (index < 0)
						index = value.indexOf("国家赔偿");
					firTxt = value.substring(0, index);
					count = firTxt.lastIndexOf("。");
					if (count == -1) {
						firTxt.lastIndexOf("号");
					}
					firTxt = firTxt.substring(count + 1, firTxt.length());
					index = value.indexOf(val);
					if (index < 0) {
						if (val.equals("驳回申诉通知"))
							val = "国家赔偿";
					}
					lastxt = value
							.substring(value.indexOf(val), value.length());
					lastxt = lastxt.substring(lastxt.indexOf(val),
							lastxt.indexOf("。") + 1);
					return new StringBuffer(firTxt).append(lastxt).toString();
				}
			}

		} catch (Exception e) {
			logger.error("提取案由出错:" + e.getMessage());
		}
		return null;
	}

	// 提取全文
	public static String getDataAll(String value) {
		if(value == null || "".equals(value)){return null;}
		int index = 0;
		index = value.indexOf("。");
		if(index == -1){return null;}
		String value2 = value.substring(0, index);
		for (String date : DATESTATUS) {
			index = value2.indexOf(date);
			if (index >= 0) {
				value = value.substring(index + date.length(), value.length());
				return value;
			}
		}
		return value;
	}
	
	//正确的编码读取内容    已停用
		public  static String getAllcharset(File fi) throws Exception{
			Document doc;
			String html = "";
			for(String val : charset){
				doc = Jsoup.parse(fi, val);
				html = getDataAll(doc.text());
				boolean Garbled = getErrorCode(html);//判断编码是否错误
				if (Garbled == false) {
					continue;	
				}
				return html;
			}
			return null;
		}
}
