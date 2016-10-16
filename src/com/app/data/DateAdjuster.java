package com.app.data;

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
		return dateFormat.format(calendar.getTime());	
	}
}
