package com.app.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class GetDataServlet
 */
@WebServlet("/GetDataServlet")
public class GetDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetDataServlet() {
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
      PrintWriter out = response.getWriter();
		FileReader fr = new FileReader("/root/soda/dataHour.txt");
		BufferedReader br = new BufferedReader(fr);
		String hresult;
		String hstring[] = null;  //保存小时信息
		if((hresult=br.readLine())!=null)
		{
			hstring = hresult.split(" ");   //20+8+2+1
		}
		br.close();
		fr.close();		
		fr = new FileReader("/root/soda/dataMinute.txt");
		br = new BufferedReader(fr);
      
      int row=0;
      String mresult;
      Object[] temp_line;
      JSONObject jo_result = new JSONObject();
      JSONArray ja = new JSONArray();
      ArrayList<JSONObject> acci_arr = new ArrayList();
      JSONObject final_result = new JSONObject();
      while((mresult=br.readLine())!=null)
        {
    	  temp_line = mresult.split(" ");
    	  JSONObject jo = new JSONObject();
    	  JSONObject jo_line = new JSONObject();	      	        
    	  try {
    		  if(temp_line.length>4)  //有事故信息
       	       {
       		  int i;
       		  for(i=0;i<(temp_line.length-4)/3;i++)
       		      {
       			  JSONObject acc_jo=new JSONObject();
       			  acc_jo.put("gid", row+1);
         		  acc_jo.put("location", temp_line[4]);
         		  BigDecimal b = new BigDecimal(temp_line[5].toString());
         		  acc_jo.put("lo", b.setScale(6, BigDecimal.ROUND_DOWN));
         		  b= new BigDecimal(temp_line[6].toString());
         		  acc_jo.put("la", b.setScale(6, BigDecimal.ROUND_DOWN));
         		  acci_arr.add(acc_jo);
       		      }			  
       	       }
    		   //地铁
        	  jo.put("in", Integer.parseInt(temp_line[0].toString()));
        	  jo.put("out", Integer.parseInt(temp_line[1].toString()));
        	  jo_line.put("metro", jo);
        	   //出租车
        	  jo = new JSONObject();
        	  jo.put("in", Integer.parseInt(temp_line[2].toString()));
        	  jo.put("out", Integer.parseInt(temp_line[3].toString()));
        	  jo_line.put("taxi", jo);
        	  //gid
        	  jo_line.put("gid",row+1);
        	  //联通
        	  jo_line.put("unicom",Integer.parseInt(hstring[row]));
        	  //汇纳商圈
        	  switch (row)
        	  {
        	      case 0 :jo = new JSONObject();
        	      jo.put("N1", Integer.parseInt(hstring[22].toString()));
        	      jo.put("N2", Integer.parseInt(hstring[23].toString()));
        	      jo.put("N3", Integer.parseInt(hstring[24].toString()));
        	      jo_line.put("mall", jo);break;
        	      case 1 :jo = new JSONObject();
        	      jo.put("H1", Integer.parseInt(hstring[20].toString()));
        	      jo.put("H2", Integer.parseInt(hstring[21].toString()));
        	      jo_line.put("mall", jo);break;
        	      case 2 :jo = new JSONObject();
        	      jo.put("W1", Integer.parseInt(hstring[25].toString()));
        	      jo.put("W2", Integer.parseInt(hstring[26].toString()));
        	      jo.put("W3", Integer.parseInt(hstring[27].toString()));
        	      jo_line.put("mall", jo);break;
        	      default:;
        	  }
        	  
               //人流信息
        	  ja.put(jo_line);
	     } catch (Exception e) {
			      // TODO: handle exception
			      e.printStackTrace();
		   }
    	  row++;
       }
      br.close();  //差点忘了关闭文件
      fr.close();
     try {
			final_result.put("people", ja);
			ja = new JSONArray();
			for (JSONObject j : acci_arr)
			{
				ja.put(j);
			}
			final_result.put("accident", ja);
			JSONObject rain_jo = new JSONObject();
			BigDecimal b = new BigDecimal(hstring[28]);
   		rain_jo.put("徐家汇", b.setScale(1, BigDecimal.ROUND_DOWN));
   		b = new BigDecimal(hstring[29]);
   		rain_jo.put("宝山", b.setScale(1, BigDecimal.ROUND_DOWN));
			final_result.put("rainfall", rain_jo);
			final_result.put("aqi", Integer.parseInt(hstring[30]));		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      finally {
          out.print(final_result.toString());
          out.flush();
		    out.close();
        }
      //System.out.println(final_result);
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
