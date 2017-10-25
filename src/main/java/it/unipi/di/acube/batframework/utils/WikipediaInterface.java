package it.unipi.di.acube.batframework.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public abstract class WikipediaInterface {
	/**
	 * Get the unique Wikipedia ID of a page, given its title. Redirects are not resolved.
	 * @param title the wikipedia article title. (Eg. "Barack Obama")
	 * @return the wikipedia id for the article, or -1 if the article was not found.
	 * @throws IOException if an error happened while retrieving data.
	 */
	public abstract int getIdByTitle(String title) throws IOException;

	/**Get the title of a Wikipedia article given its Wikipedia ID.
	 * @param wid the Wikipedia ID.
	 * @return the title of the page whose ID is {@code wid}, or {@code null} if such a title does not exist.
	 * @throws IOException if an error happened while retrieving data.
	 */
	public abstract String getTitlebyId(int wid) throws IOException;

	/** Check if a Wikipedia ID is a redirect.
	 * @param wid a Wikipedia ID
	 * @return true iff the page with {@code wid} is a redirect.
	 * @throws IOException if an error happened while retrieving data.
	 */
	public abstract boolean isRedirect(int wid) throws IOException;

	/**De-reference a Wikipedia ID resolving redirections, if any.
	 * @param wid a Wikipedia ID.
	 * @return {@code -1} if {@code wid} does not exist; {@code wid} if the page is not a redirect; the Wikipedia ID of the page pointed by the redirection otherwise.
	 * @throws IOException if an error happened while retrieving data.
	 */
	public abstract int dereference(int wid) throws IOException;

	/**If it makes things faster, prefetch the information (id, redirect, ...) of a set of Wikipedia Titles for faster caching.
	 * @param titlesToPrefetch the titles to prefetch.
	 * @throws IOException is the query processing failed.
	 * @throws ParserConfigurationException if the reply parsing failed.
	 * @throws SAXException if the reply parsing failed.
	 * @throws XPathExpressionException if the reply parsing failed.
	 */
	public abstract void prefetchTitles(List<String> titlesToPrefetch)
	        throws IOException, ParserConfigurationException, SAXException, XPathExpressionException;

	/**If it makes things faster, orefetch the information (title, redirects) of a list of Wikipedia IDs.
	 * @param widsToPrefetch the IDs to prefetch.
	 * @throws IOException is the query processing failed.
	 * @throws ParserConfigurationException if the reply parsing failed.
	 * @throws SAXException if the reply parsing failed.
	 * @throws XPathExpressionException if the reply parsing failed.
	 */
	public abstract void prefetchWids(List<Integer> widsToPrefetch)
	        throws IOException, ParserConfigurationException, SAXException, XPathExpressionException;

	public abstract void flush() throws FileNotFoundException, IOException;

	/**
	 * Normalize a title of a Wikipedia page, replacing substrings formed by underscores `_' to one single space ` '.
	 * 
	 * @param title
	 *            the title to normalize.
	 * @return the normalized title.
	 */
	public static String normalize(String title) {
		return title.replaceAll("_+", " ");
	}
}
