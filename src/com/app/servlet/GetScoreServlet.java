package com.app.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.app.transfer.DateAdjuster;
import com.app.transfer.Transfer;

/**
 * Servlet implementation class GetDegreeServlet
 */
@WebServlet("/GetScoreServlet")
public class GetScoreServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetScoreServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json"); 
      response.setCharacterEncoding("UTF-8"); 
      Transfer t = new Transfer();
      PrintWriter out = response.getWriter();
		FileReader fr = new FileReader("/root/soda/dataHour.txt");
		BufferedReader br = new BufferedReader(fr);
		FileReader amfr = new FileReader("/root/soda/accu_dataMinute.txt");
		BufferedReader ambr = new BufferedReader(amfr);
		FileReader ahfr = new FileReader("/root/soda/accu_dataHour.txt");
		BufferedReader ahbr = new BufferedReader(ahfr);
		int[] xishu = {16,24,32,8,7,8,5};
		String[] pname ={"metro","taxi","mall","unicom","accident","rainfall","aqi"};
		JSONArray ja = new JSONArray();  //最终结果
		String hresult,mresult,ahresult,amresut;
		int i;
		String hstring[] = null;  //保存小时信息
		String mstring[] = null;
		String amstring[] = null;
		String ahstring[] = null;
		float[] para = new float[7];	
		
		if((hresult=br.readLine())!=null)
		{
			hstring = hresult.split(" ");   //20+8+2+1
		}
		br.close();
		fr.close();
		fr = new FileReader("/root/soda/dataMinute.txt");
		br = new BufferedReader(fr);
		int row = 0;
		while((mresult=br.readLine())!=null && (amresut=ambr.readLine())!=null && (ahresult=ahbr.readLine())!=null)
		{
			mstring=mresult.split(" ");   //>=4
			amstring=amresut.split(" ");  //4
			ahstring=ahresult.split(" "); //4
			para[0]=(Float.valueOf(mstring[0])+Integer.valueOf(mstring[1]))/Integer.valueOf(amstring[0]);  //metro
			DateAdjuster da = new DateAdjuster();
			if(da.getDateHour().contains("2016-03-17"))  //强生出租车缺少3月17日的数据
			{
				amstring[1]="0";
			}
			para[1]=(Float.valueOf(mstring[2])+Integer.valueOf(mstring[3])-Integer.valueOf(amstring[1]))/(Integer.valueOf(amstring[2])-Integer.valueOf(amstring[1])); //taxi
			switch(row)  //mall
			{
			    case 0: para[2]=(Float.valueOf(hstring[22])+Integer.valueOf(hstring[23])+Integer.valueOf(hstring[24])-Integer.valueOf(ahstring[2]))/(Integer.valueOf(ahstring[3])-Integer.valueOf(ahstring[2]));break;
			    case 1: para[2]=(Float.valueOf(hstring[20])+Integer.valueOf(hstring[21])-Integer.valueOf(ahstring[2]))/(Integer.valueOf(ahstring[3])-Integer.valueOf(ahstring[2]));break;
			    default: para[2]=(Float.valueOf(hstring[25])+Integer.valueOf(hstring[26])+Integer.valueOf(hstring[27])-Integer.valueOf(ahstring[2]))/(Integer.valueOf(ahstring[3])-Integer.valueOf(ahstring[2]));
			}
			para[3]=(Float.valueOf(hstring[row])-Integer.valueOf(ahstring[0]))/(Integer.valueOf(ahstring[1])-Integer.valueOf(ahstring[0]));  //unicom
			para[4]=t.accFormal(Integer.valueOf(amstring[3]));  //accident
			if(row==2)  //宝山气象站
			{
				para[5]=t.rainFormal(Float.valueOf(hstring[29]));   //rainfall
			}
			else   //徐家汇气象站
			{
				para[5]=t.rainFormal(Float.valueOf(hstring[28]));   //rainfall
			}
			para[6]=t.aqiFormal(Integer.valueOf(hstring[30]));
			//System.out.println(t1+" "+t2);
			float score = 0f;
			for(i=0;i<7;i++)
			{
				score += xishu[i]*para[i];
			}
			JSONObject jo = new JSONObject();
			JSONObject jo_tmp = new JSONObject();
			try {
				jo.put("gid", row+1);
				for(i=0;i<7;i++)
				{
					BigDecimal b = new BigDecimal(para[i]);
					b = b.setScale(2, BigDecimal.ROUND_HALF_UP);		
					jo_tmp.put(pname[i], b);
				}
				jo.put("detail", jo_tmp);
				BigDecimal b = new BigDecimal(score);
				b = b.setScale(2, BigDecimal.ROUND_HALF_UP);	
				jo.put("score", b);
				ja.put(jo);
			}
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			row++;
		}
		br.close();  //差点忘了关闭文件
	   fr.close();
	   ambr.close();
	   amfr.close();
	   ahbr.close();
	   ahfr.close();
	   out.print(ja.toString());
      out.flush();
		out.close();
		
		
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
