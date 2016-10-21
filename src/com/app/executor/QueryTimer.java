package com.app.executor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.app.data.DataProc;
import com.app.transfer.DateAdjuster;

//定时执行类，分为立即执行、每分钟执行、每整点执行
public class QueryTimer {

    private DataProc dp = new DataProc();
    private Calendar calendar = Calendar.getInstance();

    //如果当前数据为空，则立刻运行此函数
    public void queryNow() throws Exception
    {
    	String date_min = new DateAdjuster().getDate();
    	String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
    	String date_half_hour = new DateAdjuster().getDateHalfHour(date_min);
    	dp.runHour(date_hour);
    	dp.saveHourData();
    	dp.saveAccuHourData();
    	dp.runMinute(date_min);
    	dp.saveMinuteData();
    	dp.saveAccuMinuteData();
    	dp.runHalfHour(date_half_hour);
    	dp.saveHalfHourData(date_half_hour);
    }
    
    //每5分钟查询一次
	public void queryEvery5Mintue() throws ParseException
	{
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 5);
		calendar.set(Calendar.SECOND, 10);
		Date time_start = calendar.getTime();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String date_min = new DateAdjuster().getDate();
					if(date_min.compareTo("2016-03-17 00:00")<0 || date_min.compareTo("2016-03-18 23:59")>1)
					{
						dp.runMinute(date_min);
						dp.saveMinuteData();
						dp.saveAccuMinuteData();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time_start, 1000*60*5);
	}
	
	
	//每半小时查询一次
		public void queryEveryHalfHour() throws ParseException
		{
			Date cur_date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");			
			calendar.setTime(cur_date);
			calendar.add(Calendar.MINUTE, new DateAdjuster().getLatency(dateFormat.format(cur_date)));
			calendar.set(Calendar.SECOND, 0);
			Date time_start = calendar.getTime();
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						String date_min = new DateAdjuster().getDate();
						if(date_min.compareTo("2016-03-17 00:00")<0 || date_min.compareTo("2016-03-18 23:59")>1)
						{
							DateAdjuster da = new DateAdjuster();					
							String date_half_hour = da.getDateHalfHour(date_min);
	                  dp.runHalfHour(date_half_hour);
	                  dp.saveHalfHourData(date_half_hour);
						}		
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, time_start, 1000*60*30);
		}
	
	
	//每小时查询一次
	public void queryEveryHour() throws ParseException
	{
		calendar.setTime(new Date());
		calendar.add(Calendar.HOUR, 1);  //会进位
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date time_start= calendar.getTime();
		Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
					if(date_hour.compareTo("2016-03-17 00:00")<0 || date_hour.compareTo("2016-03-18 23:59")>1)
					{
						dp.runHour(date_hour);
						dp.saveHourData();
						dp.saveAccuHourData();
					}		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time_start, 1000*60*60);
		
	}
}
