package com.app.executor;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.app.data.DataProc;
import com.app.data.DateAdjuster;

//定时执行类，分为立即执行、每分钟执行、每整点执行
public class QueryTimer {

    private DataProc dp = new DataProc();
    private Calendar calendar = Calendar.getInstance();

    //如果当前数据为空，则立刻运行此函数
    public void QueryNow() throws Exception
    {
    	String date_min = new DateAdjuster().getDate();
    	String date_hour = new DateAdjuster().getDateHour();  //获取当前日期，精确到小时
    	dp.runHour(date_hour);
    	dp.saveHourData();
    	dp.saveAccuHourData();
    	dp.runMinute(date_min);
    	dp.saveMinuteData();
    	dp.saveAccuMinuteData();
    }
    
    //每5分钟查询一次
	public void QueryEvery5Mintue() throws ParseException
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
					dp.runMinute(date_min);
					dp.saveMinuteData();
					dp.saveAccuMinuteData();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time_start, 1000*60*5);
	}
	
	//每小时查询一次
	public void QueryEveryHour() throws ParseException
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
					dp.runHour(date_hour);
					dp.saveHourData();
					dp.saveAccuHourData();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time_start, 1000*60*60);
		
	}
}
