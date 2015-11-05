package cn.com.szgao.dto;

public class RecordData {

	/**
	 * 市名
	 */
	private String cityName;
	/**
	 * 省名
	 */
	private String provinceName;
	/**
	 * 数据记录条数据
	 */
    private Long numberData;
    public RecordData(){};
    public RecordData(String provinceName,String cityName,long numberData){
    	this.provinceName=provinceName;
    	this.cityName=cityName;
    	this.numberData=numberData;
    }
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public Long getNumberData() {
		return numberData;
	}
	public void setNumberData(Long numberData) {
		this.numberData = numberData;
	}
    
}
