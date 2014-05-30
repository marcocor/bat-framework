package it.acubelab.batframework.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FreebaseApi {
	private String key;
	File mid2WikiTitleCache = null;
	Object2ObjectArrayMap<String, String> mid2WikiTitle = null;
	int flushCounter = 0;

	@SuppressWarnings("unchecked")
	public FreebaseApi(String m2wFileName) {
		if (m2wFileName == null)
			// will not load/store the mapping to a file, keeping all the data
			// in memory
			mid2WikiTitle = new Object2ObjectArrayMap<String, String>();
		else {
			mid2WikiTitleCache = new File(m2wFileName);
			if (mid2WikiTitleCache.exists() && mid2WikiTitleCache.length() > 0)
				// load the cached data
				try {
					mid2WikiTitle = (Object2ObjectArrayMap<String, String>) new ObjectInputStream(
							new FileInputStream(mid2WikiTitleCache))
							.readObject();
				} catch (Exception e) {
					throw new RuntimeException(
							"Could not load cache file "
									+ mid2WikiTitleCache.getAbsolutePath()
									+ ". Try to manually delete the file to clear the cache. Message: "
									+ e.getMessage());
				}
			else
				// create a new empty mapping to fill in.
				mid2WikiTitle = new Object2ObjectArrayMap<String, String>();
		}

	}

	public FreebaseApi(String key, String file) {
		this(file);
		this.key = key;
	}

	public String midToTitle(String mid) throws IOException {
		if (mid.charAt(0) != '/')
			mid = "/" + mid;

		if (mid2WikiTitle.containsKey(mid))
			return mid2WikiTitle.get(mid);

		JSONObject response = jsonQuery(mid, false);
		JSONObject propJson = (JSONObject) response.get("property");
		if (propJson == null) { // See if this MID has been replaced...
			JSONObject replacedResponse = jsonQuery(mid, true);
			JSONObject replacedPropJson = (JSONObject) replacedResponse
					.get("property");
			JSONObject replacedTopicEqJson = (JSONObject) replacedPropJson
					.get("/dataworld/gardening_hint/replaced_by");
			JSONArray replValuesJson = (JSONArray) replacedTopicEqJson
					.get("values");
			JSONObject valObj = (JSONObject) replValuesJson.get(0);
			String replMid = (String) valObj.get("id");
			System.out.printf("WARNING: mid %s replaced by %s%n", mid, replMid);
			String title =  midToTitle(replMid);
			mid2WikiTitle.put(mid, title);
			return title;
		}

		JSONObject topicEqJson = (JSONObject) propJson
				.get("/common/topic/topic_equivalent_webpage");
		JSONArray valuesJson = (JSONArray) topicEqJson.get("values");

		String title = null;
		for (Object obj : valuesJson.toArray()) {
			JSONObject jsonObj = (JSONObject) obj;
			String value = ((String) jsonObj.get("text"));
			if (value
					.startsWith("http://en.wikipedia.org/wiki/index.html?curid="))
				continue;
			if (value.startsWith("http://en.wikipedia.org/wiki/")) {
				title = value.replace("http://en.wikipedia.org/wiki/", "")
						.replaceAll("_", " ");
				mid2WikiTitle.put(mid, title);
				break;
			}
		}
		return mid2WikiTitle.get(mid);
	}

	private JSONObject jsonQuery(String mid, boolean replaced)
			throws IOException {
		String url = String
				.format("https://www.googleapis.com/freebase/v1/topic%s?filter=%s&limit=0%s",
						mid, replaced ? "/dataworld/gardening_hint/replaced_by"
								: "/common/topic/topic_equivalent_webpage",
						key == null ? "" : "&key=" + key);
		System.out.println("Querying " + url);
		String charset = "UTF-8";

		URLConnection connection = new URL(url).openConnection();
		connection.setConnectTimeout(0);
		connection.setRequestProperty("Accept-Charset", charset);

		int status = ((HttpURLConnection) connection).getResponseCode();
		if (status != 200) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					((HttpURLConnection) connection).getErrorStream()));
			String line = null;
			while ((line = br.readLine()) != null)
				System.err.println(line);
			throw new RuntimeException();
		}

		InputStream is = connection.getInputStream();
		Scanner s = new Scanner(is);
		Scanner s2 = s.useDelimiter("\\A");
		String resultStr = s2.hasNext() ? s2.next() : "";

		s.close();
		s2.close();
		JSONObject obj = (JSONObject) JSONValue.parse(resultStr);

		increaseFlushCounter();
		return obj;
	}

	public void increaseFlushCounter() throws FileNotFoundException, IOException {
		flushCounter++;
		if (flushCounter % 10 == 0)
			flush();
	}

	public void flush() throws FileNotFoundException, IOException {
		if (mid2WikiTitleCache != null) {
			mid2WikiTitleCache.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(mid2WikiTitleCache));
			oos.writeObject(mid2WikiTitle);
			oos.close();
		}

	}
}
