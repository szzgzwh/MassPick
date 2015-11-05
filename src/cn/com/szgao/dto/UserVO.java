package cn.com.szgao.dto;

import java.util.Date;
/**
 * 用户实体类
 * @author xiongchangyi
 * @since  2015-2-11
 */
public class UserVO {
	/**
	 * 用户ID
	 * @author xiongchangyi
	 */
	private Long userId;
	/**
	 * 员工工号
	 */
	private String employeeNo;
	/**
	 * 用户名
	 * @author xiongchangyi
	 */
	private String userName;
	/**
	 * 性别
	 * @author xiongchangyi
	 */
	private Byte sex;
	/**
	 * 邮箱地址
	 * @author xiongchangyi
	 */
	private String email;
	/**
	 * 手机号码
	 * @author xiongchangyi
	 */
	private String mobile;
	/**
	 * 部门
	 * @author xiongchangyi
	 */
	private String department;
	/**
	 * 创建时间
	 * @author xiongchangyi
	 */
	private Date createDate;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Byte getSex() {
		return sex;
	}
	public void setSex(Byte sex) {
		this.sex = sex;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getEmployeeNo() {
		return employeeNo;
	}
	public void setEmployeeNo(String employeeNo) {
		this.employeeNo = employeeNo;
	}

}
