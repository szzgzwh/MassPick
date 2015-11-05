package cn.com.szgao.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 网站提取字段实体类
 * @author xiongchangyi
 * @since  2015-2-11
 */
public class NetUrlVO {
	/**
	 * 网址ID
	 * @author xiongchangyi
	 */
	private String urlId;
	/**
	 * 网址
	 * @author xiongchangyi
	 */
	private String url;
	/**
	 * 网址文本
	 * @author xiongchangyi
	 */
	private String urlText;
	/**
	 * 网址来源
	 * @author xiongchangyi
	 */
	private String urlSource;
	/**
	 * 网址状态
	 * @author xiongchangyi
	 */
	private Byte urlState;
	/**
	 * 创建时间
	 * @author xiongchangyi
	 */
	private Date createDate;
	/**
	 * 创建用户
	 * @author xiongchangyi
	 */
	private UserVO createUser;
	/**
	 * 最后修改时间
	 * @author xiongchangyi
	 */
	private Date lastModifyDate;
	/**
	 * 最后修改人
	 * @author xiongchangyi
	 */
	private UserVO lastModifyUser;
	/**
	 * 下级URL
	 * @author xiongchangyi
	 */
	private Set<NetUrlVO> subUrl = new HashSet<NetUrlVO>();
	/**
	 * 上级URL
	 * @author xiongchangyi
	 */
	private Set<NetUrlVO> parentUrl = new HashSet<NetUrlVO>();
	/**
	 * 构造函数	
	 * @param urlId        		url的ID
	 * @param url          		URL
	 * @param urlText      		url的文本
	 * @param urlState     		url状态
	 * @param createDate   		创建时间
	 * @param createUser   		创建用户
	 * @param lastModifyDate 	最后修改时间	
	 * @param lastModifyUser 	最后修改人
	 */
	public NetUrlVO(String urlId, String url, String urlText,
			Byte urlState, Date createDate, UserVO createUser,
			Date lastModifyDate, UserVO lastModifyUser) {
		super();
		this.urlId = urlId;
		this.url = url;
		this.urlText = urlText;		
		this.urlState = urlState;
		this.createDate = createDate;
		this.createUser = createUser;
		this.lastModifyDate = lastModifyDate;
		this.lastModifyUser = lastModifyUser;
	}
	public NetUrlVO(){
		
	}
	public String getUrlId() {
		return urlId;
	}
	public void setUrlId(String urlId) {
		this.urlId = urlId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrlText() {
		return urlText;
	}
	public void setUrlText(String urlText) {
		this.urlText = urlText;
	}
	public String getUrlSource() {
		return urlSource;
	}
	public void setUrlSource(String urlSource) {
		this.urlSource = urlSource;
	}
	public Byte getUrlState() {
		return urlState;
	}
	public void setUrlState(Byte urlState) {
		this.urlState = urlState;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public UserVO getCreateUser() {
		return createUser;
	}
	public void setCreateUser(UserVO createUser) {
		this.createUser = createUser;
	}
	public Date getLastModifyDate() {
		return lastModifyDate;
	}
	public void setLastModifyDate(Date lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
	}
	public UserVO getLastModifyUser() {
		return lastModifyUser;
	}
	public void setLastModifyUser(UserVO lastModifyUser) {
		this.lastModifyUser = lastModifyUser;
	}
	public Set<NetUrlVO> getSubUrl() {
		return subUrl;
	}
	public void setSubUrl(Set<NetUrlVO> subUrl) {
		this.subUrl = subUrl;
	}
	public Set<NetUrlVO> getParentUrl() {
		return parentUrl;
	}
	public void setParentUrl(Set<NetUrlVO> parentUrl) {
		this.parentUrl = parentUrl;
	}
		
}
