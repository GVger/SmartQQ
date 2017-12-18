package cn.vgeraiwanni.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONUtil {
	
	//获取uin为key QQ名为value
	private static Map<String,String> getQQName(String strJson){
		JSONObject jsonObj = JSONObject.fromObject(strJson);
		JSONObject result = jsonObj.getJSONObject("result");
		JSONArray info = result.getJSONArray("info");
		int size = info.size();
		JSONObject tmp = null;
		Map<String,String> QQName = new HashMap<String, String>();
		for(int i = 0; i < size; i++){
			tmp = info.getJSONObject(i);
			QQName.put(tmp.getString("uin"), tmp.getString("nick"));
		}
		return QQName;
	}
	
	//获取uin为key 昵称为value
	private static Map<String, String> getQQNickName(String strJson){
		JSONObject jsonObj = JSONObject.fromObject(strJson);
		JSONObject result = jsonObj.getJSONObject("result");
		JSONArray markNames = result.getJSONArray("marknames");
		int size = markNames.size();
		JSONObject tmp = null;
		Map<String,String> QQNickName = new HashMap<String, String>();
		for(int i = 0; i < size; i++){
			tmp = markNames.getJSONObject(i);
			QQNickName.put(tmp.getString("uin"), tmp.getString("markname"));
		}
		return QQNickName;
	}
	
	//获取好友列表，uin为key 昵称，如果没有昵称则为QQ名字为value
	public static Map<String, String> getFriendList(String strJson){
		Map<String,String> QQNickName = getQQNickName(strJson);
		Map<String,String> QQName = getQQName(strJson);
		Iterator<Map.Entry<String, String>> it = QQName.entrySet().iterator(); 
		while(it.hasNext()){
			Map.Entry<String, String> tmp = it.next();
			if(QQNickName.get(tmp.getKey()) == null){
				QQNickName.put(tmp.getKey(), tmp.getValue());
			}
		}
		return QQNickName;
	}
	
	//获取face列表
	private static Map<String, String> getFaceList(String strJson){
		JSONObject jsonObj = JSONObject.fromObject(strJson);
		JSONObject result = jsonObj.getJSONObject("result");
		JSONArray info = result.getJSONArray("info");
		int size = info.size();
		JSONObject tmp = null;
		Map<String,String> faceList = new HashMap<String, String>();
		for(int i = 0; i < size; i++){
			tmp = info.getJSONObject(i);
			faceList.put(tmp.getString("uin"), tmp.getString("face"));
		}
		return faceList;
	} 
	
	//返回face
	public static String getFaceByUin(String uin, String strJson){
		Map<String,String> faces = getFaceList(strJson);
		return faces.get(uin);
	}
	
	
	//根据value值获取到对应的一个key值  
	public static String getKey(Map<String,String> map,String value){  
		String key = null;  
		//Map,HashMap并没有实现Iteratable接口.不能用于增强for循环.  
		for(String getKey: map.keySet()){  
			if(map.get(getKey).equals(value)){  
				key = getKey;  
			}  
		}
		return key;  
	}
	
	
	
}
