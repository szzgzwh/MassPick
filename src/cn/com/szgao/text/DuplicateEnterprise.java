package cn.com.szgao.text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cn.com.szgao.dao.impl.CollectionDataDao;

/**
 * 购置企业数据去重
 * 去重规则：  按照3个字段 企业名称+市+县
 * @author xiongchangyi
 *
 */
public class DuplicateEnterprise {
	static String url = "jdbc:postgresql://192.168.251:5432/duplicatedb";
    static String usr = "postgres";
    static String psd = "615601.xcy*";
    private static Logger logger = LogManager.getLogger(DuplicateEnterprise.class.getName());
	public static void main(String[] args) {
		PropertyConfigurator.configure("E:\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties");
		//查询比较的基础字段
		PreparedStatement stmtSelect = null;
		//按照第一次差的结果[企业名称+市+县]去查询
		PreparedStatement stmtMore = null;
		//删除
		PreparedStatement stmtUpdate = null;
		Connection conn = null;
		ResultSet rs = null;
		//查询被比较
		ResultSet rsToCompare = null;
		String sql = "select compare from enterp_buy_t where enterp_id=?";
		String updateSql = "delete from enterp_buy_t where enterp_id=?";
		//按照第一次差的结果[企业名称+市+县]去查询
		String moreSql = "select enterp_id from enterp_buy_t where compare=?";
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, usr, psd);
			stmtSelect=conn.prepareStatement(sql);
			//按照企业ID删除企业记录 语句
			stmtUpdate=conn.prepareStatement(updateSql);
			//按照第一次差的结果[企业名称+市+县]查询：可能多条记录 语句
			stmtMore = conn.prepareStatement(moreSql);
			//自动提交事务取消
			conn.setAutoCommit(false);
			int index = 0;
			for(int i=1;i<=13869173;i++)
			{
			 
			  stmtSelect.setInt(1,i);
			  rs=stmtSelect.executeQuery();
			  while(rs.next())
			  {
				 String compareBase = rs.getString(1);				
				 if(null != compareBase||!"".equals(compareBase))
				 {
					 System.out.println(i+">>"+compareBase);					 
					 stmtMore.setString(1,compareBase);//按照第一次差的结果[企业名称+市+县]查询：可能多条记录
					 rsToCompare = stmtMore.executeQuery();
					 int count = 0;
					 while(rsToCompare.next())
					 {
						 
						++count;
						int toCompareEnterpId = rsToCompare.getInt(1);//获得ID
				
						if(count > 1)
						{
							++index;
							stmtUpdate.setInt(1, toCompareEnterpId);//删除
							//conn.commit();	
							stmtUpdate.addBatch();
							logger.info("Delete Id: "+toCompareEnterpId);
						}
						 if(index==5)
						  {
							  index = 0;
							  stmtUpdate.executeBatch();
							  conn.commit();	
						  }
					 }					 
				 }					
			  }
			 
			}			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}finally{
		      try{
		         if(stmtUpdate!=null||stmtSelect!=null||stmtMore!=null){
		        	stmtSelect.close();
		        	rsToCompare.close();
		        	stmtUpdate.close();
		        	stmtMore.close();
		        	stmtMore = null;
		        	rsToCompare = null;
		        	stmtSelect = null;
		         	stmtUpdate=null;	
		            conn.close();
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

}
