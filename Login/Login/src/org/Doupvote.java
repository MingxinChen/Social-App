package org;

import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.*;

/**
 * Servlet implementation class TestTomcat
 */
public class Doupvote extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Doupvote() {
		super();
	}
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
		this.doPost(req,resp);
	}
	@Override 
	protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
		if(req == null) {
			return;
		}
		req.setCharacterEncoding("UTF-8");
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));//使用字符流读取客户端发过来的数据
		String line = null;
		StringBuffer s = new StringBuffer();
		while ((line = br.readLine()) != null) {
			s.append(line);
		}
		br.close();
		JSONObject json = JSONObject.fromObject(s.toString());//转化为jSONObject对象

		String user = json.getString("un");		
		Double mes_id = json.getDouble("mes_id");
		String command = "";
		String mes="";
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		
		Connection con;
		Statement sql; 
		command = "Insert into Upvote(mes_id,votor) values(%d,\"%s\")";
		String command1 = String.format(command, mes_id,user);
		try{ 
			Class.forName("com.mysql.jdbc.Driver"); 
		}catch(ClassNotFoundException e){ 
			mes += e.toString(); 
			JSONObject rjson = new JSONObject();
			rjson.put("mes", mes);
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
		}    
		try {  
;		     con=DriverManager.getConnection("jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf-8","root","root");
		     sql=con.createStatement();
		     sql.executeUpdate(command1);
		     con.close();
		}
		catch(SQLException e1) {
			mes += e1.toString();
			JSONObject rjson = new JSONObject();
			rjson.put("mes",mes);
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应				
		}
		JSONObject rjson = new JSONObject();
		mes = "yes";		
		rjson.put("mes",mes);
		resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应				
	}
}
