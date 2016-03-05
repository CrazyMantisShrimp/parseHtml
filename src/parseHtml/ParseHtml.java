package parseHtml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import Utils.Constant;
import Utils.User;
import Utils.Weibo;

class ParseHtml {
	private Weibo weibo;
	private String url;
	private String html;
	
	public ParseHtml(Weibo weibo, String url, String html) {
		super();
		this.weibo = weibo;
		this.url = url;
		this.html = html;
	}

	
	public ParseHtml() {
		super();
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public Weibo getWeibo() {
		return weibo;
	}
	public void setWeibo(Weibo weibo) {
		this.weibo = weibo;
	}
	
	public void parseString() {
		Document doc = Jsoup.parse(html,Constant.baseUrl);
		System.out.println(doc);
		Elements es = doc.body().getAllElements();
		System.out.println(es.attr("onload"));
		System.out.println(es.select("p"));
	}
	
	//返回节点到最终父节点的长度
	public int getLength(Element eNode) {
		int length=0;
		Element eTemp = eNode;
		while(eTemp != null){
			length++;
			eTemp = eTemp.parent();
		}
		return length;
	}
	
	//返回两个节点的公共节点
	public Element findCommonParent(Element node1,Element node2){
		int length1 = getLength(node1);
		int length2 = getLength(node2);
		
		Element eNode1 = null;
		Element eNode2 = null;
		int k=0;
		if(length1 >= length2) {
			Element eTemp = node1;
			while(k < length1-length2){
				k++;
				eTemp = eTemp.parent();
			}
			eNode1 = eTemp;
			eNode2 = node2;
		}
		else {
			Element eTemp = node2;
			while(k < length2-length1) {
				k++;
				eTemp = eTemp.parent();
			}
			eNode1 = node1;
			eNode2 = node2;
		}
		
		while(eNode1!=null && eNode2!=null && eNode1 != eNode2) {
			eNode1 = eNode1.parent();
			eNode2 = eNode2.parent();
		}
		return eNode1;
	}
	
	public void parseWeiBo(Element weiboNode) {  
        String text = weiboNode.text();
		//判断是否是转发的
		if(isForward(weiboNode)) {
			System.out.println("此微博是转发的微博");
			String [] textByforward = text.split("◆◆");
			String userName = getUserName(textByforward[0]);
			System.out.println(userName);
			String [] textContent = textByforward[0].split(userName);
			System.out.println(textContent[1]);
			User user = new User();                 				
		    user = getUser(weiboNode, userName);
		    System.out.println(user.toString());
		    //ArrayList<String> mentionLinks = new ArrayList<String>();
			//获取用户所发文本信息
		    Element temp = weiboNode.getElementsContainingOwnText("◆").first();
		    Element forward = temp.parent().parent();
		   // System.out.println(forward.outerHtml());
		    if(forward == null)
		    	return;
		    parseWeiboNode(forward);
		    System.out.println("主微博的地址："+getWeiboUrl(weiboNode,Constant.Sina_WeiBoUrl_identify));
		}
		else {
			parseWeiboNode(weiboNode);
		}  
    } 
	
	//判断是否是转发
	public boolean isForward(Element root) {
		String text = root.text();
		if(text.contains("◆◆"))
			return true;
		return false;
	}

	public void parseWeiboNode(Element root) {
		if(root == null) {
			return;
		}
		System.out.println(root.text());
		String weiboText = root.text();
		System.out.println(weiboText);
		String username=null;
		if(weiboText.contains("◆◆")) {
			String [] needText=weiboText.split("◆◆");
			weiboText = needText[1];
			System.out.println(weiboText);
			username = getForwardName(weiboText);
		}else {
			username = getUserName(weiboText);
		}
		System.out.println(username);
          //获取用户基本信息
         User user = new User();                 				
         user = getUser(root,username);
         if(user!=null)
        	 System.out.println("用户信息："+user.toString()+"\n");
         else {
        	 System.err.println("该条微博用户信息解析错误");
        	 //System.out.println(root.outerHtml());
        	 return;
         }
         Weibo weibo = new Weibo();
         ArrayList<String> mentionLinks = new ArrayList<String>();
         mentionsUser(root,mentionLinks);													//关联的用户信息
         for(int i=0;i<mentionLinks.size();i++) {
      	   System.out.println("@的用户:  "+mentionLinks.get(i));
          }
          //热点信息
          Map<String,String> hotTopicMap = new HashMap<String,String>();
          getHotTopic(root, hotTopicMap);
          for(Map.Entry<String, String> m:hotTopicMap.entrySet()) {
          	System.out.println("热点信息："+m.getKey()+"  :  "+m.getValue());
          }
          Element ul = root.getElementsByTag("ul").first();     // 获取微博中的图片链接
          if(ul != null) {
	            Elements imageUrls = ul.getElementsByTag("img");              
	            for(Element img: imageUrls) {
	            	System.out.println("微博中图片的地址：     "+img.attr("src"));
	            }
          }
          ArrayList<String> weiboLinks = new ArrayList<String>();
          getTextUrl(weiboText, weiboLinks);
          System.out.println("\n");
          for(int i=0;i<weiboLinks.size();i++) {
         	   System.out.println("微博中的链接信息："+weiboLinks.get(i));
          }
          System.out.println("微博的地址：    "+ getWeiboUrl(root,Constant.Sina_WeiBoUrl_identify));
	}
	
	public String getForwardName(String text) {
		String [] forwardText = text.split("@");
		String [] username = forwardText[1].split(" ");
		return username[0];
	}
	
	//获取发表微博信息的用户信息
	public User getUser(Element root,String userName) {
		 User user = new User();
		 Element userNode = root.select("[title="+userName+"]").first();
		 user.setName(userName);
		 if(userNode == null)
			 return null;
		 if(userNode.attr("href") != null)
			 user.setUser_link(Constant.baseUrl+userNode.attr("href"));
		 if(userNode.getElementsByTag("img").first() != null)
			 user.setIcon_link(userNode.getElementsByTag("img").first().attr("src"));
         return user;
	}
	
	//获取该条微博的地址
	public String getWeiboUrl(Element root,String identify) {			//根据微博的特殊属性判断,比如新浪"来自"
		 Elements froms = root.getElementsContainingOwnText(identify);    
		 Element lastLine = froms.last().parent();
		 if(lastLine == null)
			 return null;
		 Element weibo_url = lastLine.getElementsByTag("a").first();
		 return Constant.baseUrl+weibo_url.attr("href");
		 
	}
	
	//获取@的对象
	public void mentionsUser(Element root,ArrayList<String> links) {
		 Elements mentions =root.getElementsByTag("a");
		 int count=0;
         for(Element mention : mentions) {
         	Element index = mention.getElementsContainingText("@").first();
         	if(index != null) {
         		String info=null;
	         	info = index.text().split("@")[1];
	         	info += "##"+ Constant.baseUrl+index.attr("href");           //用##将用户名和所在链接分隔开
	         	links.add(info);
	         	count++;
         	}
         }
	}
	//根据返回的文本提取发表人的信息
	public String getUserName(String text) {    
		 String [] str = text.split(" ");                   //文本内容
		 if(str[0].contains("@")) {
			 str[0].replace("@", "");
		 }
		 //System.out.println(str[0]);
         return str[0];
	}
	
	//获取热点信息
	public void getHotTopic(Element root,Map<String,String> map) {
		String nodeText = root.text();
		String regex = "#.*?#";
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(nodeText);
		while(match.find()) {
			Element hotTopicNode = root.getElementsContainingOwnText(match.group()).first();
			if(hotTopicNode!=null)
				map.put(match.group(), hotTopicNode.attr("href"));
		}
	}
	
	//返回链接，如果有链接的话
	public void getTextUrl(String text,ArrayList<String>link) {                      
		String vedioUrl = null;
			//url正则表达式
		String regex = "\\bhttps?://[a-zA-Z0-9\\-.]+(?::(\\d+))?(?:(?:/[a-zA-Z0-9\\-._?,'+\\&%$=~*!():@\\\\]*)+)?";
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(text);
		while(match.find()) {
			link.add(match.group());
		}
	}
}
