package cn.com.szgao.dto;
/**
 * 公司实体类
 * @author xiongchangyi
 * 2015-3-5
 */
public class CompanyVO {
	/**
	 * 公司ID
	 */
	private Long companyId;
	/**
	 * 公司名称
	 */
	private String companyName;
	/**
	 * 责任人
	 */
	private String responsiblePerson;
	/**
	 * 联系人
	 */
	private String contactPerson;
	/**
	 * 职位
	 */
	private String occupPostion;
	/**
	 * 行政区划
	 */
	private String compartment;
	/**
	 * 公司地址
	 */
	private String enterpAddress;
	/**
	 * 区号
	 */
	private String areaCode;
	/**
	 * 公司电话
	 */
	private String enterpTell;
	/**
	 * 公司分机
	 */
	private String enterpExten;
	/**
	 * 公司传真
	 */
	private String enterpFax;
	/**
	 * 公司手机
	 */
	private String enterpMobile;
	/**
	 * 邮政编码
	 */
	private String postCode;
	/**
	 * 公司邮箱
	 */
	private String enterpEmail;
	/**
	 * 公司网址
	 */
	private String enterpUrl;
	/**
	 * 公司经营范围
	 */
	private String manageScope;
	/**
	 * 经营产品
	 */
	private String manageProduct;
	/**
	 * 主营行业
	 */
	private String manageTrade;
	/**
	 * 行业代码
	 */
	private String industryCode;
	/**
	 * 经济代码
	 */
	private String economicCode;
	/**
	 * 股份控制情况
	 */
	private String controlStake;
	/**
	 * 隶属关系
	 */
	private String subordRelation;
	/**
	 * 公司开业时间
	 */
	private String openDate;
	/**
	 * 注册日期
	 */
	private String registDate;
	/**
	 * 公司经营状态
	 */
	private String manageState;
	/**
	 * 会计制度
	 */
	private String accountSystem;
	/**
	 * 机构类型
	 */
	private String orgType;
	/**
	 * 注册资金
	 */
	private String registCapital;
	/**
	 * 年收入
	 */
	private String yearRurnover;
	/**
	 * 营业收入
	 */
	private String operationRevenue;
	/**
	 * 总资产
	 */
	private String totalAssets;
	/**
	 * 职工人数
	 */
	private String employeeAmount;
	/**
	 * 公司简介
	 */
	private String briefIntroduct;
	/**
	 * 经营模式
	 */
	private String manageModel;
	/**
	 * 经济类型
	 */
	private String economicType;
	/**
	 * 注册地址
	 */
	private String registAddress;
	/**
	 * 经营地址
	 */
	private String manageAddress;
	/**
	 * 主要市场
	 */
	private String mainMarke;
	/**
	 * 经营品牌
	 */
	private String manageBrand;
	/**
	 * 主要客户
	 */
	private String mainCustom;
	/**
	 * 管理体系
	 */
	private String manageSystem;
	/**
	 * 银行资金账户
	 */
	private String bankAccount;
	/**
	 * 银行名称
	 */
	private String bankName;
	/**
	 * 是否OEM
	 */
	private byte oemIs;
	/**
	 * 研发人数
	 */
	private String developAmount;
	/**
	 * 厂房面积
	 */
	private String factoryArea;
	/**
	 * 质量控制
	 */
	private String qualityControl;
	/**
	 * 月产量
	 */
	private String monthOutput;
	/**
	 * 信用等级
	 */
	private String creditRat;
	/**
	 * 年进口额
	 */
	private String importAmount;
	/**
	 * 年出口额
	 */
	private String outAmount;
	/**
	 * 诚信类型  
	 */
	private String credibilityType;
	/**
	 * 法人
	 */
	private String legalPerson;
	
	public CompanyVO(Long companyId, String registDate) {
		super();
		this.companyId = companyId;
		this.registDate = registDate;
	}
	public CompanyVO(){
		
	}
	public Long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getResponsiblePerson() {
		return responsiblePerson;
	}
	public void setResponsiblePerson(String responsiblePerson) {
		this.responsiblePerson = responsiblePerson;
	}
	public String getContactPerson() {
		return contactPerson;
	}
	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}
	public String getOccupPostion() {
		return occupPostion;
	}
	public void setOccupPostion(String occupPostion) {
		this.occupPostion = occupPostion;
	}
	public String getCompartment() {
		return compartment;
	}
	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}
	public String getEnterpAddress() {
		return enterpAddress;
	}
	public void setEnterpAddress(String enterpAddress) {
		this.enterpAddress = enterpAddress;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public String getEnterpTell() {
		return enterpTell;
	}
	public void setEnterpTell(String enterpTell) {
		this.enterpTell = enterpTell;
	}
	public String getEnterpExten() {
		return enterpExten;
	}
	public void setEnterpExten(String enterpExten) {
		this.enterpExten = enterpExten;
	}
	public String getEnterpFax() {
		return enterpFax;
	}
	public void setEnterpFax(String enterpFax) {
		this.enterpFax = enterpFax;
	}
	public String getEnterpMobile() {
		return enterpMobile;
	}
	public void setEnterpMobile(String enterpMobile) {
		this.enterpMobile = enterpMobile;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public String getEnterpEmail() {
		return enterpEmail;
	}
	public void setEnterpEmail(String enterpEmail) {
		this.enterpEmail = enterpEmail;
	}
	public String getEnterpUrl() {
		return enterpUrl;
	}
	public void setEnterpUrl(String enterpUrl) {
		this.enterpUrl = enterpUrl;
	}
	public String getManageScope() {
		return manageScope;
	}
	public void setManageScope(String manageScope) {
		this.manageScope = manageScope;
	}
	public String getManageProduct() {
		return manageProduct;
	}
	public void setManageProduct(String manageProduct) {
		this.manageProduct = manageProduct;
	}
	public String getManageTrade() {
		return manageTrade;
	}
	public void setManageTrade(String manageTrade) {
		this.manageTrade = manageTrade;
	}
	public String getIndustryCode() {
		return industryCode;
	}
	public void setIndustryCode(String industryCode) {
		this.industryCode = industryCode;
	}
	public String getEconomicCode() {
		return economicCode;
	}
	public void setEconomicCode(String economicCode) {
		this.economicCode = economicCode;
	}
	public String getControlStake() {
		return controlStake;
	}
	public void setControlStake(String controlStake) {
		this.controlStake = controlStake;
	}
	public String getSubordRelation() {
		return subordRelation;
	}
	public void setSubordRelation(String subordRelation) {
		this.subordRelation = subordRelation;
	}
	public String getOpenDate() {
		return openDate;
	}
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}
	public String getRegistDate() {
		return registDate;
	}
	public void setRegistDate(String registDate) {
		this.registDate = registDate;
	}
	public String getManageState() {
		return manageState;
	}
	public void setManageState(String manageState) {
		this.manageState = manageState;
	}
	public String getAccountSystem() {
		return accountSystem;
	}
	public void setAccountSystem(String accountSystem) {
		this.accountSystem = accountSystem;
	}
	public String getOrgType() {
		return orgType;
	}
	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}
	public String getRegistCapital() {
		return registCapital;
	}
	public void setRegistCapital(String registCapital) {
		this.registCapital = registCapital;
	}
	public String getYearRurnover() {
		return yearRurnover;
	}
	public void setYearRurnover(String yearRurnover) {
		this.yearRurnover = yearRurnover;
	}
	public String getOperationRevenue() {
		return operationRevenue;
	}
	public void setOperationRevenue(String operationRevenue) {
		this.operationRevenue = operationRevenue;
	}
	public String getTotalAssets() {
		return totalAssets;
	}
	public void setTotalAssets(String totalAssets) {
		this.totalAssets = totalAssets;
	}
	public String getEmployeeAmount() {
		return employeeAmount;
	}
	public void setEmployeeAmount(String employeeAmount) {
		this.employeeAmount = employeeAmount;
	}
	public String getBriefIntroduct() {
		return briefIntroduct;
	}
	public void setBriefIntroduct(String briefIntroduct) {
		this.briefIntroduct = briefIntroduct;
	}
	public String getManageModel() {
		return manageModel;
	}
	public void setManageModel(String manageModel) {
		this.manageModel = manageModel;
	}
	public String getRegistAddress() {
		return registAddress;
	}
	public void setRegistAddress(String registAddress) {
		this.registAddress = registAddress;
	}
	public String getMainMarke() {
		return mainMarke;
	}
	public void setMainMarke(String mainMarke) {
		this.mainMarke = mainMarke;
	}
	public String getManageBrand() {
		return manageBrand;
	}
	public void setManageBrand(String manageBrand) {
		this.manageBrand = manageBrand;
	}
	public String getMainCustom() {
		return mainCustom;
	}
	public void setMainCustom(String mainCustom) {
		this.mainCustom = mainCustom;
	}
	public String getManageSystem() {
		return manageSystem;
	}
	public void setManageSystem(String manageSystem) {
		this.manageSystem = manageSystem;
	}
	public String getBankAccount() {
		return bankAccount;
	}
	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public byte getOemIs() {
		return oemIs;
	}
	public void setOemIs(byte oemIs) {
		this.oemIs = oemIs;
	}
	public String getDevelopAmount() {
		return developAmount;
	}
	public void setDevelopAmount(String developAmount) {
		this.developAmount = developAmount;
	}
	public String getFactoryArea() {
		return factoryArea;
	}
	public void setFactoryArea(String factoryArea) {
		this.factoryArea = factoryArea;
	}
	public String getQualityControl() {
		return qualityControl;
	}
	public void setQualityControl(String qualityControl) {
		this.qualityControl = qualityControl;
	}
	public String getMonthOutput() {
		return monthOutput;
	}
	public void setMonthOutput(String monthOutput) {
		this.monthOutput = monthOutput;
	}
	public String getCreditRat() {
		return creditRat;
	}
	public void setCreditRat(String creditRat) {
		this.creditRat = creditRat;
	}
	public String getImportAmount() {
		return importAmount;
	}
	public void setImportAmount(String importAmount) {
		this.importAmount = importAmount;
	}
	public String getOutAmount() {
		return outAmount;
	}
	public void setOutAmount(String outAmount) {
		this.outAmount = outAmount;
	}
	public String getCredibilityType() {
		return credibilityType;
	}
	public void setCredibilityType(String credibilityType) {
		this.credibilityType = credibilityType;
	}
	public String getLegalPerson() {
		return legalPerson;
	}
	public void setLegalPerson(String legalPerson) {
		this.legalPerson = legalPerson;
	}
	public String getManageAddress() {
		return manageAddress;
	}
	public void setManageAddress(String manageAddress) {
		this.manageAddress = manageAddress;
	}
	public String getEconomicType() {
		return economicType;
	}
	public void setEconomicType(String economicType) {
		this.economicType = economicType;
	}		
}
