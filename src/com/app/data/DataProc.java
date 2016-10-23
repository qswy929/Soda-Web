package com.app.data;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.channels.FileLock;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import com.app.dao.SqlExec;
import com.app.predict.HoltWinters;
import com.app.transfer.DateAdjuster;

//查询处理类，负责Inceptor查询和组织结果
public class DataProc {
   private SqlExec se = new SqlExec();
   private HoltWinters hw = new HoltWinters();
	private String query="";
	private ResultSet rs = null;
	private DateAdjuster da = new DateAdjuster();
	
	//初始化为空的数据
	private int aqiResult = 10;
	private float[] rainResult = new float[2];
	private int[][] metroResult = new int[20][2];
	private int[][] taxiResult = new int[20][2];
	private int[] mallResult = new int[8];
	private int[] unicomResult = new int[20];
	private int[] hmetroResult = new int[3];
	private int[][] htaxiResult = new int[3][2];
	private int[][] hunicomResult = new int[3][2];
	private int[][] hmallResult = new int[3][2];
	private int[] accCount = {0,0,0};
	private ArrayList<Double> gid1_score = new ArrayList();
	private ArrayList<Double> gid2_score = new ArrayList();
	private ArrayList<Double> gid3_score = new ArrayList();
	private ArrayList<Object[]> accResult = new ArrayList();
	private BigDecimal[] predictScore = new BigDecimal[3];
	
	//查询分数
	private void getScore(String date_half_hour) throws ParseException, SQLException
	{
		gid1_score.clear();
		gid2_score.clear();
		gid3_score.clear();
		query="select score from predict where ddate>'"+da.getRevisedDateH(date_half_hour, -24)+"' and ddate<='"+date_half_hour+"'";
	   rs = se.getSqlResult(query);
	   int row = 0;
	   while(rs.next())
	   {
		   if(row%3==0)
		    {
			   gid1_score.add(rs.getDouble(1));
		    }
		   else if(row%3==1)
		    {
			   gid2_score.add(rs.getDouble(1));
		    }
		   else
		    {
			   gid3_score.add(rs.getDouble(1));
		    }
		   row++;
	   }
	   rs.close();
	   se.closeConn();
	}
	
	//查询AQI
	private void getAqi(String date_hour) throws SQLException
	{
	   //String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		query="select aqi from aqi_buc_stime where stime='"+date_hour+"'";
		rs = se.getSqlResult(query);
		while(rs.next())
		{
		    aqiResult= rs.getInt(1);
		}
		rs.close();
		se.closeConn();
	}
	//查询降雨量
	private void getRainfall(String date_hour) throws SQLException
	{
		//String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		query="select rainfall from weather where ddate='"+date_hour+"' and (site='徐家汇' or site='宝山') order by site desc";
		rs = se.getSqlResult(query);
		int row = 0;
		while(rs.next())
		{
		    rainResult[row]= rs.getFloat(1);
		    row++;
		}
		rs.close();
		se.closeConn();
	}
	
	//查询地铁信息
	//metroResult第一列：出站，第二列：入站
	private void getMetro(String date_min) throws Exception
	{
		//String date_min = new DateAdjuster().getDate();  //获取当前日期，精确到分钟
		//String[] para = date_min.split(" ");
		String rdate_min = da.getRevisedDate(date_min, -60);
		//String[] rpara = rdate_min.split(" ");
	
		//要查60分钟之内的总人数
		query="select sum(case when price>0 then 1 else 0 end) sum_in,sum(case when price=0 then 1 else 0 end) sum_out from transcard_core right join ggid on (ddate>'"+rdate_min+"' and ddate<='"+date_min+"' and transcard_core.gid=ggid.gid) group by ggid.gid order by ggid.gid";
		//System.out.println(query);
		rs = se.getSqlResult(query);
		int row = 0;
		while(rs.next())
		{
			metroResult[row][0] = rs.getInt(1);
			metroResult[row][1] = rs.getInt(2);
			row++;
		}
		rs.close();
		se.closeConn();
	}
	
	//查询强生数据
	//taxiResult第一列：下车，第二列：上车
	private void getTaxi(String date_min) throws Exception
	{
		//String date_min = new DateAdjuster().getDate();  //获取当前日期，精确到分钟	
		String rdate_min = da.getRevisedDate(date_min, -60);
	
		//要查60分钟之内的总人数
		query="select sum(case when empty=0 then 1 else 0 end),sum(case when empty=1 then 1 else 0 end) from taxi_core right join ggid on (gps_time>'"+rdate_min+"' and gps_time<='"+date_min+"' and taxi_core.gid=ggid.gid) group by ggid.gid order by ggid.gid";
		//System.out.println(query);
		rs = se.getSqlResult(query);
		int row = 0;
		while(rs.next())
		{
			taxiResult[row][0] = rs.getInt(1);
			taxiResult[row][1] = rs.getInt(2);
			row++;
		}
		rs.close();
		se.closeConn();
	}
	
	//查询事故信息
	private void getAccident(String date_min) throws ParseException,SQLException
	{
		//String date_min = new DateAdjuster().getDate();  //获取当前日期，精确到分钟	
		String rdate_min = da.getRevisedDate(date_min, -15);
		query="select location,lo,la,gid from accident_core where ddate>'"+rdate_min+"' and ddate<='"+date_min+"'";
		rs = se.getSqlResult(query);
		Object[] result = new Object[4];
		accResult.clear();
		accCount[0]=accCount[1]=accCount[2]=0;    //清零
		while (rs.next())
		{	
			result[0] = rs.getInt(4);
			result[1] = rs.getString(1);
			result[2] = rs.getFloat(2);
			result[3] = rs.getFloat(3);
			accResult.add(result);
		}	
		rs.close();
		se.closeConn();
	}
	//查询汇纳商圈数据，返回人数
	private void getMall(String date_hour) throws SQLException
	{
		//String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		//String[] para=date_hour.split(" ");
		query="select num from mall_core where ddate='"+date_hour+"' order by mname";
		rs = se.getSqlResult(query);
	   int row = 0;
		while(rs.next())
		{
		    mallResult[row]= rs.getInt(1);
		    row++;
		}
		rs.close();
		se.closeConn();
	}
	
	//联通数据
	private void getUnicom (String date_hour) throws SQLException
	{
		//String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
		//String[] para=date_hour.split(" ");
		query="select count(*) from unicom_core where ddate='"+date_hour+"' group by gid order by gid";
		rs = se.getSqlResult(query);
	    int row = 0;
		while(rs.next())
		{
		    unicomResult[row]= rs.getInt(1);
		    row++;
		}
		rs.close();
		se.closeConn();
	}
	
	/*以下是累计一小时的数据*/
	private void min_Max_Metro(String date_min) throws ParseException, SQLException
	{
		//String date_min = new DateAdjuster().getDate();  //获取当前日期，精确到分钟
		//da.getRevisedDateH(date_min, -1);
		//String[] rpara = rdate_min.split(" ");
	
		query="select max(summ) from(select gid,count(*) summ,case "+
				"when ddate>'"+da.getRevisedDateH(date_min, -1)+"' and ddate<='"+date_min+"' then 1 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -2)+"' and ddate<='"+da.getRevisedDateH(date_min, -1)+"' then 2 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -3)+"' and ddate<='"+da.getRevisedDateH(date_min, -2)+"' then 3 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -4)+"' and ddate<='"+da.getRevisedDateH(date_min, -3)+"' then 4 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -5)+"' and ddate<='"+da.getRevisedDateH(date_min, -4)+"' then 5 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -6)+"' and ddate<='"+da.getRevisedDateH(date_min, -5)+"' then 6 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -7)+"' and ddate<='"+da.getRevisedDateH(date_min, -6)+"' then 7 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -8)+"' and ddate<='"+da.getRevisedDateH(date_min, -7)+"' then 8 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -9)+"' and ddate<='"+da.getRevisedDateH(date_min, -8)+"' then 9 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -10)+"' and ddate<='"+da.getRevisedDateH(date_min, -9)+"' then 10 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -11)+"' and ddate<='"+da.getRevisedDateH(date_min, -10)+"' then 11 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -12)+"' and ddate<='"+da.getRevisedDateH(date_min, -11)+"' then 12 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -13)+"' and ddate<='"+da.getRevisedDateH(date_min, -12)+"' then 13 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -14)+"' and ddate<='"+da.getRevisedDateH(date_min, -13)+"' then 14 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -15)+"' and ddate<='"+da.getRevisedDateH(date_min, -14)+"' then 15 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -16)+"' and ddate<='"+da.getRevisedDateH(date_min, -15)+"' then 16 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -17)+"' and ddate<='"+da.getRevisedDateH(date_min, -16)+"' then 17 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -18)+"' and ddate<='"+da.getRevisedDateH(date_min, -17)+"' then 18 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -19)+"' and ddate<='"+da.getRevisedDateH(date_min, -18)+"' then 19 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -20)+"' and ddate<='"+da.getRevisedDateH(date_min, -19)+"' then 20 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -21)+"' and ddate<='"+da.getRevisedDateH(date_min, -20)+"' then 21 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -22)+"' and ddate<='"+da.getRevisedDateH(date_min, -21)+"' then 22 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -23)+"' and ddate<='"+da.getRevisedDateH(date_min, -22)+"' then 23 "+
				"when ddate>'"+da.getRevisedDateH(date_min, -24)+"' and ddate<='"+da.getRevisedDateH(date_min, -23)+"' then 24 "+
				"else 25 end tj "+
				"from transcard_core where ddate>'"+da.getRevisedDateH(date_min, -24)+"' and ddate<='"+date_min+"' and gid<4 group by tj,gid) group by "+"gid order by gid";
		//System.out.println(query);
		rs = se.getSqlResult(query);
		int row = 0;
		while(rs.next())
		{
			hmetroResult[row] = rs.getInt(1);  //保存最大值
			row++;
		}
		rs.close();
		se.closeConn();
	}
	
	private void min_Max_Taxi(String date_min) throws ParseException, SQLException
	{
		//String date_min = new DateAdjuster().getDate();  //获取当前日期，精确到分钟	
		query="select case "+ 
				"when min(summ) is null then 0 "+
				"else min(summ) end, "+
				"max(summ) from(select gid,count(*) summ,case "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -1)+"' and gps_time<='"+date_min+"' then 1 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -2)+"' and gps_time<='"+da.getRevisedDateH(date_min, -1)+"' then 2 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -3)+"' and gps_time<='"+da.getRevisedDateH(date_min, -2)+"' then 3 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -4)+"' and gps_time<='"+da.getRevisedDateH(date_min, -3)+"' then 4 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -5)+"' and gps_time<='"+da.getRevisedDateH(date_min, -4)+"' then 5 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -6)+"' and gps_time<='"+da.getRevisedDateH(date_min, -5)+"' then 6 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -7)+"' and gps_time<='"+da.getRevisedDateH(date_min, -6)+"' then 7 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -8)+"' and gps_time<='"+da.getRevisedDateH(date_min, -7)+"' then 8 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -9)+"' and gps_time<='"+da.getRevisedDateH(date_min, -8)+"' then 9 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -10)+"' and gps_time<='"+da.getRevisedDateH(date_min, -9)+"' then 10 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -11)+"' and gps_time<='"+da.getRevisedDateH(date_min, -10)+"' then 11 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -12)+"' and gps_time<='"+da.getRevisedDateH(date_min, -11)+"' then 12 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -13)+"' and gps_time<='"+da.getRevisedDateH(date_min, -12)+"' then 13 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -14)+"' and gps_time<='"+da.getRevisedDateH(date_min, -13)+"' then 14 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -15)+"' and gps_time<='"+da.getRevisedDateH(date_min, -14)+"' then 15 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -16)+"' and gps_time<='"+da.getRevisedDateH(date_min, -15)+"' then 16 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -17)+"' and gps_time<='"+da.getRevisedDateH(date_min, -16)+"' then 17 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -18)+"' and gps_time<='"+da.getRevisedDateH(date_min, -17)+"' then 18 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -19)+"' and gps_time<='"+da.getRevisedDateH(date_min, -18)+"' then 19 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -20)+"' and gps_time<='"+da.getRevisedDateH(date_min, -19)+"' then 20 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -21)+"' and gps_time<='"+da.getRevisedDateH(date_min, -20)+"' then 21 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -22)+"' and gps_time<='"+da.getRevisedDateH(date_min, -21)+"' then 22 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -23)+"' and gps_time<='"+da.getRevisedDateH(date_min, -22)+"' then 23 "+
				"when gps_time>'"+da.getRevisedDateH(date_min, -24)+"' and gps_time<='"+da.getRevisedDateH(date_min, -23)+"' then 24 "+
				"else 25 end tj "+
				"from taxi_core where gps_time>'"+da.getRevisedDateH(date_min, -24)+"' and gps_time<='"+date_min+"' and taxi_core.gid<4 group by tj,gid) group by gid order by gid";
		rs = se.getSqlResult(query);
		int row = 0;
		while(rs.next())
		{
			htaxiResult[row][0] = rs.getInt(1);
			htaxiResult[row][1] = rs.getInt(2);
			row++;
		}
		rs.close();
		se.closeConn();
	}
	
	private void min_Max_Mall(String date_hour) throws ParseException, SQLException
	{
		query="select min(summ),max(summ) from "+
				"(select ddate,sum(num) summ,case "+
				"when mname like 'N%' then 1 "+
				"when mname like 'H%' then 2 "+
				"when mname like 'W%' then 3 "+
				"else 4 end gid "+
				"from mall_core where ddate>'"+da.getRevisedDateH(date_hour, -24)+"' and ddate<='"+date_hour+"' group by ddate,gid) group by gid order by gid";
		rs = se.getSqlResult(query);
	   int row = 0;
		while(rs.next())
		{
		    hmallResult[row][0]= rs.getInt(1);
		    hmallResult[row][1]= rs.getInt(2);
		    row++;
		}
		rs.close();
		se.closeConn();
	}
	
	private void min_Max_Unicom(String date_hour) throws ParseException, SQLException
	{
		query="select case "+
				"when min(summ) is null then 0 "+
				"else min(summ) end, "+
				"max(summ) from(select gid,count(*) summ "+
				"from unicom_core where ddate>'"+da.getRevisedDateH(date_hour, -24)+"' and ddate<='"+date_hour+"' and gid<4 group by ddate,gid) group by gid order by gid";

		rs = se.getSqlResult(query);
	   int row = 0;
		while(rs.next())
		{
		    hunicomResult[row][0]= rs.getInt(1);
		    hunicomResult[row][1]= rs.getInt(2);
		    row++;
		}
		rs.close();
		se.closeConn();
	}
	
	
	//每10分钟执行的函数添加到此处
	public void runMinute(String date_min) throws Exception
	{
		getMetro(date_min);
		getTaxi(date_min);
		getAccident(date_min);
		if(!date_min.contains("2016-03-17") && !date_min.contains("2016-03-18"))
		{
			min_Max_Metro(date_min);
			min_Max_Taxi(date_min);	
		}
			
	}
	
	//每半小时执行的函数添加到此处
	public void runHalfHour(String date_half_hour) throws Exception
	{
		getScore(date_half_hour);
	}

	
	//每小时执行的函数添加到处处
	public void runHour(String date_hour) throws SQLException, ParseException
	{
		getMall(date_hour);
		getRainfall(date_hour);
		getAqi(date_hour);
		getUnicom(date_hour);
		if(!date_hour.contains("2016-03-17") && !date_hour.contains("2016-03-18"))
		{
			min_Max_Mall(date_hour);
			min_Max_Unicom(date_hour);		
		}
		
	}
	
	
	//保存每10分钟查询结果
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
			int i,j;
			fl=fo.getChannel().tryLock();   //“写”上锁
			if(fl!=null)
		    {
				 System.out.print("Locked File Minute. ");
			}
			for(i=0;i<20;i++)  //20行
			{						
				bw.write(metroResult[i][0]+" ");  //地铁
			   bw.write(metroResult[i][1]+" ");  //地铁
			   bw.write(taxiResult[i][0]+" ");  //出租
			   bw.write(taxiResult[i][1]+" ");  //出租
			   for(j=0;j<accResult.size();j++)
			    {
				   if((Integer)accResult.get(j)[0]==i+1)  //道路事故
				    {
					   if(i<3)
					    {
						   accCount[i]++;
					    }
					   bw.write((String)accResult.get(j)[1]+" ");  //事故路名
					   BigDecimal b = new BigDecimal((accResult.get(j)[2]).toString());
					   bw.write(b.setScale(6, BigDecimal.ROUND_DOWN)+" ");  //事故地点经度
					   b = new BigDecimal((accResult.get(j)[3]).toString());
					   bw.write(b.setScale(6, BigDecimal.ROUND_DOWN)+" ");  //事故地点纬度
				    }	
			    }
			   bw.write("\n");
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
	
    //保存每10分钟统计的1小时累计最值
	public void saveAccuMinuteData()
	{
		FileOutputStream fo=null;;
		OutputStreamWriter ow=null;
		BufferedWriter bw=null;
		FileLock fl = null;
		try {
			fo = new FileOutputStream("/root/soda/accu_dataMinute.txt");
		   ow = new OutputStreamWriter(fo);
			bw = new BufferedWriter(ow);
			int i;
			fl=fo.getChannel().tryLock();   //“写”上锁
			if(fl!=null)
		    {
				 System.out.print("Locked File Accu_Minute. ");
			}
			for(i=0;i<3;i++)
			{
				bw.write(hmetroResult[i]+" ");  //地铁_max
				bw.write(htaxiResult[i][0]+" ");  //taxi_min
				bw.write(htaxiResult[i][1]+" ");  //taxi_max
				bw.write(accCount[i]+"\n");  //accident count
			}
		}
		catch (Exception e) {
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
				  System.out.println("Released Lock Accu_Minute.");
		    	} catch (IOException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
			    }   
		   }
	   }
	}
	
	//保存每半小时查询结果
			public void saveHalfHourData(String date_half_hour) throws ParseException
			{
				//FileWriter fw = new FileWriter("/root/soda/dataMinute.txt",false);
				FileOutputStream fo=null;;
				OutputStreamWriter ow=null;
				BufferedWriter bw=null;
				FileLock fl = null;
				int i;
				double[] temp = new double[gid1_score.size()];
				//计算预测值
				for(i=0;i<gid1_score.size();i++)
				{
					temp[i]= gid1_score.get(i);
				}
			   predictScore[0]=hw.forecast(temp, 0.6884199, 0.8652352, 1,1, false);
			   for(i=0;i<gid2_score.size();i++)
				{
					temp[i]= gid2_score.get(i);
				}
			   predictScore[1]=hw.forecast(temp, 0.6884199, 0.8652352, 1,1, false);
			   for(i=0;i<gid3_score.size();i++)
				{
					temp[i]= gid3_score.get(i);
				}
			   predictScore[2]=hw.forecast(temp, 0.6884199, 0.8652352, 1,1, false);
				try {
					fo = new FileOutputStream("/root/soda/dataHalfHour.txt");
				   ow = new OutputStreamWriter(fo);
					bw = new BufferedWriter(ow);
					fl=fo.getChannel().tryLock();   //“写”上锁
					if(fl!=null)
				    {
				       System.out.print("Locked File Half Hour. ");
					}
				   for(i=0;i<3;i++)
				    {
				 	    bw.write(new DateAdjuster().getRevisedDate(date_half_hour, 30)+" ");
				 	    bw.write((i+1)+" ");
						 bw.write(predictScore[i].setScale(2, BigDecimal.ROUND_DOWN)+"\n"); 
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
						  System.out.println("Released Lock Half Hour.");
				    	} catch (IOException e) {
						    // TODO Auto-generated catch block
						    e.printStackTrace();
					    }   
				   }
			   }
		  }
	
	

	//保存每小时查询结果
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
					 System.out.print("Locked File Hour. ");
				}
				for(i=0;i<20;i++)  //前20项
				{
					bw.write(unicomResult[i]+" ");   //联通数据
				}
				for(i=0;i<8;i++)
				{
					bw.write(mallResult[i]+" ");   //商圈数据
				}
				BigDecimal b = new BigDecimal(String.valueOf(rainResult[0]));
				bw.write(b.setScale(1, BigDecimal.ROUND_DOWN)+" ");  //徐家汇降雨量
				b = new BigDecimal(String.valueOf(rainResult[1]));
				bw.write(b.setScale(1, BigDecimal.ROUND_DOWN)+" ");  //宝山降雨量
				bw.write(aqiResult+"\n");   //AQI数据
				
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
		 //保存1小时统计的1小时累计最值
		public void saveAccuHourData()
		{
			FileOutputStream fo=null;;
			OutputStreamWriter ow=null;
			BufferedWriter bw=null;
			FileLock fl = null;
			try {
				fo = new FileOutputStream("/root/soda/accu_dataHour.txt");
			   ow = new OutputStreamWriter(fo);
				bw = new BufferedWriter(ow);
				int i;
				fl=fo.getChannel().tryLock();   //“写”上锁
				if(fl!=null)
			    {
					 System.out.print("Locked File Accu_Hour. ");
				}
				for(i=0;i<3;i++)
				{
					bw.write(hunicomResult[i][0]+" ");  //unicom_min
					bw.write(hunicomResult[i][1]+" ");  //unicom_max
					bw.write(hmallResult[i][0]+" ");  //mall_min
					bw.write(hmallResult[i][1]+"\n");  //mall_max
				}
			}
			catch (Exception e) {
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
					  System.out.println("Released Lock Accu_Hour.");
			    	} catch (IOException e) {
					    // TODO Auto-generated catch block
					    e.printStackTrace();
				    }   
			   }
		   }
	  }	
}
