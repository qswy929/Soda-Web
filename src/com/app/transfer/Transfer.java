package com.app.transfer;


public class Transfer {

	//归一化类
	public float rainFormal(float f)
	{
		if(f<5)
		{
			return 1;
		}
		else if(f>=5 && f<15)
		{
			return 0.75f;
		}
		else if(f>=15 && f<30)
		{
			return 0.5f;
		}
		else
		{
			return (float)0.25;
		}
	}
	
	public float aqiFormal(int a)
	{
		if(a<=100)
		{
			return 1;
		}
		else if(a>100 && a<=150)
		{
			return 0.8f;
		}
		else if(a>150 && a<=200)
		{
			return 0.7f;
		}
		else if(a>200 && a<=300)
		{
			return 0.6f;
		}
		else
		{
			return 0.4f;
		}	
	}
	public float accFormal(int num)
	{
		if(num==0)
		{
			return 1;
		}
		else if (num==1)
		{
			return 0.8f;
		}
		else
		{
			return 0.5f;
		}
	}
}
