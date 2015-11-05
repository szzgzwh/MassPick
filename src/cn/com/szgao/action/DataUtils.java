package cn.com.szgao.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
/**
 * 数据库工具类：连接数据库；
 * 查询省、市、县区等数据
 * @author xiongchangyi
 * @since 2015-06-01
 */
public class DataUtils {
	/**
	 * 查询【省】集合，存储字段是：common_name,province_name
	 */
	static Map<String,String> shengMap = new HashMap<String,String>();
	/**
	 * 查询【地级市】集合，存储字段是short_name,city_name
	 */
	static Map<String,String> shiMap = new HashMap<String,String>();
	/**
	 * 查询【县级市】集合，存储字段：short_name,country
	 */
	static Map<String,String> xianJiShiMap = new HashMap<String,String>();
	/**
	 * 查询【县】集合，存储字段：short_name,country
	 */
	static Map<String,String> xianMap = new HashMap<String,String>();
	/**
	 * 查询【区】集合，存储字段：country
	 */
	static List<String> quList = new ArrayList<String>();
	/**
	 * 查询【县级市、县、旗】集合，存储字段：short_name,country  XianJiShiXianQu
	 */
	static Map<String,String> xianJiShiXianMap = new HashMap<String,String>();
	/**
	 * 查询【县级市、县、旗】集合，存储字段：country,parent_id
	 */
	static Map<String,Integer> xianJiShiXianQuMap = new HashMap<String,Integer>();
	/**
	 * 查询【地级市名称、省ID】
	 * 存储字段： city_name,parent_id
	 */
	static Map<String,Integer> province_city_map = new HashMap<String,Integer>();
	
	
	
	//县
	static Map<String,Integer> countryMap = new HashMap<String,Integer>();
	//市名称和省的ID
	//县名称和市的ID
	static Map<String,Integer> city_country_map = new HashMap<String,Integer>();
	//县级市简称和上级市ID
	static Map<String,Integer> shortCountryMap = new HashMap<String,Integer>();
	/**
	 * 通用连接数据库的方法
	 * @param  conn: 连接对象
	 * @author xiongchangyi
	 * @since 2018-05-08
	 * @version 1.0
	 */
	public Connection getConnection() throws ClassNotFoundException, SQLException{
		String url = "jdbc:postgresql://192.168.251:5432/duplicatedb";
		String usr = "postgres";
		String psd = "615601.xcy*";
		Connection conn = null;
		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection(url, usr, psd);
		return conn;
	}
	/**
	 * 查询【省】，存储字段：common_name,province_name      +++++++++++++
	 * @author xiongchangyi
	 * @since 2018-05-08
	 * @version 1.0
	 */
	public void listProvince()
	{
		PreparedStatement provinceStmt = null;//查询省
		Connection conn = null;//连接
		ResultSet rs = null;//结果集
		String provinceSql = "SELECT common_name,province_name FROM province_t";//省
		try {
			conn = getConnection();	
			provinceStmt = conn.prepareStatement(provinceSql);	//预编译查询
			rs = provinceStmt.executeQuery();//查询省
			while(rs.next())
			{
				shengMap.put(rs.getString(1),rs.getString(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=provinceStmt)
		          {
		        	  provinceStmt.close();		        
		        	  provinceStmt = null;		        		          
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	
	/**
	 * 查询【地级市】，存储字段：short_name,city_name      +++++++++++++++++
	 */
	public void listCity()
	{
		PreparedStatement cityStmt = null;//查询市
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String citySql = "SELECT short_name,city_name FROM city_t WHERE short_name IS NOT NULL";//市
		try {
			conn = getConnection();	
			cityStmt = conn.prepareStatement(citySql);		//预编译查询
			rs = cityStmt.executeQuery();//查询市
			while(rs.next())
			{
				shiMap.put(rs.getString(1),rs.getString(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=cityStmt)
		          {
		        	  cityStmt.close();
		        	  cityStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	
	/**
	 * 查询【县级市】，存储字段：short_name,country          +++++++++++++
	 */
	public void listAllShortCityName(){
		PreparedStatement countryStmt = null;//查询县
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT short_name,country FROM country_t WHERE country like '%市'"; //县
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);  //预编译查询
			rs = countryStmt.executeQuery();				  //查询县
			while(rs.next())
			{
				xianJiShiMap.put(rs.getString(1),rs.getString(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	
	/**
	 * 查询【县】，存储字段：short_name,country             ++++++++++++++
	 */
	public void listAllCountry(){
		PreparedStatement countryStmt = null;//查询县、旗
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT short_name,country FROM country_t WHERE country LIKE '%县' OR country LIKE '%旗'"; //县
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);  //预编译查询
			rs = countryStmt.executeQuery();				  //查询县
			while(rs.next())
			{
				xianMap.put(rs.getString(1),rs.getString(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	
	/**
	 * 查询【区】，存储字段：country             +++++++++++++++          
	 */
	public void listAllArea(){
		PreparedStatement countryStmt = null;//查询区
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT country FROM country_t WHERE country LIKE '%区'"; //区
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);  //预编译查询
			rs = countryStmt.executeQuery();				  //查询县
			while(rs.next())
			{
				quList.add(rs.getString(1));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	/**
	 * 查询【县级市、县、旗】集合，存储字段：short_name,country  +++++++++++++++          
	 */
	public void listXianJiShiXian(){
		PreparedStatement countryStmt = null;
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT short_name,country FROM country_t WHERE country LIKE '%市' OR country LIKE '%县' OR country LIKE '%旗'"; 
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);  //预编译查询
			rs = countryStmt.executeQuery();				  //查询县
			while(rs.next())
			{
				xianJiShiXianMap.put(rs.getString(1),rs.getString(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  rs.close();
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	
	/**
	 * 不带条件，查询【所有县级市、县、区】及地级市ID      ++++++++++++++++++
	 * 存储字段：country,parent_id
	 */
	public void listXianJiShiXianQu()
	{
		PreparedStatement countryStmt = null;//查询县、县级市、区
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT country,parent_id FROM country_t"; //县、县级市、区，及父id
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);  //预编译查询
			rs = countryStmt.executeQuery();				  //查询县
			while(rs.next())
			{
				xianJiShiXianQuMap.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	
	/**
	 * 通过市ID查询【地级市市名称和省ID】                                ++++++++++++++++++++++++
	 * 存储字段：city_name,parent_id
	 */
	public Map<String,Integer> listCityProvinceIdByCityId(int cityId){
		Map<String,Integer> cityNameAndProvinceIdMap = new HashMap<String,Integer>();
		PreparedStatement countryStmt = null;//查询市
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT city_name,parent_id FROM city_t WHERE city_id=?";//县
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);//预编译查询
			countryStmt.setInt(1, cityId);
			rs = countryStmt.executeQuery();//查询县
			while(rs.next())
			{
				cityNameAndProvinceIdMap.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
		return cityNameAndProvinceIdMap;
	}
	
	/**
	 * 通过省ID查询省名称                                                          +++++++++++++++++++++++++++
	 * @param id: 是省ID
	 * @author xiongchangyi
	 * @since 2018-05-08
	 * @version 1.0
	 */
	public String listProvinceNameByProvinceId(int id)
	{
		PreparedStatement provinceStmt = null;//查询省
		Connection conn = null;//连接
		ResultSet rs = null;//结果集
		String provinceSql = "SELECT province_name FROM province_t WHERE province_id=?";//省
		try {
			conn = getConnection();	
			provinceStmt = conn.prepareStatement(provinceSql);	//预编译查询
			provinceStmt.setInt(1, id);
			rs = provinceStmt.executeQuery();//查询省
			if(rs.next())
			{
				return rs.getString(1);
			}	
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=provinceStmt)
		          {
		        	  provinceStmt.close();		        
		        	  provinceStmt = null;		        		          
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
		return null;
	}
	
	/**
	 * 不带条件查询【市名称】和【省的ID】                         ++++++++++++++++++++++++++++++
	 * 存储字段：city_name,parent_id
	 */
	public void listProvinceCity()
	{
		PreparedStatement cityStmt = null;//查询市
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String citySql = "SELECT city_name,parent_id FROM city_t";//市
		try {			
			conn = getConnection();	
			cityStmt = conn.prepareStatement(citySql);		//预编译查询
			rs = cityStmt.executeQuery();//查询市
			while(rs.next())
			{
				province_city_map.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=cityStmt)
		          {
		        	  cityStmt.close();
		        	  cityStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	/**
	 * 查询 县 数据到集合中
	 */
	public void listCountry()
	{
		PreparedStatement countryStmt = null;//查询县
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT country,parent_id FROM country_t country like '%县'"; //县
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);  //预编译查询
			rs = countryStmt.executeQuery();				  //查询县
			while(rs.next())
			{
				countryMap.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	

	/**
	 * 查询市ID和县名称
	 */
	public void listCityIdCountry()
	{
		PreparedStatement cityStmt = null;//查询市
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String citySql = "SELECT country,parent_id FROM country_t";
		try {			
			conn = getConnection();	
			cityStmt = conn.prepareStatement(citySql);		//预编译查询
			rs = cityStmt.executeQuery();//查询市
			while(rs.next())
			{
				city_country_map.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=cityStmt)
		          {
		        	  cityStmt.close();
		        	  cityStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	}
	/**
	 * 通过县名称，查询市的ID
	 * 通过区名称，查询市ID
	 * 上面2个都可以通用这个方法
	 * 
	 */
	public Map<String,Integer> listCityIdByCountryName(String country){
		//县名称和市的ID
		Map<String,Integer> city_country_map = new HashMap<String,Integer>();		
		PreparedStatement countryStmt = null;//查询市
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT country,parent_id FROM country_t WHERE country=?";//县
		try {
			conn = getConnection();	
			countryStmt = conn.prepareStatement(countrySql);//预编译查询
			countryStmt.setString(1, country);
			rs = countryStmt.executeQuery();//查询县
			while(rs.next())
			{
				city_country_map.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
		return city_country_map;
	}
	
	/**
	 * 通过县简称查询市ID和县简称的Map
	 */
	public void listCountryParentId(){
		PreparedStatement countryStmt = null;//查询县级市
		Connection conn = null;//连接
		ResultSet rs = null;//结果集		
		String countrySql = "SELECT short_name,parent_id FROM country_t WHERE short_name IS NOT NULL";
		try {
			countryStmt = getConnection().prepareStatement(countrySql);		//预编译查询
			rs = countryStmt.executeQuery();//查询市
			while(rs.next())
			{
				shortCountryMap.put(rs.getString(1),rs.getInt(2));
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		          if(null !=countryStmt)
		          {
		        	  countryStmt.close();
		        	  countryStmt = null;
		         }
		      }catch(SQLException se){
		    }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }		
	}
	
	
	
	/**
	 * CouchBase公共查询
	 * @param ip CouchBase的IP地址
	 * @param bucketName Bucket名称
	 * @param view_dev 视图的设计名称
	 * @param viewName 视图的名称
	 * @param limit 视图限制条数
	 * @return 返回视图里面所有数据记录条数迭代器
	 */
	public Iterator<ViewRow> commonViewIterator(String ip,String bucketName,String view_dev,String viewName,int limit){
		//连接服务器
		Cluster cluster = CouchbaseCluster.create(ip);
		//连接指定的桶
		Bucket bucket = cluster.openBucket(bucketName);
		//查询视图
		ViewResult result = bucket.query(ViewQuery.from(view_dev, viewName).limit(limit));	
		Iterator<ViewRow> iterator = result.rows();		
		return iterator;
	}
	/**
	 * CouchBase公共查询
	 * @param ip CouchBase的IP地址
	 * @param bucketName Bucket名称
	 * @return 返回Bucket对象
	 */
	public Bucket commonBucket(String ip,String bucketName){
		//连接服务器
		Cluster cluster = CouchbaseCluster.create(ip);
		//连接指定的桶
		Bucket bucket = cluster.openBucket(bucketName);
		return bucket;
	}
}
