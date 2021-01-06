package com.example.moonstonemusicplayer.utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * return lyrics for specified song (artist, title)
 * */
public class GooglesearchLyrics {

  public static void main(String[] a){
    searchlyrics("linkin park","forgotten");
  }

  /**
   * transforms HTML to Text by using JSOUP <br>
   * transforms <br> and <p> into linebreaks <br>
   * @param html
   * @return return text without html tags
   */
  public static String html2text(String html) {
    if(html==null)
      return html;
    Document document = Jsoup.parse(html);
    document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
    document.select("br").append("\\n");
    document.select("p").prepend("\\n\\n");
    String s = document.html().replaceAll("\\\\n", "\n");
    return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
  }

  /**
   * Builds Search Query, <br>
   * gets HTML-String of GoogleSearchResults (using getSearchContent(...)) <br>
   * transforms HTML-String to String and filters out song lyrics <br>
   * @param artist
   * @param title
   * @return lyrics of song (if available)
   */
  public static String searchlyrics(String artist, String title){
    String lyrics = "";
    try {
      String begin = title.toLowerCase();
      String end = "Quelle: ".toLowerCase();

      String webpage = html2text(getSearchContent(artist+" "+title+" lyrics"));
      String search = webpage.toLowerCase();

      if(search.contains("Quelle:".toLowerCase())){
        webpage = webpage.substring(0,search.lastIndexOf("Quelle:".toLowerCase()));
        search = webpage.toLowerCase();
      }
      if(search.contains(artist.toLowerCase())){
        webpage = webpage.substring(search.lastIndexOf(artist.toLowerCase()) + artist.toLowerCase().length());
      }

      if(!webpage.contains("<")){
        //TODO: check if webpage only contains lyrics
        lyrics = webpage;
      }
      System.out.println(lyrics);
      return lyrics;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Builds URL from googleSearchQuery {@link String} object,  <br>
   * specifys user agent,  <br>
   * opens an URL Connection for specified URL and user agent <br>
   * and transforms inputstream from url connection into String. <br>
   * @param googleSearchQuery the google search query
   * @return the content as {@link String} object
   * @throws Exception
   */
  public static String getSearchContent(String googleSearchQuery) throws Exception {
    //URL encode string in JAVA to use with google search
    System.out.println("Searching for: " + googleSearchQuery);
    googleSearchQuery = googleSearchQuery.trim();
    googleSearchQuery = URLEncoder
        .encode(googleSearchQuery, StandardCharsets.UTF_8.toString());
    String queryUrl = "https://www.google.com/search?q=" + googleSearchQuery + "&num=10";
    System.out.println(queryUrl);
    final String agent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
    URL url = new URL(queryUrl);
    final URLConnection connection = url.openConnection();
    /** User-Agent is mandatory otherwise Google will return HTTP responsecode: 403 */
    connection.setRequestProperty("User-Agent", agent);
    final InputStream stream = connection.getInputStream();
    return getString(stream);
  }

  /**
   * First transforms InputStream into BufferedReader and then <br>
   * goes line by line through bufferedreader and merges these lines by stringbuilder. <br>
   * @param inputStream
   * @return transforms inputstream into string.
   * @throws IOException
   */
  private static String getString(InputStream inputStream) throws IOException {
    String newLine = System.getProperty("line.separator");
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder result = new StringBuilder();
    boolean flag = false;
    for (String line; (line = reader.readLine()) != null; ) {
      result.append(flag? newLine: "").append(line);
      flag = true;
    }
    return result.toString();
  }
}
