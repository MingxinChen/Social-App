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
public class Moments extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Moments() {
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
		String img = json.getString("img");
		String text = json.getString("text");
		String des = json.getString("description");
		Double lng  = json.getDouble("longitude");
		Double lat = json.getDouble("latitude");
		String uid = json.getString("uid");
		String school = "";
		String mes = "";
		String mes_id = "";
		String command = "";
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		
		Connection con;
		Statement sql; 
		ResultSet rs = null;	
		command = "Select school from Userinfo where un =\"%s\""; 
		String command1 = String.format(command, user);
		command = "Insert into Moments(school,un,text,img,description,uid,longitude,latitude,loc) values(\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.8f,%.8f,GeomFromText('POINT(%.8f %.8f)'))";
		try{ 
			Class.forName("com.mysql.jdbc.Driver"); 
		}catch(ClassNotFoundException e){ 
			mes += e.toString(); 
			JSONObject rjson = new JSONObject();
			rjson.put("mes", "not completed");
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
		}    
		try {  
;		     con=DriverManager.getConnection("jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf-8","root","root");
		     sql=con.createStatement();
		     rs=sql.executeQuery(command1);
		     while(rs.next()) {
		    	 school = rs.getString("school");
			 }
		     String command2 = String.format(command,school,user,text,img,des,uid,lng,lat,lng,lat);
		     sql.executeUpdate(command2);
		     rs = sql.executeQuery("SELECT LAST_INSERT_ID() as mes_id FROM Moments");
		     while(rs.next()) {
		    	 mes_id = rs.getString("mes_id");
		     }
		     con.close();
			 JSONObject rjson = new JSONObject();
			 mes = "yes";
			 rjson.put("mes_id", mes_id);			
			 rjson.put("mes",mes);
			 resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应				
		}
		catch(SQLException e1) { 
			mes += e1.toString();
			JSONObject rjson = new JSONObject();
			rjson.put("mes",mes);
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应				
		}
	}
}
