package Utils;

import java.util.ArrayList;

public class Weibo {
	private String weibo_url;
	private String url_ID;
	private String text;
	private ArrayList<String> imageList = new ArrayList<String>();
	private ArrayList<String> urlList = new ArrayList<String>();
	

	private int forward_num;
	private int collect_num;
	private int support_num;
	
	public Weibo() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Weibo(String weibo_url, String url_ID, String text,
			ArrayList<String> imageList, ArrayList<String> urlList,
			int forward_num, int collect_num, int support_num) {
		super();
		this.weibo_url = weibo_url;
		this.url_ID = url_ID;
		this.text = text;
		this.imageList = imageList;
		this.urlList = urlList;
		this.forward_num = forward_num;
		this.collect_num = collect_num;
		this.support_num = support_num;
	}


	public ArrayList<String> getImageList() {
		return imageList;
	}


	public void setImageList(ArrayList<String> imageList) {
		this.imageList = imageList;
	}


	public ArrayList<String> getUrlList() {
		return urlList;
	}


	public void setUrlList(ArrayList<String> urlList) {
		this.urlList = urlList;
	}


	public String getWeibo_url() {
		return weibo_url;
	}
	
	public void setWeibo_url(String weibo_url) {
		this.weibo_url = weibo_url;
	}
	
	public String getUrl_ID() {
		return url_ID;
	}
	
	public void setUrl_ID(String url_ID) {
		this.url_ID = url_ID;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getForward_num() {
		return forward_num;
	}
	
	public void setForward_num(int forward_num) {
		this.forward_num = forward_num;
	}
	
	public int getCollect_num() {
		return collect_num;
	}
	
	public void setCollect_num(int collect_num) {
		this.collect_num = collect_num;
	}
	
	public int getSupport_num() {
		return support_num;
	}
	
	public void setSupport_num(int support_num) {
		this.support_num = support_num;
	}
	
}
