package com.app.transfer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//日期设定类，查询语句中的年月固定为2016年3月
public class DateAdjuster {
	
	//返回分钟粒度的时间
	public String getDate()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.YEAR,2016);
		calendar.set(Calendar.MONTH, 2);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(dateFormat.format(calendar.getTime()).contains("2016-03-01"))
		{
			calendar.add(Calendar.DAY_OF_MONTH, 30);
		}	
		return dateFormat.format(calendar.getTime());
	}
	
	//将给定时间加快min分钟
	public String getRevisedDate(String date,int min) throws ParseException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateFormat.parse(date));
		calendar.add(Calendar.MINUTE, min);
		return dateFormat.format(calendar.getTime());	
	}
	
	//返回最近的半小时时间
	public String getDateHalfHour(String date) throws ParseException
	{
		String[] time = date.split(":");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateFormat.parse(date));
		if(Integer.parseInt(time[1])>0 && Integer.parseInt(time[1])<30)
		{	
			calendar.set(Calendar.MINUTE,0);
			return dateFormat.format(calendar.getTime());	
		}
		else if(Integer.parseInt(time[1])>30 && Integer.parseInt(time[1])<=59)
		{
			calendar.set(Calendar.MINUTE,30);			
		}
		return dateFormat.format(calendar.getTime());	
	}
	
	//还需多久执行一次半小时查询
	public int getLatency(String date) throws ParseException
	{
		String[] time = date.split(":");
		if(Integer.parseInt(time[1])>0 && Integer.parseInt(time[1])<30)
		{	
			return 30-Integer.parseInt(time[1]);	
		}
		else if(Integer.parseInt(time[1])>30 && Integer.parseInt(time[1])<=59)
		{		
			return 60-Integer.parseInt(time[1]);	
		}
		else
		{
			return 0;	
		}		
	}
	
	public int getLatencyM(String date) throws ParseException
	{
		String[] time = date.split(":");
		return 10*(Integer.parseInt(time[1])/10)+9-Integer.parseInt(time[1]);
	}
	
	
	//将给定时间加快h小时
		public String getRevisedDateH(String date,int h) throws ParseException
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateFormat.parse(date));
			calendar.add(Calendar.HOUR, h);
			return dateFormat.format(calendar.getTime());	
		}
	
	
	//返回小时粒度的时间
	public String getDateHour()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.YEAR,2016);
		calendar.set(Calendar.MONTH, 2);
		calendar.set(Calendar.MINUTE,0);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(dateFormat.format(calendar.getTime()).contains("2016-03-01"))
		{
			calendar.add(Calendar.DAY_OF_MONTH, 30);
		}	
		return dateFormat.format(calendar.getTime());	
	}
}
