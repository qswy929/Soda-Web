package com.app.data;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileLock;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.app.dao.SqlExec;

//查询处理类，负责Inceptor查询和组织结果
public class DataProc {
   private SqlExec se = new SqlExec();
	private String query="";
	private ResultSet rs = null;
	
	//初始化为空的数据
	private int aqiResult;
	private int[][] metroResult = new int[20][2];
	private int[] mallResult = new int[8];
	private int[] unicomResult = new int[20];

	
	//查询AQI
	private void getAqi() throws SQLException
	{
	   String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		query="select aqi from aqi_buc_stime where stime='"+date_hour+"'";
		rs = se.getSqlResult(query);
		while(rs.next())
		{
		    aqiResult= rs.getInt(1);
		}
		se.closeConn();
	}
	
	//查询地铁信息
	//metroResult第一列：入站，第二列：出站
	private void getMetro() throws SQLException
	{
		String date_min = new DateAdjuster().getDate();  //获取当前日期，精确到分钟
		String[] para=date_min.split(" ");
		//要查5分钟之内的总数数
		query="select sum(case when price=0 then 1 else 0 end),sum(case when price>0 then 1 else 0 end) from transcard where ddate='"+para[0]+"' and ttime='"+para[1]+"' group by gid order by gid";
		rs = se.getSqlResult(query);
		int row = 0;
		while(rs.next())
		{
			metroResult[row][0] = rs.getInt(1);
			metroResult[row][1] = rs.getInt(2);
			row++;
		}
		se.closeConn();
	}
	
	//查询汇纳商圈数据，返回人数
	private void getMall() throws SQLException
	{
		String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		String[] para=date_hour.split(" ");
		query="select num from mall_core where ddate='"+para[0]+"' and ttime='"+para[1]+"' order by mname";
		rs = se.getSqlResult(query);
	    int row = 0;
		while(rs.next())
		{
		    mallResult[row]= rs.getInt(1);
		    row++;
		}
		se.closeConn();
	}
	
	//联据数据
	private void getUnicom () throws SQLException
	{
		String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		String[] para=date_hour.split(" ");
		query="select count(*) from unicom_core where ddate='"+para[0]+"' and ttime='"+para[1]+"' group by gid order by gid";
		rs = se.getSqlResult(query);
	    int row = 0;
		while(rs.next())
		{
		    unicomResult[row]= rs.getInt(1);
		    row++;
		}
		se.closeConn();
	}
	
	
	
	//每5分钟执行的函数添加到此处
	public void runMinute() throws SQLException
	{
		getMetro();
	}
	
	//每小时执行的函数添加到处处
	public void runHour() throws SQLException
	{
		//getMall();
		//getAqi();
		getUnicom();
	}
	
	
	//保存每5分钟查询结果
	public void saveMinuteData()
	{
		//FileWriter fw = new FileWriter("/root/soda/dataMinute.txt",false);
		FileOutputStream fo=null;;
		OutputStreamWriter ow=null;
		BufferedWriter bw=null;
		FileLock fl = null;
		try {
			fo = new FileOutputStream("/root/soda/dataMinute.txt");
		   ow = new OutputStreamWriter(fo);
			bw = new BufferedWriter(ow);
			int i;
			fl=fo.getChannel().tryLock();   //“写”上锁
			if(fl!=null)
		    {
				 System.out.println("Locked File Minute.");
			}
			for(i=0;i<20;i++)
			{
			    bw.write(metroResult[i][0]+" ");
			    bw.write(metroResult[i][1]+"\n");
			}
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   finally {
		   if(fl!=null)
		   {
			   try {
				   fl.release();   //释放锁
				   bw.flush();
				   bw.close();
				   ow.close();
				   fo.close();
				  System.out.println("Released Lock Minute.");
		    	} catch (IOException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
			    }   
		   }
	   }
	}
	
	//保存每5分钟查询结果
		public void saveHourData()
		{
			//FileWriter fw = new FileWriter("/root/soda/dataMinute.txt",false);
			FileOutputStream fo=null;;
			OutputStreamWriter ow=null;
			BufferedWriter bw=null;
			FileLock fl = null;
			try {
				fo = new FileOutputStream("/root/soda/dataHour.txt");
			   ow = new OutputStreamWriter(fo);
				bw = new BufferedWriter(ow);
				int i;
				fl=fo.getChannel().tryLock();   //“写”上锁
				if(fl!=null)
			    {
					 System.out.println("Locked File Hour.");
				}
				for(i=0;i<20;i++)
				{
				    bw.write(unicomResult[i]+"\n");
				}
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   finally {
			   if(fl!=null)
			   {
				   try {
					   fl.release();   //释放锁
					   bw.flush();
					   bw.close();
					   ow.close();
					   fo.close();
					  System.out.println("Released Lock Hour.");
			    	} catch (IOException e) {
					    // TODO Auto-generated catch block
					    e.printStackTrace();
				    }   
			   }
		   }
		}
}
