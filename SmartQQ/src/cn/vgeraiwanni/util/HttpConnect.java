package cn.vgeraiwanni.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class HttpConnect {

	//获取SmartQQ二维码和对应二维码的cookie
	public static String downloadAndGetCookie(String strUrl, String saveFilePath, String referer){
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			if(referer != null){
				connection.setRequestProperty("referer", referer);
			}
			connection.connect();
			byte[] buffer = getBuffer(connection.getInputStream());
			fileWrite(saveFilePath, buffer);
			return connection.getHeaderField("set-cookie");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//获取网路数据流buffer
	private static byte[] getBuffer(InputStream inputStream) throws IOException{
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while((len = inputStream.read(buffer)) != -1){
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}
	
	//写入文件
	private static void fileWrite(String filePath, byte[] buffer) throws IOException{
		File file = new File(filePath);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(buffer);
		fos.close();
	}
	
	//获取返回的set-cookie的信息
	public static String getCookie(String strUrl, String cookie){
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			if(cookie != null){
				connection.setRequestProperty("cookie", cookie);
			}
			connection.connect();
			return connection.getHeaderField("set-cookie");
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//获取返回的头部信息
	public static Map getCookies(String strUrl, String cookie, String referer){
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			if(cookie != null){
				connection.setRequestProperty("cookie", cookie);
			}
			if(referer != null){
				connection.setRequestProperty("referer", referer);
			}
			connection.connect();
			return connection.getHeaderFields();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	//根据返回的头部信息的 Map集合，返回set-cookie即获得的cookie的第一个;前的内容，并组合在一起
	public static String decodeCooikes(Map cookies){
		Collection collection = (Collection) cookies.get("Set-Cookie");
		Iterator it = collection.iterator();
		String result = "";
		while(it.hasNext()) {
			Object ob = it.next();
			if(ob instanceof String){
				String temp = (String) ob;
				result += temp.substring(0, temp.indexOf(";")+1) + " ";
			}
		}
		return result;
	}
	
	
	//github上的qrsig转ptqrtoken
	public static int hash33(String s) {
        int e = 0, n = s.length();
        for (int i = 0; n > i; ++i)
            e += (e << 5) + s.charAt(i);
        return 2147483647 & e;
    }
	
	//通过加载js引擎，读取js文件的函数对获取好友列表的hash值进行计算
	public static String hash2(String numQQ){
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		
		String jsFileName = "property/hash.js";
		FileReader reader =null;
		try {
			reader = new FileReader(jsFileName);
			engine.eval(reader);
			if(engine instanceof Invocable){
				Invocable invoke = (Invocable) engine;
				String hash = (String) invoke.invokeFunction("hash2", numQQ, "");
				return hash;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	//获取文本返回
	public static String getResponse(String strUrl, String cookie, String referer){
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			if(cookie != null){
				connection.setRequestProperty("cookie", cookie);
			}
			if(referer != null){
				connection.setRequestProperty("referer", referer);
			}
			connection.connect();
			byte[] buffer = getBuffer(connection.getInputStream());
			return new String(buffer);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getResponsePost(String strUrl, String cookie, String referer, String param){
		//用来上传参数的
		PrintWriter out = null;
		
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			if(cookie != null){
				connection.setRequestProperty("cookie", cookie);
			}
			if(referer != null){
				connection.setRequestProperty("referer", referer);
			}
			// 发送POST请求必须设置如下两行
            connection.setDoOutput(true);
            connection.setDoInput(true);
            
            connection.connect();
            
            out = new PrintWriter(connection.getOutputStream());
            out.print(param);
            out.flush();
            byte[] buffer = getBuffer(connection.getInputStream());
			return new String(buffer);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(out != null){
				out.close();
			}
		}
		
		return null;
	}
	
	//获取cookie和返回的文本信息，并将文本信息作为response-str键值对存到map集合中
	public static Map getResponseAndCookie(String strUrl, String cookie){
		Map result = null;
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			if(cookie != null){
				connection.setRequestProperty("cookie", cookie);
			}
			connection.connect();
			byte[] buffer = getBuffer(connection.getInputStream());
			result = new HashMap<>();
			result.put("header", connection.getHeaderFields());
			result.put("response-str", new String(buffer));
			return result;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//获取头部信息禁止重定向
	public static Map getHeaderWithout302(String strUrl, String cookie, String referer){
		try {
			URL url = new URL(strUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//禁止重定向
			connection.setInstanceFollowRedirects(false);
			if(cookie != null){
				connection.setRequestProperty("cookie", cookie);
			}
			connection.connect();
			return connection.getHeaderFields();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//暂时有点问题
	public static String sendFriendMsg(String content, String uin, String face, String cookie34, String psessionid){
		String strUrl = "http://d1.web2.qq.com/channel/send_buddy_msg2";
//		String referer = "http://d1.web2.qq.com/cfproxy.html?v=&callback=1";
		String referer = "http://d1.web2.qq.com/cfproxy.html";
		
		String param = "r={"
				+ "\"to\":"+ uin+","//uin
				+ "\"content\":\"[\\\""+ content+"\\\","
				+ "[\\\"font\\\",{\\\"name\\\":\\\"宋体\\\",\\\"size\\\":10,\\\"style\\\":[0,0,0],\\\"color\\\":\\\"000000\\\"}]]\","
				+ "\"face\":"+ face+","//现在主要的点 TODO
				+ "\"clientid\":53999199,"//clientid不变
				+ "\"msg_id\":"+ Math.round(Math.random())*65535+","//这个测试看变不变
				+ "\"psessionid\":\""+ psessionid+"\""
				+ "}";
		
		return getResponsePost(strUrl, cookie34, referer, param);
	}
}
