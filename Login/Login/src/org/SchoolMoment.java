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
public class SchoolMoment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public SchoolMoment() {
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
		
		String sch = json.getString("school");	
		Double cur_lng = json.getDouble("longitude");
		Double cur_lat = json.getDouble("latitude");
		//int index = json.getInt("index");
		//index--;
		String un = "";
		//Double lng = 0.0;
		//Double lat = 0.0;
		String header = "";
		String img = "";
		String des = "";
		String command="";
		int mes_id = 0;
		String text = "";
		String mes ="";
		String datetime="";
		int com_id = 0;
		String com_text = "";
		String commentor = "";
		int vote_id = 0;
		String votor = "";
		String nickname = "";
		JSONObject result = new JSONObject();
		JSONArray moments = new JSONArray(); 
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		
		Connection con;
		Statement sql; 
		Statement sql1;
		Statement sql2;
		Statement sql3;
		ResultSet rs = null;	
		ResultSet rs1 = null;		
		ResultSet rs2 = null;		
		ResultSet rs3 = null;
		command = "SELECT description,un,mes_id,text,img,longitude,latitude,creat_time,(st_distance(loc, point(%.8f,%.8f))*111) AS distance FROM Moments where school = '%s' order by rand() limit 1"; 
		//command = "SELECT description,un,mes_id,text,img,creat_time FROM Moments where school = '%s' order by mes_id desc limit %d,1"; 		
		command = String.format(command, cur_lng,cur_lat,sch);
		String command1 = "SELECT * FROM Comment where mes_id = %d";
		String command2 = "SELECT * FROM Upvote where mes_id = %d";
		String command3 = "SELECT img,nickname FROM Userinfo where un = \"%s\"";
		try{ 
			Class.forName("com.mysql.jdbc.Driver"); 
		}catch(ClassNotFoundException e){ 
			mes += e.toString(); 
			JSONObject rjson = new JSONObject();
			rjson.put("mes", mes);
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
		}  
		
		
		try {  
		     con=DriverManager.getConnection("jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf-8","root","root");
		     sql=con.createStatement();
		     rs=sql.executeQuery(command);
		     while(rs.next()) {
		    	 JSONObject each_moment = new JSONObject();
		    	 mes_id = rs.getInt("mes_id");
		    	 text = rs.getString("text");
		    	 img = rs.getString("img");
		    	 //lng = rs.getDouble("longitude");
		    	 //lat = rs.getDouble("latitude");
		    	 des = rs.getString("description");
		    	 datetime = rs.getString("creat_time");
		    	 un = rs.getString("un");
		    	 
		    	 
		    	 JSONArray comment = new JSONArray();
		    	 String com_command = String.format(command1, mes_id);
		    	 sql1=con.createStatement();
		    	 rs1 = sql1.executeQuery(com_command);
		    	 while(rs1.next()) {
		    		 JSONObject each_comment = new JSONObject();
		    		 com_id = rs1.getInt("com_id");
		    		 commentor = rs1.getString("commentor");
		    		 com_text = rs1.getString("text");
		    		 each_comment.put("com_id", com_id);
		    		 each_comment.put("commentor",commentor);
		    		 each_comment.put("text", com_text);
		    		 comment.add(each_comment);
		    	 }
	    	 
		    	 JSONArray upvote = new JSONArray();
		    	 String vot_command = String.format(command2, mes_id);
		    	 sql2 = con.createStatement();
		    	 rs2 = sql2.executeQuery(vot_command);
		    	 while(rs2.next()) {
		    		 JSONObject each_upvote = new JSONObject();
		    		 vote_id = rs2.getInt("vote_id");
		    		 votor = rs2.getString("votor");
		    		 each_upvote.put("vote_id", vote_id);
		    		 each_upvote.put("votor",votor);
		    		 upvote.add(each_upvote);		    		 
		    	 }
		    	 
		    	 String userinfo = String.format(command3, un);
		    	 sql3 = con.createStatement();
		    	 rs3 = sql3.executeQuery(userinfo);
		    	 while(rs3.next()) {
		    		 header = rs3.getString("img");
		    		 nickname = rs3.getString("nickname");
		    	 }
		    	 
		    	 try {
			    	 each_moment.put("header", header);
			    	 each_moment.put("nickname", nickname);
			    	 each_moment.put("mes_id", mes_id);
			    	 each_moment.put("text", text);
			    	 each_moment.put("img", img);
			    	 each_moment.put("description", des);
			    	 //each_moment.put("longitude", lng);
			    	 //each_moment.put("latitude", lat);
			    	 each_moment.put("creat_time", datetime);
			    	 each_moment.put("comment", comment);
			    	 each_moment.put("upvote", upvote);	
			    	 moments.add(each_moment);
		    	 }catch(JSONException e1) {
		 			mes += e1.toString(); 
					JSONObject rjson = new JSONObject();
					rjson.put("mes", mes);
					resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));    		 
		    	 }
			 }
		     con.close();
			 mes = "yes";
			 result.put("moments", moments);
			 result.put("mes", mes);
			 resp.getOutputStream().write(result.toString().getBytes("UTF-8"));
		}
		catch(SQLException e1) { 
			mes += e1.toString(); 
			JSONObject rjson = new JSONObject();
			rjson.put("mes", mes);
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
		}			
	}
}
