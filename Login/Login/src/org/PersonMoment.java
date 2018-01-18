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
public class PersonMoment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public PersonMoment() {
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
		String img = "";
		String command="";
		String mes_id = "";
		String text = "";
		String mes ="";
		String loc = "";
		String datetime="";
		String com_id ="";
		String com_text = "";
		String commentor = "";
		String vote_id = "";
		String votor = "";
		JSONObject result = new JSONObject();
		JSONArray moments = new JSONArray(); 
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		
		Connection con;
		Statement sql; 
		ResultSet rs = null;	
		ResultSet rs1 = null;		
		ResultSet rs2 = null;		
		command = "SELECT * FROM Moments where un = \"" + user + "\""; 
		String command1 = "SELECT * FROM Comment where mes_id = \"%s\"";
		String command2 = "SELECT * FROM Upvote where mes_id = \"%s\"";
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
		    	 mes_id = rs.getString("mes_id");
		    	 text = rs.getString("text");
		    	 img = rs.getString("img");
		    	 loc = rs.getString("loc");
		    	 datetime = rs.getString("creat_time");
		    	
		    	 JSONArray comment = new JSONArray();
		    	 String com_command = String.format(command1, mes_id);
		    	 rs1 = sql.executeQuery(com_command);
		    	 while(rs1.next()) {
		    		 JSONObject each_comment = new JSONObject();
		    		 com_id = rs1.getString("com_id");
		    		 commentor = rs1.getString("commentor");
		    		 com_text = rs1.getString("text");
		    		 each_comment.put("com_id", com_id);
		    		 each_comment.put("commentor",commentor);
		    		 each_comment.put("text", com_text);
		    		 comment.add(each_comment);
		    	 }
		    	 
		    	 JSONArray upvote = new JSONArray();
		    	 String vot_command = String.format(command2, mes_id);
		    	 rs2 = sql.executeQuery(vot_command);
		    	 while(rs2.next()) {
		    		 JSONObject each_upvote = new JSONObject();
		    		 vote_id = rs2.getString("vote_id");
		    		 votor = rs1.getString("votor");
		    		 each_upvote.put("vote_id", vote_id);
		    		 each_upvote.put("votor",votor);
		    		 upvote.add(each_upvote);		    		 
		    	 }
		    	 
		    	 each_moment.put("mes_id", mes_id);
		    	 each_moment.put("text", text);
		    	 each_moment.put("img", img);
		    	 each_moment.put("loc", loc);
		    	 each_moment.put("creat_time", datetime);
		    	 each_moment.put("comment", comment);
		    	 each_moment.put("upvote", upvote);	
		    	 moments.add(each_moment);
			 }
		     con.close();
		}
		catch(SQLException e1) { 
			mes += e1.toString(); 
			JSONObject rjson = new JSONObject();
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
		}	
		mes = "yes";
	    result.put("moments", moments);
	    result.put("mes", mes);
		resp.getOutputStream().write(result.toString().getBytes("UTF-8"));
	}
}
