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
public class Dochange extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public Dochange() {
		super();
	}
	@Override
	protected void doGet(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
		this.doPost(req,resp);
	}
	/* （非 Javadoc）
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	/* （非 Javadoc）
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	/* （非 Javadoc）
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	/* （非 Javadoc）
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	/* （非 Javadoc）
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	/* （非 Javadoc）
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
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

		String method = json.getString("method");//从json对象中得到相应key的值
		String user = json.getString("un");	
		
		String old_pwd = "";
		String pwd = "";
		String new_pwd = "";
		String em = "";
		String sch = "";
		String img = "";
		String mes = "";
		String command="";
		String sex="";
		String nickname = "";
		String command1="";
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		
		if(method.equals("1")) {
			Connection con;
			Statement sql; 
			ResultSet rs = null;	
			command = "SELECT school,email,sex,nickname,pwd FROM Userinfo where un = \"" + user + "\""; 
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
			    	 sch = rs.getString("school");
			    	 em = rs.getString("email");
			    	 sex = rs.getString("sex");
			    	 nickname = rs.getString("nickname");
				 }
			     con.close();
			}
			catch(SQLException e1) { 
				mes += e1.toString(); 
				JSONObject rjson = new JSONObject();
				rjson.put("mes", mes);
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
			}
			JSONObject rjson = new JSONObject();
			mes="yes";
			rjson.put("mes", mes);
			rjson.put("school", sch);
			rjson.put("email",em);
			rjson.put("nickname", nickname);
			rjson.put("sex", sex);
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应
		}
		else if(method.equals("2")) {
			pwd = json.getString("pwd");
			new_pwd = json.getString("new_pwd");
			em = json.getString("email");
			img = json.getString("img");
			sex = json.getString("sex");
			nickname = json.getString("nickname");
			
			Connection con;
			Statement sql;
			ResultSet rs = null;
			command = "select school,pwd from Userinfo where un = \"%s\"";
			String command3 = String.format(command, user); 
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
			     rs = sql.executeQuery(command3);
			     while(rs.next()) {
			    	 //old_sch = rs.getString("school");
			    	 old_pwd = rs.getString("pwd");
			     }
			     con.close();    
			}
			catch(SQLException e1) { 
				mes+=e1.toString(); 
				JSONObject rjson = new JSONObject();
				rjson.put("mes", mes);
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应
			}	
			if(pwd.isEmpty() && new_pwd.isEmpty()) {
				command = "update Userinfo set email=\"%s\",sex=\"%s\",img=\"%s\",nickname=\"%s\" where un = \"%s\"";
				command1 = String.format(command,em,sex,img,nickname,user);			
			}
			else if(new_pwd.isEmpty() && pwd.isEmpty()) {
				command = "update Userinfo set pwd = \"%s\",email=\"%s\",sex=\"%s\",img=\"%s\",nickname=\"%s\" where un = \"%s\"";
				command1 = String.format(command,new_pwd,em,sex,img,nickname,user);				
			}
			if((pwd.equals(old_pwd) && !pwd.isEmpty() && !new_pwd.isEmpty()) || (pwd.isEmpty() && new_pwd.isEmpty())) {
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
//					     if(!old_sch.equals(sch) && !old_sch.isEmpty()) {
//					    	 sql.executeUpdate(command2);
//					     }
					     sql.executeUpdate(command1);
					     con.close();
						 JSONObject rjson = new JSONObject();
						 mes = "yes";
						 rjson.put("mes", mes);
						 resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应				     
					}
					catch(SQLException e1) { 
						mes+=e1.toString(); 
						JSONObject rjson = new JSONObject();
						rjson.put("mes", mes);
						resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应
					}					
			}
			else {
				mes = "no";
				JSONObject rjson = new JSONObject();
				rjson.put("mes", mes);
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
			}
				
		}
		else {
			JSONObject rjson = new JSONObject();
			rjson.put("mes", "method error");
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应			
		}
	}
}
