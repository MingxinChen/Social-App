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
public class DoLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public DoLogin() {
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
		//System.out.println(req.getContentType());//得到客户端发送过来内容的类型，application/json;charset=UTF-8
		//System.out.println(req.getRemoteAddr());//得到客户端的ip地址，192.168.1.101
		//------------使用字节流读取客户端发送过来的数据------------
		/*BufferedInputStream bis = new BufferedInputStream(req.getInputStream());
		byte[] b = new byte[1024];
		int len=-1;
		StringBuffer buffer = new StringBuffer();
		while((len=bis.read(b))!=-1){
		buffer.append(new String(b, 0, len));
		}
		bis.close();
		//System.out.println("buffer="+buffer);
		JSONObject json = JSONObject.fromObject(buffer.toString());*/
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
		String pwd = json.getString("pw");
		String em = "";
		String img = "";
		String sch = "";
		String mes = "";
		String check_pass = "";
		String check = "";
		String nickname = "";
		
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json;charset=UTF-8");
		
		if(method.equals("1")) {
			Connection con;
			Statement sql; 
			ResultSet rs = null;	
			check = "SELECT * FROM Userinfo where un = \"" + user + "\""; 
			try{ 
				Class.forName("com.mysql.jdbc.Driver"); 
			}catch(ClassNotFoundException e){ 
				mes += e; 
				JSONObject rjson = new JSONObject();
				rjson.put("mes", mes);
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
			}    
			try {  
			     con=DriverManager.getConnection("jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf-8","root","root");
			     sql=con.createStatement();
			     rs=sql.executeQuery(check);
			     while(rs.next()) {
			    	 check_pass = rs.getString("pwd");
			    	 img = rs.getString("img");
			    	 nickname = rs.getString("nickname");
			    	 sch = rs.getString("school");
				 }
			     con.close();
			}
			catch(SQLException e1) { 
				mes += e1; 
				JSONObject rjson = new JSONObject();
				rjson.put("mes", mes);
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));
			}
			if(check_pass.equals(pwd) && !check_pass.isEmpty()) {
				JSONObject rjson = new JSONObject();
				mes = "yes";
				rjson.put("school", sch);
				rjson.put("img", img);
				rjson.put("nickname", nickname);
				rjson.put("mes", "yes");
				// response.getWriter().write(rjson.toString());//向客户端发送一个带有json对象内容的响应
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应
			}
			else {
				JSONObject rjson = new JSONObject();
				rjson.put("mes", "no");
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应
			}	
		}
		else if(method.equals("2")) {
			em = json.getString("email");
			sch = json.getString("school");
			img = json.getString("img");
			if(!user.isEmpty() && !pwd.isEmpty() && !sch.isEmpty() && !em.isEmpty()) {
				Connection con;
				Statement sql; 
				check = "insert into Userinfo(un,pwd,school,email,nickname,sex,img) values(\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\")";
				String command = String.format(check,user,pwd,sch,em,user,"0",img);
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
				     sql.executeUpdate(command);
				     con.close();
						JSONObject rjson = new JSONObject();
						mes = "yes";
						rjson.put("mes", mes);
						resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应				     
				}
				catch(SQLException e1) { 
					mes+=e1.toString(); 
					JSONObject rjson = new JSONObject();
					rjson.put("mes",mes);
					resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应
				}		
			}
			else {
				JSONObject rjson = new JSONObject();
				rjson.put("mes", "not completed");
				resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应			
			}
		}
		else {
			JSONObject rjson = new JSONObject();
			rjson.put("mes", "method error");
			resp.getOutputStream().write(rjson.toString().getBytes("UTF-8"));//向客户端发送一个带有json对象内容的响应			
		}		
	}
}
