package cn.com.szgao.dto;
/**
 * 档案实体类
 * @author dell
 *
 */
public class ArchivesVO {

	/**
	 * 标题
	 */
	private String title;
	/**
	 * URL
	 */
	private String detailLink;
	/**
	 * 法院名称
	 */
	private String courtName;
	private String area;
	private String catalog;
	private String province;
	private String publishDate;
	private String collectDate;
	private String caseNum;
	private String city;
	/**
	 * 原告
	 */
	private String plaintiff;
	/**
	 * 被告
	 */
	private String defendant;
	/**
	 * 审批结论
	 */
	private String approval;
	/**
	 * 诉讼类型
	 */
	private String suitType;
	/**
	 * 起诉日期
	 */
	private String suitDate;
	/**
	 * 审结日期
	 */
	private String approvalDate;
	/**
	 * 案由
	 */
	private String caseCause;
	/**
	 * 摘要
	 */
	private String summary;
	/**
	 * 键
	 */
	private String uuid;
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getCatalog() {
		return null==catalog?"":catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public String getProvince() {
		return null==province?"":province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getPublishDate() {
		return null==publishDate?"":publishDate;
	}
	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}
	public String getCollectDate() {
		return null==collectDate?"":collectDate;
	}
	public void setCollectDate(String collectDate) {
		this.collectDate = collectDate;
	}
	public String getCaseNum() {
		return null==caseNum?"":caseNum;
	}
	public void setCaseNum(String caseNum) {
		this.caseNum = caseNum;
	}
	public String getCity() {
		return null==city?"":city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public ArchivesVO(){}
	public ArchivesVO(String title,String detailLink,String courtName){
		this.title=title;
		this.detailLink=detailLink;
		this.courtName=courtName;
	}
	public ArchivesVO(String uuid,String plaintiff,String defendant,String approval,String suitType,String suitDate,
			String approvalDate,String caseCause,String summary){
		this.uuid=uuid;
		this.plaintiff=plaintiff;
		this.defendant=defendant;
		this.approval=approval;
		this.suitType=suitType;
		this.suitDate=suitDate;
		this.approvalDate=approvalDate;
		this.caseCause=caseCause;
		this.summary=summary;
	}
	public String getPlaintiff() {
		return null==plaintiff?"":plaintiff;
	}
	public void setPlaintiff(String plaintiff) {
		this.plaintiff = plaintiff;
	}
	public String getDefendant() {
		return null==defendant?"":defendant;
	}
	public void setDefendant(String defendant) {
		this.defendant = defendant;
	}
	public String getApproval() {
		return null==approval?"":approval;
	}
	public void setApproval(String approval) {
		this.approval = approval;
	}
	public String getSuitType() {
		return null==suitType?"":suitType;
	}
	public void setSuitType(String suitType) {
		this.suitType = suitType;
	}
	public String getSuitDate() {
		return null==suitDate?"":suitDate;
	}
	public void setSuitDate(String suitDate) {
		this.suitDate = suitDate;
	}
	public String getApprovalDate() {
		return null==approvalDate?"":approvalDate;
	}
	public void setApprovalDate(String approvalDate) {
		this.approvalDate = approvalDate;
	}
	public String getCaseCause() {
		return null==caseCause?"":caseCause;
	}
	public void setCaseCause(String caseCause) {
		this.caseCause = caseCause;
	}
	public String getSummary() {
		return null==summary?"":summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getUuid() {
		return null==uuid?"":uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getTitle() {
		return null==title?"":title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetailLink() {
		return null==detailLink?"":detailLink;
	}
	public void setDetailLink(String detailLink) {
		this.detailLink = detailLink;
	}
	public String getCourtName() {
		return null==courtName?"":courtName;
	}
	public void setCourtName(String courtName) {
		this.courtName = courtName;
	}
	
}
