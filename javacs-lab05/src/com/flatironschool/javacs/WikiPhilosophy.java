package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringTokenizer;

public class WikiPhilosophy {
	
	// you want only one wikifetcher object
	final static WikiFetcher fetcher = new WikiFetcher();

	// you want a list of urls that you have visited, only one no matter how many instances are run
	final static List<String> linksVisited = new ArrayList<String>();

	// to check if in parenthesis
	private static Deque<String> stackOfParens = new ArrayDeque<String>();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {	
        // you don't need much code in the main method, this method is only used to kick off the actual work 
		// initial setup

		// string storing inital url
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		// string storing where you want to end up
		String end = "https://en.wikipedia.org/wiki/Philosophy";
		// send to method to see if we can reach philosopy under 8 links
		findPhilosophy(url, end, 8);	
	}

	// method to actually see if you can reach philsophy by following links under 10 links
	public static void findPhilosophy (String urlSrc, String urlDest, int linkLimit) throws IOException {
		// only visit a url limit times
		int linkNum = 0;
		while (linkNum < linkLimit) {
			// check to make sure it hasn't already been visited
			if (linksVisited.contains(urlSrc)) {
				// failure, you are going around again
				// failure message and exit
				System.out.println("Hit a link twice");
				return;
			// otherwise, add current link to list of visited links
			} else {
				linksVisited.add(urlSrc);
			}
			Element firstLinkElement = getLink(urlSrc);
			// make sure you have been given an actual link
			if (firstLinkElement == null) {
				// no link? failure message and exit
				System.out.println("no first valid link");
				return;
			}
			// print out the link (element gives us an easy way to do this)
			System.out.println("LINK:" + firstLinkElement.text() + "/n");
			// grab actual link from 
			String nextUrl = firstLinkElement.attr("abs:href");
			// CHECK TO SEE IF IT"S THE FINAL LINK
			if (nextUrl.equals(urlDest)) {
				// yay success message and exit
				System.out.println("Yay we found it!");
			}
			urlSrc = nextUrl;
			linkNum++;
		}

	}


		// first make method to take initial link and grab the first paragraph in that link
		public static Element getLink(String urlSrc) throws IOException {
			// first grab the dom tree/elements of the url 
			//using wikifetcher code (jsoup and sleeping)
			Elements paragraphs = fetcher.fetchWikipedia(urlSrc);

			for (Element paragraph: paragraphs) {
				Element firstLinkInParagraph = getLinkInParagraph(paragraph);
				if (firstLinkInParagraph != null) {
					return firstLinkInParagraph;
				} 
				
			}
			return null;
		}


		private static Element getLinkInParagraph (Node paragraph) {
			stackOfParens = new ArrayDeque<String>();
			Iterable<Node> paragraphDom = new WikiNodeIterable(paragraph);
			for (Node currElement : paragraphDom) {
				if (currElement instanceof Element) {
					Element currElem = (Element) currElement;
					if (linkIsValid(currElem)) {
						return currElem;
					} else {
						return null;
					}
				}
				if (currElement instanceof TextNode) {
					StringTokenizer str = new StringTokenizer(((TextNode)currElement).text(), "()", true);
					while (str.hasMoreTokens()) {
						String currTok = str.nextToken();
						if (currTok.equals(")")) {
							if (stackOfParens.isEmpty()) {
								return null;
							}
							stackOfParens.pop();
							if (currTok.equals("(")) {
								stackOfParens.push(currTok);
							}
						}
					}
				}
			}
			return null;

		}

		private static boolean linkIsValid(Element linkToCheck) {
			// check if it's a link
			if ((linkToCheck.tagName().equals("a")) || (noParens()) || (noItalic(linkToCheck)) ) {
				return false;
			} else {
				return true;
			}
		}

		private static boolean noItalic (Element elem) {
		 	Element curr = elem;
			for  (curr = elem; curr != null; curr = curr.parent()) {
				if (curr.tagName().equals("em") || curr.tagName().equals("i")) {
					return false;
				}
			}
			return true;
		}

		private static boolean noParens() {
			return (stackOfParens.isEmpty());
		}
}
