package parseHtml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WeiboParser {

	public static enum Company {
		sina, tencent
	}

	private Document doc;
	private Charset charset;
	private String docText;
	private ArrayList<ArrayList<String>> samples;
	private ArrayList<Element> listTrees;
	private ArrayList<Element> weiboTrees;
	private String weiboListSplitRegex;
	private String textSplitRegex;

	public WeiboParser() {
		samples = new ArrayList<ArrayList<String>>();
		listTrees = new ArrayList<Element>();
		weiboTrees = new ArrayList<Element>();
	}

	public void changeDocument(String html, String charset) {
		clearDocument();
		this.charset = Charset.forName(charset);
		this.doc = Jsoup.parse(html);
		docText = getElementText(this.doc, charset);
		anaylseDocument();
	}

	public void changeDocument(File f, String charset) {
		clearDocument();
		this.charset = Charset.forName(charset);
		try {
			this.doc = Jsoup.parse(f, charset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		docText = getElementText(this.doc, charset);
		// System.out.println(docText);
		anaylseDocument();
	}

	private void anaylseDocument() {
		// 准备每一条微博的样本文本
		weiboListSplitRegex = readRegex(new File("asset/sina_split_regex"));
		textSplitRegex = readRegex(new File("asset/text_split_regex"));
		selectSampleText();
		analyseWeiboListTrees();
		analyseWeiboTrees();
	}

	public ArrayList<Element> getWeiboListTrees() {
		return listTrees;
	}

	public ArrayList<Element> getWeiboTrees() {
		return weiboTrees;
	}

	private void clearDocument() {
		samples.clear();
		listTrees.clear();
		weiboTrees.clear();
	}

	/**
	 * 获取Element包含的纯文本,同时剔除对于给定的字符集(charset)中不可正确编码的字符
	 * */
	private String getElementText(Element e, String charset) {
		StringBuilder sb = new StringBuilder(e.text());
		// int k = sb.indexOf("举报");
		// System.out.println(""+sb.charAt(k)+","+(int)sb.charAt(k));
		// System.out.println(""+sb.charAt(k-1)+","+(int)sb.charAt(k-1));
		// System.out.println(""+sb.charAt(k-2)+","+(int)sb.charAt(k-2));
		// System.out.println(""+sb.charAt(k-3)+","+(int)sb.charAt(k-3));
		// System.out.println(charset.toLowerCase().trim() + ","
		// + (charset.toLowerCase().trim().equals("utf-8")));
		// 替换掉不可正确编码的字符

		// System.out.println("true???");
		if (charset.toLowerCase().trim().equals("utf-8")) {
			for (int i = 0; i < sb.length(); i++) {
				// System.out.println("true");
				if (sb.charAt(i) == 160) {
					sb.replace(i, i + 1, " ");
				}
			}
		}

		return sb.toString();
	}

	private void analyseWeiboTrees() {
		weiboTrees.clear();
		Element tmp = null;
		for (ArrayList<String> sample : samples) {
			if ((tmp = findElementContainSample(sample)) != null) {
				weiboTrees.add(tmp);
				// break;
			}
		}
	}

	private Element findElementContainSample(ArrayList<String> sample) {
		for (Element listRoot : listTrees) {
			// System.out.println("findElementContainSample\n"+listRoot.attributes()+"(listRoot)");
			for (Element weiboTree : listRoot.children()) {
				// System.out.println("findElementContainSample\n"+weiboTree.attributes()+"(weiboTree)");
				if (isElementContainSample(weiboTree, sample)) {
					// System.out.println("findElementContainSample\n"+listRoot.attributes()+",\n"+weiboTree.attributes());
					return weiboTree;
				}
			}
		}
		return null;
	}

	private boolean isElementContainSample(Element e, ArrayList<String> sample) {
		boolean flag = true;
		StringBuffer sb = new StringBuffer(":contains()");
		int begin, end;
		for (String s : sample) {
			begin = sb.indexOf("(");
			end = sb.lastIndexOf(")");
			sb.replace(begin + 1, end, s);
			if (e.select(sb.toString()).isEmpty()) {
				flag = false;
				break;
			}
		}
//		 if (flag) {
//			 System.out.println(e.attributes() + ", " + sample);
//		 }
		return flag;
	}

	/**
	 * 获取包含微博列表的dom子树的根节点集合
	 * 
	 * @param root
	 *            dom树根节点
	 * @param charset
	 *            用于解析纯文本的编码集，它应该和解析dom树用的编码集合一致
	 * @return 包含微博列表的dom子树的根节点集合；集合中的根节点之间存在父子关系，节点在dom树中较深的排在前面
	 */
	private void analyseWeiboListTrees() {
		// String text = getElementText(doc,charset.displayName());

		ArrayList<Element> nodes = new ArrayList<Element>();
		ArrayList<Element> weibotrees = new ArrayList<Element>();
		int size = samples.size();
		Element tmp;
		for (int i = 0; i < size; i++) {
			List<String> samplesPerWeiBo = samples.get(i);
			nodes.clear();
			for (String sample : samplesPerWeiBo) {
				Elements els = doc.select(":containsOwn(" + sample + ")");
				// 样本文本在只在树的一个节点处出现时，我们能确定它在微博列表子树中，否则我们不能确定
				if (els.size() == 1) {
					if (!nodes.contains(els.first())) {
						nodes.add(els.first());
						// System.out.println("\n--\n"+els.first().text());
					}
				}
			}
			// System.out.println(nodes);
			tmp = findCommonTree(nodes);
			if (tmp != null && !weibotrees.contains(tmp)) {
				weibotrees.add(tmp);
			}
		}
		// listTrees = new ArrayList<Element>();
		listTrees.clear();
		int weiboTreeSize = weibotrees.size();
		for (int i = 0; i < weiboTreeSize - 1; i += 2) {
			tmp = findCommonTree(weibotrees.get(i), weibotrees.get(i + 1));
			if (tmp != null && !listTrees.contains(tmp)) {
				listTrees.add(tmp);
			}
		}
		// listTrees中的节点排序，节点在dom树中较深的排在前面
		Collections.sort(listTrees, new Comparator<Element>() {

			@Override
			public int compare(Element o1, Element o2) {
				// TODO Auto-generated method stub
				int size1 = o1.parents().size();
				int size2 = o2.parents().size();
				return -(size1 - size2);
			}

		});
		// return listTrees;
	}

	private void selectSampleText() {
		// ArrayList<ArrayList<String>> samples = new
		// ArrayList<ArrayList<String>>();
		samples.clear();
		// String regex = readRegex(new File("asset/sina_split_regex"));
		String[] ss = spliteText(weiboListSplitRegex, docText);
		//System.out.println(docText);
		int len = ss.length;
		// 移除按照正则表达式分割后的文本中的最后一项，因为它不含有微博
		for (int i = 0; i < len - 1; i++) {
			//System.out.println(ss[i]);
			samples.add(selectSampleTextPerWeibo(ss[i]));
		}
	}

	/**
	 * 从抽取微博列表中抽取的作为样本的微博数目
	 * 
	 * @deprecated
	 */
	public static final int SAMPLE_WEIBO_SIZE = 5;
	/** 从一条微博中抽取的文本片段最大数目 */
	public static final int SAMPLE_SEGMENT_MAX_SIZE = 3;
	/** 从一条微博中抽取的每条文本片的段最小长度 */
	public static final int SAMPLE_SEGMENT_MIN_LENGTH = 3;

	/**
	 * 从微博正文中抽取若干文本片段作为样本，顺序是从后向前
	 * 
	 * @param weiboText
	 *            微博文本内容
	 * @return 微博文本内容中提取的样本
	 */
	private ArrayList<String> selectSampleTextPerWeibo(String weiboText) {
		ArrayList<String> samples = new ArrayList<String>();

		String[] splitedText = weiboText.trim().split(textSplitRegex);
		int ssize = splitedText.length;
		int num = 0;
		// for (int j = 0; j < splitedText.length && j <
		// SAMPLE_SEGMENT_MAX_SIZE; j++) {
		for (int j = ssize - 1; j >= 0 && num <= SAMPLE_SEGMENT_MAX_SIZE; j--) {
			if (splitedText[j].length() >= SAMPLE_SEGMENT_MIN_LENGTH
					&& !samples.contains(splitedText[j])) {
				samples.add(splitedText[j]);
				num++;
			}
		}
		return samples;
	}

	/**
	 * 获取节点集合中element的共有最小父节点
	 */
	private Element findCommonTree(ArrayList<Element> elmentList) {
		Element e1, e2, result;
		while (elmentList.size() > 1) {
			e1 = elmentList.get(0);
			e2 = elmentList.get(1);
			result = findCommonTree(e1, e2);
			elmentList.add(result);
			elmentList.remove(e1);
			elmentList.remove(e2);
		}
		if (!elmentList.isEmpty())
			return elmentList.get(0);
		else
			return null;
	}

	/**
	 * 获取节点集合中element的共有最小父节点
	 */
	private Element findCommonTree(Element e1, Element e2) {
		Elements parents1 = e1.parents();
		Elements parents2 = e2.parents();
		int i = parents1.size() - 1;
		int j = parents2.size() - 1;
		for (; i >= 0 && j >= 0; i--, j--) {
			if (!parents1.get(i).equals(parents2.get(j)))
				break;
		}
		if (i < 0)
			return e1;
		else if (j < 0)
			return e2;
		else
			return parents1.get(i + 1);
	}

	/** @return 按照正则表达式分割后的文本 */
	private String[] spliteText(String regex, String text) {
		Pattern p = Pattern.compile(regex, Pattern.COMMENTS);
		return p.split(text, 0);
	}

	public static String replaceToASCII(String s) {
		StringBuilder sb = new StringBuilder(s);

		int index = 0;
		for (index = sb.indexOf("\\", index); index != -1; index = sb.indexOf(
				"\\", index)) {
			switch (sb.charAt(index + 1)) {
			case '\"':
				sb.replace(index, index + 2, "\"");
				break;
			case '\'':
				sb.replace(index, index + 2, "\'");
				break;
			case 'n':
				sb.replace(index, index + 2, "\n");
				break;
			case 't':
				sb.replace(index, index + 2, "\t");
				break;
			case 'r':
				if (sb.charAt(index + 2) == '\\' && sb.charAt(index + 3) == 'n') {
					sb.replace(index, index + 4, "\n");
				} else {
					sb.replace(index, index + 2, "\n");
				}
				break;
			case '/':
				sb.replace(index, index + 2, "/");
				break;
			default:

			}
			index += 1;
		}
		return sb.toString();
	}

	/** 从文件中读取正则表达式 */
	private String readRegex(File f) {
		FileReader fr = null;
		char[] buf = new char[1024];
		int len = -1;
		StringBuilder sb = new StringBuilder();
		try {
			fr = new FileReader(f);
			while ((len = fr.read(buf)) != -1) {
				sb.append(buf, 0, len);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// int begin, end;
		// StringBuffer sb = new StringBuffer("()");
		// begin = sb.indexOf("(");
		// end = sb.lastIndexOf(")");
		// sb.replace(begin+1, end, "kk");
		// System.out.println(""+begin+","+end);
		// System.out.println(sb.toString());
		ParseHtml parseHtml = new ParseHtml();
		WeiboParser wp = new WeiboParser();
		wp.changeDocument(
				//new File("F:\\课程\\搜索引擎\\网页预处理\\脱机网页\\微博综合搜索 - kk - 新浪微博.htm"),"utf-8");
				new File("testFile\\weibo2.html"), "UTF-8");
		ArrayList<Element> trees = wp.getWeiboListTrees();

		ArrayList<Element> childs = wp.getWeiboTrees();

		for (Element e : trees) {
			if (trees.contains(e.parent())) {
				System.out.println("yes");
			}
			System.out.println(e.attributes());
		}
		int count = 1;
		System.out.println("............................");
		for (Element e : childs) {
			System.out.println("第"+count+"条微博解析如下：");
			parseHtml.parseWeiBo(e);
			count++;
		}
	}
}
