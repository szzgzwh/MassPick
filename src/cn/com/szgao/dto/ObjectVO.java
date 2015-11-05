package cn.com.szgao.dto;
/**
 * @author xcy
 * @version v1.0
 * 对象实体类
 */
public class ObjectVO {
	/**
	 * 对象ID
	 */
	private Long objId;
	/**
	 * 对象名称
	 */
	private String objName;
	/**
	 * 对象备注
	 */
	private String mark;
	
	public Long getObjId() {
		return objId;
	}
	public void setObjId(Long objId) {
		this.objId = objId;
	}
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
}
