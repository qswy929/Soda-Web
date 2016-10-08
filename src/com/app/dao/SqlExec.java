package com.app.dao;

import java.sql.*;

//数据库连接类
public class SqlExec {
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private Connection conn=null;
    private Statement stmt=null;
	private ResultSet rs=null;
	
    //返回查询结果
	public ResultSet getSqlResult(String str) throws SQLException
	{
		try {
			Class.forName(driverName);
			} catch (ClassNotFoundException e) {
			      e.printStackTrace();
			      System.exit(1);
			    }
			//Hive2 JDBC URL with LDAP
			String jdbcURL = "jdbc:inceptor2://10.15.160.20:10000/soda";
			conn = DriverManager.getConnection(jdbcURL);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(str);		    
			return rs;		
	}
	
	//关闭连接
	public void closeConn()
	{
		try {
			rs.close();
			stmt.close();
			   conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
