package kr.KENNYSOFT.Kart20190911Crawler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Kart20190911Crawler
{
	public static JSONParser jsonParser = new JSONParser();
	public static final Map<Long, String> nameMap = Map.ofEntries(Map.entry(2070l, "레전드 파츠 X (무제한)"), Map.entry(2071l, "스펙터 X (무제한)"), Map.entry(2072l, "베히모스 X (무제한)"), Map.entry(2073l, "플래티넘 캡슐 기어"), Map.entry(2074l, "황금 기어"), Map.entry(2075l, "코인"), Map.entry(2076l, "코인"), Map.entry(2077l, "네오의 신상 카트 No.7"), Map.entry(2078l, "네오의 신상 카트 No.7"), Map.entry(2079l, "스트라이크 X (무제한)"), Map.entry(2080l, "켈베로스 X (무제한)"), Map.entry(2081l, "비트 X (무제한)"), Map.entry(2082l, "폭스 X (무제한)"), Map.entry(2083l, "마그마 X (무제한)"), Map.entry(2084l, "바스테트 X (무제한)"), Map.entry(2085l, "파츠 조각"), Map.entry(2086l, "파츠 조각"), Map.entry(2087l, "루찌 주머니"), Map.entry(2088l, "한가위 보름달 상자"));
	public static final Map<Long, Integer> cntMap = Map.ofEntries(Map.entry(2073l, 2), Map.entry(2074l, 2), Map.entry(2075l, 500), Map.entry(2076l, 200), Map.entry(2077l, 5), Map.entry(2078l, 2), Map.entry(2085l, 400), Map.entry(2086l, 200), Map.entry(2087l, 20));
	public static final String ENC = "YOUR_ENC";
	public static final String KENC = "YOUR_KENC";
	public static final String NPP = "YOUR_NPP";
	
	public static void main(String[] args) throws Exception
	{
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
		HttpCookie cookie1 = new HttpCookie("ENC", ENC);
		cookie1.setDomain("nexon.com");
		cookie1.setPath("/");
		cookie1.setVersion(0);
		cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie1);
		HttpCookie cookie2 = new HttpCookie("KENC", KENC);
		cookie2.setDomain("kart.nexon.com");
		cookie2.setPath("/");
		cookie2.setVersion(0);
		cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie2);
		HttpCookie cookie3 = new HttpCookie("NPP", NPP);
		cookie3.setDomain("nexon.com");
		cookie3.setPath("/");
		cookie3.setVersion(0);
		cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie3);
		CSVPrinter csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get("coupon.csv")), CSVFormat.DEFAULT.withHeader("\uFEFF당첨일시", "아이템", "수량", "유효기간", "쿠폰번호"));
		int position = 0;
		while (true)
		{
			JSONObject object = null;
			boolean completed;
			do
			{
				completed = true;
				try
				{
					object = play(position);
				}
				catch (HttpRetryException e)
				{
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie1);
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie2);
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie3);
					completed = false;
				}
			} while (!completed);
			switch ((int) (long) ((JSONObject) object.get("Return")).get("n4Return"))
			{
			case 0:
			case -202:
				position = (int) (long) ((JSONObject) ((JSONArray) object.get("Data")).get(0)).get("n1Position");
				continue;
			case -203:
			default:
				break;
			}
			break;
		}
		int pages = 1;
		for (int i = 1; i <= pages; ++i)
		{
			JSONObject object = null;
			boolean completed;
			do
			{
				completed = true;
				try
				{
					object = list(i);
				}
				catch (HttpRetryException e)
				{
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie1);
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie2);
					cookieManager.getCookieStore().add(new URI("http://kart.nexon.com/"), cookie3);
					completed = false;
				}
			} while (!completed);
			pages = (int) ((long) object.get("TotalCount") + 5) / 6;
			JSONArray array = (JSONArray) object.get("Data");
			for (Object obj : array)
			{
				JSONObject item = (JSONObject) obj;
				csvPrinter.printRecord(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.valueOf(((String) item.get("dtCreate")).replaceAll("[^\\d]", "")))), nameMap.get(item.get("n4ItemNo")), cntMap.containsKey(item.get("n4ItemNo")) ? cntMap.get(item.get("n4ItemNo")) : "-", "2019년 10월 9일(수) 23시 59분까지", item.get("strCouponSN"));
			}
		}
		csvPrinter.flush();
		csvPrinter.close();
	}
	
	public static JSONObject play(int position) throws Exception
	{
		HttpURLConnection conn = (HttpURLConnection) new URL("http://kart.nexon.com/events/2019/0911/AjaxYutNori.aspx").openConnection();
		Map<String, String> parameters = new HashMap<>();
		parameters.put("strType", "apply");
		parameters.put("PositionCur", String.valueOf(position));
		StringJoiner sj = new StringJoiner("&");
		for (Entry<String, String> entry : parameters.entrySet()) sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
		conn.setRequestMethod("POST");
		conn.setFixedLengthStreamingMode(out.length);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Referer", "http://kart.nexon.com/events/2019/0911/Event.aspx");
		conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		conn.setDoOutput(true);
		DataOutputStream os = new DataOutputStream(conn.getOutputStream());
		os.write(out);
		os.flush();
		os.close();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		StringBuffer response = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) response.append(line);
		br.close();
		conn.disconnect();
		System.out.println(response);
		return (JSONObject) jsonParser.parse(response.toString());
	}
	
	public static JSONObject list(int page) throws Exception
	{
		HttpURLConnection conn = (HttpURLConnection) new URL("http://kart.nexon.com/events/2019/0911/AjaxYutNori.aspx").openConnection();
		Map<String, String> parameters = new HashMap<>();
		parameters.put("strType", "list");
		parameters.put("PageNo", String.valueOf(page));
		StringJoiner sj = new StringJoiner("&");
		for (Entry<String, String> entry : parameters.entrySet()) sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
		conn.setRequestMethod("POST");
		conn.setFixedLengthStreamingMode(out.length);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Referer", "http://kart.nexon.com/events/2019/0911/Event.aspx");
		conn.setDoOutput(true);
		DataOutputStream os = new DataOutputStream(conn.getOutputStream());
		os.write(out);
		os.flush();
		os.close();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		StringBuffer response = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null) response.append(line);
		br.close();
		conn.disconnect();
		System.out.println(response);
		return (JSONObject) jsonParser.parse(response.toString());
	}
}