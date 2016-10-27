package com.app.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class GetHistoryServlet
 */
@WebServlet("/GetHistoryServlet")
public class GetHistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetHistoryServlet() {
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
		JSONArray ja_final = new JSONArray();  //最终结果
		FileReader fr = new FileReader("/root/soda/dataHistory.txt");
		BufferedReader br = new BufferedReader(fr);
		String line;
		int i;
		int row = 0;
		while((line=br.readLine())!=null)
		{
			String[] arrayLine = line.split(" ");
			JSONArray ja = new JSONArray();
			JSONObject jo = new JSONObject();
			for(i=0;i<arrayLine.length;i++)
			{
				BigDecimal b = new BigDecimal(arrayLine[i]);
    			ja.put(b.setScale(2, BigDecimal.ROUND_DOWN));
			}
			try {
				jo.put("gid", row+1);
				jo.put("history", ja);
				ja_final.put(jo);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			row++;	
		}
		br.close();
		fr.close();
		out.print(ja_final.toString());
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
