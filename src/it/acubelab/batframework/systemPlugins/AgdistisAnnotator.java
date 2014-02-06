package it.acubelab.batframework.systemPlugins;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.problems.D2WSystem;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * D2W annotator that uses HITS on DBpedia Graph.
 * 
 * @see <a href="https://github.com/AKSW/AGDISTIS">https://github.com/AKSW/AGDISTIS</a>
 */
public class AgdistisAnnotator implements D2WSystem {
	private static final JSONParser JSON_PARSER = new JSONParser();

	private long calib = -1;
	private long lastTime = -1;

	private final String host;
	private final int port;
	private final WikipediaApiInterface wikiApi;

	public AgdistisAnnotator(String host, int port, WikipediaApiInterface wikiApi) {
		this.host = host;
		this.port = port;
		this.wikiApi = wikiApi;
	}

	public AgdistisAnnotator(WikipediaApiInterface wikiApi) {
		this("139.18.2.164", 8080, wikiApi);
	}

	@Override
	public String getName() {
		return "Agdistis";
	}

	@Override
	public long getLastAnnotationTime() {
		if (calib == -1)
			calib = TimingCalibrator.getOffset(this);
		return lastTime - calib > 0 ? lastTime - calib : 0;
	}

	@Override
	public Set<Annotation> solveD2W(String text, Set<Mention> mentions) throws AnnotationException {
		String textWithMentions = createTextWithMentions(text, mentions);
		try {
			return getAnnotations(textWithMentions);
		} catch (IOException | ParseException e) {
			throw new AnnotationException(e.getMessage());
		}
	}

	public Set<Annotation> getAnnotations(String textWithMentions) throws IOException, ParseException {
		URL agdistisUrl = new URL("http://" + host + ":" + port + "/AGDISTIS");
		String parameters = "type=agdistis&text=" + URLEncoder.encode(textWithMentions, "UTF-8");
		HttpURLConnection slConnection = (HttpURLConnection) agdistisUrl.openConnection();
		slConnection.setDoOutput(true);
		slConnection.setDoInput(true);
		slConnection.setRequestMethod("POST");
		slConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		slConnection.setRequestProperty("charset", "utf-8");
		slConnection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
		slConnection.setUseCaches(false);

		DataOutputStream wr = new DataOutputStream(slConnection.getOutputStream());
		wr.writeBytes(parameters);
		wr.flush();
		wr.close();

		InputStream in = slConnection.getInputStream();
		Set<Annotation> annotations = parseJsonStream(in);
		return annotations;
	}

	private Set<Annotation> parseJsonStream(InputStream in) throws IOException, ParseException {
		Set<Annotation> annotations = new HashSet<>();

		JSONArray namedEntities = (JSONArray) JSON_PARSER.parse(new InputStreamReader(in, "UTF-8"));
		for (Object obj : namedEntities) {
			JSONObject namedEntity = (JSONObject) obj;

			long start = (long) namedEntity.get("start");
			long offset = (long) namedEntity.get("offset");
			int position = (int) start;
			int length = (int) offset;

			String url = (String) namedEntity.get("disambiguatedURL");
			if (url == null) {
				// String mention = (String) namedEntity.get("namedEntity");
				// System.err.printf("No entity for \"%s\" at position %d%n", mention, position);
				continue;
			}

			String urlDecoded = URLDecoder.decode(url, "UTF-8");
			String title = extractLabel(urlDecoded);
			int wikiArticle = wikiApi.getIdByTitle(title);
			if (wikiArticle == -1)
				System.err.printf("Wiki title %s of url %s (decoded %s) could not be found.%n", title, url, urlDecoded);
			annotations.add(new Annotation(position, length, wikiArticle));
		}

		return annotations;
	}

	private static String extractLabel(String namedEntityUri) {
		int posSlash = namedEntityUri.lastIndexOf('/');
		int posPoints = namedEntityUri.lastIndexOf(':');
		if (posSlash > posPoints) {
			return namedEntityUri.substring(posSlash + 1);
		} else if (posPoints < posSlash) {
			return namedEntityUri.substring(posPoints + 1);
		} else {
			return namedEntityUri;
		}
	}

	static String createTextWithMentions(String text, Set<Mention> mentionsSet) {
		// Example: 'The <entity>University of Leipzig</entity> in <entity>Barack Obama</entity>.'

		List<Mention> mentions = new ArrayList<>(mentionsSet);
		Collections.sort(mentions, new Comparator<Mention>() {
			@Override
			public int compare(Mention left, Mention right) {
				return Integer.compare(left.getPosition(), right.getPosition());
			}
		});

		StringBuilder textBuilder = new StringBuilder();
		int lastPos = 0;
		for (int i = 0; i < mentions.size(); i++) {
			Mention m = mentions.get(i);

			int begin = m.getPosition();
			int end = m.getPosition() + m.getLength();

			if (begin < lastPos) {
				// we have two overlapping mentions --> take the larger one
				Mention prev = mentions.get(i - 1);
				assert (m.overlaps(prev));
				System.err.printf("\"%s\" at pos %d overlaps with \"%s\" at pos %d%n", getMentionLabel(m, text),
						m.getPosition(), getMentionLabel(prev, text), prev.getPosition());
				if (m.getLength() > prev.getLength()) {
					// current is larger --> replace previous with current
					textBuilder.delete(textBuilder.length() - prev.getLength(), textBuilder.length());
					lastPos -= prev.getLength();
				} else
					// previous is larger or equal --> skip current
					continue;
			}
			String before = text.substring(lastPos, begin);
			String label = text.substring(begin, end);
			lastPos = end;
			textBuilder.append(before).append("<entity>" + label + "</entity>");
		}

		String lastSnippet = text.substring(lastPos, text.length());
		textBuilder.append(lastSnippet);

		return textBuilder.toString();
	}

	private static String getMentionLabel(Mention m, String text) {
		return text.substring(m.getPosition(), m.getPosition() + m.getLength());
	}

}
