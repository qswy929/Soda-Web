package com.app.executor;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.app.data.DataProc;

//定时执行类，分为立即执行、每分钟执行、每整点执行
public class QueryTimer {

    private DataProc dp = new DataProc();
    private Calendar calendar = Calendar.getInstance();

    //如果当前数据为空，则立刻运行此函数
    public void QueryNow() throws Exception
    {
    	dp.runMinute();
    	dp.saveMinuteData();
    	dp.runHour();
    	dp.saveHourData();
    }
    
    //每5分钟查询一次
	public void QueryEvery5Mintue() throws ParseException
	{
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		Date time_start = calendar.getTime();
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					dp.runMinute();
					dp.saveMinuteData();

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
					dp.runHour();
					dp.saveHourData();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, time_start, 1000*60*60);
		
	}
}
