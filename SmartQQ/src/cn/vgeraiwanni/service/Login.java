package cn.vgeraiwanni.service;

import java.util.Map;

import cn.vgeraiwanni.util.HttpConnect;

public class Login {
	
	private String status = "请扫描二维码";//二维码状态提示
	
	private String cookie1 = null;//获取二维码信息时候的cookie
	private String cookie2 = null;//获取二维码传过来的cookie，校验二维码是否过期时候使用的cookie
	private String cookie3 = null;//手机上点击登录后，返回的cookie
	private String cookie4 = null;//手机上点击登录后返回网页，访问网页，返回的cookie
	
	private String vfwebqq = null;
	private String psessionId = null;
	
	private String QQNum = null;
	
	private String qrcodeCallBackUrl = null;
	private String strQrsig = null;
	private int token = 0;
	private String hash2 = null;//获取好友列表的hash
	
	public Login(String filePath){
		this.cookie1 = getCookie1();
		this.cookie2 = downloadQrcode(filePath);
		
		this.strQrsig = cookie2.substring(cookie2.indexOf('=')+1,cookie2.indexOf(';'));
		this.token = HttpConnect.hash33(strQrsig);
		loginBlocked();
		this.QQNum = getQQNumFromCookie3();
		this.cookie4 = getCookie4(qrcodeCallBackUrl, cookie3);
		this.vfwebqq = getvfwebqq();
		this.hash2 = HttpConnect.hash2(QQNum);
		this.psessionId = getPsessionid();
	}
	
	//初始的cookie
	private String getCookie1(){
		String url = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http://Fw.qq.com/proxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001";
		Map cookieMap = HttpConnect.getCookies(url,null,null);
		return HttpConnect.decodeCooikes(cookieMap);
	}
	
	//下载二维码，返回qrsig
	private String downloadQrcode(String filePath){
		String url = "https://ssl.ptlogin2.qq.com/ptqrshow";//smartQQ 二维码url前缀
		int appid = 501004106;
		int e = 1; //0到4二维码外面白色的大小
		/* l为二维码纠错能力
		 * "L":约7%
		 * "M":约15%
		 * "Q":约25%
		 * "H":约30%
		 */
		String l = "M";//二维码的纠错能力
		int s = 6;//1到8二维码整体大小
		int d = 72;//意义暂时不明
		int v = 4;//貌似是点的密集程度
//		double t = 0.8557249614340405; //随机数
		double t = Math.random();//随机数
		int daid = 164; //意义不明
		int pt_3rd_aid = 0; //意义不明
		
		String urlPath = url+"?appid="+appid
						+"&e="+e
						+"&l="+l
						+"&s="+s
						+"&d="+d
						+"&v="+v
						+"&t="+t
						+"&daid="+daid
						+"&pt_3rd_aid="+pt_3rd_aid;
		String referer = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001";
		String str = HttpConnect.downloadAndGetCookie(urlPath, filePath, referer);
		return str.substring(0, str.indexOf(";")+1)+ " ";
	}
	
	//查看二维码的状态，返回"ptuicb(...)"的内容
	private String checkQrcodeStatu(){
		String cookie = "qrsig="+strQrsig;
		String strUrl = "https://ssl.ptlogin2.qq.com/ptqrlogin?u1=http://w.qq.com/proxy.html&ptqrtoken="+ token+ "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action="+1024+"&js_ver=10232&js_type=1&login_sig&pt_uistyle=40&aid=501004106&daid=164&mibao_css=m_webqq&";
		String referer = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001";
		return HttpConnect.getResponse(strUrl, cookie, referer);
	}
	
	
	//登录验证阻塞
	private void loginBlocked(){
		String temp = null;
		while(true){
			temp = setStatusStr();
			if(temp == null) {
				//TODO 状态
				System.out.println(status);
			} else {
				qrcodeCallBackUrl = temp;
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//获取二维码当前状态
	private String setStatusStr(){
		String tempStatus = getInfoByIndex(checkQrcodeStatu(), 1);
		if(tempStatus.equals("66")){
			this.status = "请扫描二维码";
		} else if(tempStatus.equals("65")) {
			this.status = "二维码已经失效了";
		} else if (tempStatus.equals("67")){
			this.status = "已经扫描了二维码，请在手机上确认登录";
		} else if (tempStatus.equals("0")){
			this.status = "手机上已经确认登录";
			String cookie = "qrsig="+strQrsig;
			String strUrl = "https://ssl.ptlogin2.qq.com/ptqrlogin?u1=http://w.qq.com/proxy.html&ptqrtoken="+ token+ "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action="+1024+"&js_ver=10232&js_type=1&login_sig&pt_uistyle=40&aid=501004106&daid=164&mibao_css=m_webqq&";
			String referer = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001";
			Map callbackMap = HttpConnect.getResponseAndCookie(strUrl, cookie);
			Map headerMap = (Map) callbackMap.get("header");
			cookie3 = HttpConnect.decodeCooikes(headerMap);
			return getInfoByIndex((String) callbackMap.get("response-str"), 5);
		} else {
			this.status = "系统出现错误";
		}
		return null;
	}
	
	
	//index只能为奇数
	//获取第index引号的内容
	private String getInfoByIndex(String str, int index){
		int i = 0;
		String temp = str;
		for(i = 0; i < index; i++){
			temp = temp.substring(temp.indexOf('\'')+1);
		}
		temp = temp.substring(0, temp.indexOf('\''));
		return temp;
	}
	
	//从cookie3中获取到QQ号码
	private String getQQNumFromCookie3(){
		String result = cookie3;
		result = result.substring(result.indexOf(" uin=")+6);
		result = result.substring(0, result.indexOf(";"));
		return result;
	}
	
	/*//检测二维码是否被扫描登录的线程
	private void checkThread(){
		Thread th = new Thread("checkQrcodeStatus"){
			public String status = "请扫描二维码";
			
			public void run(){
				String temp = null;
				while(true){
					temp = setStatusStr();
					if(temp == null) {
						//TODO 状态
						System.out.println(status);
					} else {
						qrcodeCallBackUrl = temp;
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			//index只能为奇数
			//获取第index引号的内容
			private String getInfoByIndex(String str, int index){
				int i = 0;
				String temp = str;
				for(i = 0; i < index; i++){
					temp = temp.substring(temp.indexOf('\'')+1);
				}
				temp = temp.substring(0, temp.indexOf('\''));
				return temp;
			}
			
			//获取二维码当前状态
			private String setStatusStr(){
				String tempStatus = getInfoByIndex(checkQrcodeStatu(), 1);
				if(tempStatus.equals("66")){
					this.status = "请扫描二维码";
				} else if(tempStatus.equals("65")) {
					this.status = "二维码已经失效了";
				} else if (tempStatus.equals("67")){
					this.status = "已经扫描了二维码，请在手机上确认登录";
				} else if (tempStatus.equals("0")){
					this.status = "手机上已经确认登录";
					String cookie = "qrsig="+strQrsig;
					String strUrl = "https://ssl.ptlogin2.qq.com/ptqrlogin?u1=http://w.qq.com/proxy.html&ptqrtoken="+ token+ "&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action="+1024+"&js_ver=10232&js_type=1&login_sig&pt_uistyle=40&aid=501004106&daid=164&mibao_css=m_webqq&";
					String referer = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001";
					Map callbackMap = HttpConnect.getResponseAndCookie(strUrl, cookie);
					Map headerMap = (Map) callbackMap.get("header");
					cookie3 = HttpConnect.decodeCooikes(headerMap);
					return getInfoByIndex((String) callbackMap.get("response-str"), 5);
				} else {
					this.status = "系统出现错误";
				}
				return null;
			}
			
		};
		th.start();
	}*/
	
	//获取手机上扫描成功返回网址，访问网址返回的cookie
	private String getCookie4(String strUrl, String cookie){
		String temp = HttpConnect.decodeCooikes(HttpConnect.getHeaderWithout302(strUrl, cookie, null));
		for(int i = 0; i < 3; i++){
			temp = temp.substring(temp.indexOf(';')+1);
		}
		return temp;
	}
	
	//获取vfwebqq的值
	private String getvfwebqq(){
		String strUrl = "http://s.web2.qq.com/api/getvfwebqq?ptwebqq=&clientid=53999199&psessionid=&t=1513175610597";
		//这个一定要加referer
		String referer = "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1";
		
		String response = HttpConnect.getResponse(strUrl, cookie3+cookie4, referer);
		response = response.substring(response.indexOf("vfwebqq")+10,response.lastIndexOf("\""));
		return response;
	}
	
	//真正登录获取到psessionId
	private String getPsessionid(){
		String strUrl = "http://d1.web2.qq.com/channel/login2";
		String referer = "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2";
		
		String param = "r={\"ptwebqq\":\"\",\"clientid\":53999199,\"psessionid\":\"\",\"status\":\"online\"}";
		
		String response = HttpConnect.getResponsePost(strUrl, cookie3+cookie4, referer, param);
		response = response.substring(response.indexOf("psessionid")+13, response.indexOf("status")-3);
		return response;
	}
	
	//获取好友列表的响应
	public String getFriendList(){
		String strUrl = "http://s.web2.qq.com/api/get_user_friends2";
		String referer = "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1";
		
		String param = "r={\"vfwebqq\":\""+vfwebqq+"\",\"hash\":\""+hash2+"\"}";
		return HttpConnect.getResponsePost(strUrl, cookie3+cookie4, referer, param);
	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("生成的二维码在E盘下:test.png");
		Login login = new Login("E:/test.png");
		System.out.println(login.getFriendList());
	}
}
