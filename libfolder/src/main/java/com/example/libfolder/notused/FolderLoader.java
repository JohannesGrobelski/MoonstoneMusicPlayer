package com.example.libfolder.notused;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.util.ArrayList;
//import java.util.List;
//
//
//import org.w3c.dom.*;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.OutputKeys;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
///** save RootFolder as xml file and load RootFolder from xml file
// * Format:
// * <root>
// *    <folder path="root/child1">
// *      <song
// *        id=-1
// *        title=...
// *        artist=...
// *        uri=root/child1/bla.mp3
// *        duration_ms=...
// *        lastPosition=...
// *        genre=...
// *        lyrics=...
// *        meaning=.../>
// *    </folder>
// *
// *    <folder path="root/child2">
// *      ...
// *    </folder>
// *
// *    ...
// * </root>
// * */
//public class FolderLoader {
//  private static final boolean DEBUG = false;
//  private static final String TAG = FolderLoader.class.getSimpleName();
//
//  private static final String xml_filename = "folders.xml";
//
//  public static void saveIntoXML(Folder rootFolder, String appFileDir){
//    //create a document using DocumentBuilder
//    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
//    DocumentBuilder documentBuilder = null;
//    try {
//      documentBuilder = documentFactory.newDocumentBuilder();
//      Document document = documentBuilder.newDocument();
//
//      /* create root */
//      Element root = document.createElement("root");
//      document.appendChild(root);
//
//      // root element
//      for(Folder childFolder: rootFolder.getChildren_folders()){
//        if(childFolder != null){
//          appendChildFolderToParentNodeInDocument(document,root,childFolder);
//        }
//      }
//
//      if(DEBUG)Log.d(TAG,"saved XML File");
//      //Log.d(TAG,documentToString(document));
//      saveDocument(document,appFileDir);
//    } catch (ParserConfigurationException e) {
//      e.printStackTrace();
//    }
//  }
//
//  /**
//   * create childNode from childFolder
//   * and appends the childNode to parentNode
//   * and call it recursively to do the same for all children of childFolder
//   * also append all songs in childFolder to childNode
//   * @param document
//   * @param parentNode
//   * @param folder
//   */
//  private static void appendChildFolderToParentNodeInDocument(Document document, Node parentNode, Folder folder){
//    //create childNode from childFolder
//    Element childNode = document.createElement("folder");
//    Attr attrPath = document.createAttribute("path"); attrPath.setValue(folder.getName());
//    childNode.setAttributeNode(attrPath);
//
//    //append the childNode to parentNode
//    parentNode.appendChild(childNode);
//
//    //recursively append all childrenFolders of folder to childNode
//    for(Folder childFolder: folder.getChildren_folders()){
//      if(childFolder != null)appendChildFolderToParentNodeInDocument(document,childNode,childFolder);
//    }
//
//    //recursively append all childrenSongs of folder to childNode
//    for(Song childSong: folder.getChildren_songs()){
//      if(childSong != null)appendChildSongToParentNodeInDocument(document,childNode,childSong);
//    }
//  }
//
//  /**
//   * create childNode from Song and appends it to parentNode
//   * <song
//   *       id=-1
//   *       title=...
//   *       artist=...
//   *       uri=... (here it is the same as path)
//   *       duration_ms=...
//   *       lastPosition=...
//   *       genre=...
//   *       lyrics=...
//   *       meaning=.../>
//   * @param document
//   * @param parentNode
//   * @param song
//   */
//  private static void appendChildSongToParentNodeInDocument(Document document, Node parentNode, Song song){
//    //create childNode from childFolder
//    Element childSong = document.createElement("song");
//    Attr attrId = document.createAttribute("id");
//      attrId.setValue(String.valueOf(song.getID()));
//    Attr attrTitle = document.createAttribute("title");
//      attrTitle.setValue(song.getName());
//    Attr attrArtist = document.createAttribute("artist");
//      attrArtist.setValue(song.getArtist());
//    Attr attrUri = document.createAttribute("uri");
//      attrUri.setValue(song.getURI());
//    Attr attrDuration_ms = document.createAttribute("duration_ms");
//      attrDuration_ms.setValue(String.valueOf(song.getDuration_ms()));
//    Attr attrLastposition = document.createAttribute("lastPosition");
//      attrLastposition.setValue(String.valueOf(song.getLastPosition()));
//    Attr attrGenre = document.createAttribute("genre");
//      attrGenre.setValue(song.getGenre());
//    Attr attrLyrics = document.createAttribute("lyrics");
//      attrLyrics.setValue(song.getLyrics());
//    Attr attrMeaning = document.createAttribute("meaning");
//      attrMeaning.setValue(song.getMeaning());
//
//    childSong.setAttributeNode(attrId);
//    childSong.setAttributeNode(attrTitle);
//    childSong.setAttributeNode(attrArtist);
//    childSong.setAttributeNode(attrUri);
//    childSong.setAttributeNode(attrDuration_ms);
//    childSong.setAttributeNode(attrLastposition);
//    childSong.setAttributeNode(attrGenre);
//    childSong.setAttributeNode(attrLyrics);
//    childSong.setAttributeNode(attrMeaning);
//
//    //append the childNode to parentNode
//    parentNode.appendChild(childSong);
//  }
//
//  /** opens xml file,
//   * parses it into String xmlstring
//   * and load root Folder from it by calling loadFromXML(xmlstring)
//   * @param context
//   * @return rootFolder
//   */
//  public static Folder loadFromXML(Context context){
//    //Dateiname in der Text gespeichert wird
//    Folder root = null;
//
//    FileInputStream fileInputStream;
//    try {
//      String path = context.getFilesDir().getAbsolutePath();
//      if(DEBUG)Log.d(TAG,"load file: "+path+"/"+xml_filename);
//      if(new File(path+"/"+xml_filename).exists()){
//        fileInputStream = new FileInputStream (new File(path+"/"+xml_filename));//wrong: context.openFileInput(path+"/"+xml_filename);
//        InputStream inputStream = new BufferedInputStream(fileInputStream);
//        String xmlString = convertStreamToString(inputStream);
//        root = loadFromXML(xmlString);
//        fileInputStream.close();
//      } else {
//        if(DEBUG)Log.e(TAG,"xml-file does not exist: "+path+"/"+xml_filename);
//      }
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return root;
//  }
//
//  /**
//   * open xml and transfer it into folder by calling getFolderFromNode for root node
//   * @return
//   */
//  private static Folder loadFromXML(String xmlString){
//    Folder rootFolder = null;
//
//    //Document mithilfe der DocumentBuilderFactory bauen
//    Document document;
//    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//
//    try {
//      //baue DocumentBuilder aus DocumentBuilderFactory
//      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//
//      //erstelle inputsource aus StringReader von xmlString
//      InputSource inputSource = new InputSource();
//      inputSource.setCharacterStream(new StringReader(xmlString));
//
//      //parse document aus inputsource
//      document = documentBuilder.parse(inputSource);
//
//      if(DEBUG)Log.d(TAG,"document: "+documentToString(document));
//
//      //Anfordern der Notelist aus Document
//      NodeList rootNodeList = document.getElementsByTagName("root");
//
//      if(rootNodeList.getLength() == 1){
//        Node root = rootNodeList.item(0);
//        rootFolder = new Folder("root",null,null,null,null);
//
//        List<Folder> children_folders = new ArrayList<>();
//        for(int i=0; i<root.getChildNodes().getLength(); i++){ //go through child nodes (sd-cards)
//          Folder childFolder = getFolderFromNode(root.getChildNodes().item(i));
//          if(childFolder != null)children_folders.add(childFolder);
//        }
//        if(children_folders.size() == 0){
//          rootFolder.setChildren_folders(null);
//        } else {
//          rootFolder.setChildren_folders(children_folders.toArray(new Folder[children_folders.size()]));
//        }
//        rootFolder.setParentsBelow();
//      } else {
//        Log.e("FolderLoader","multiple root folders!");
//      }
//      return rootFolder;
//    } catch (ParserConfigurationException | SAXException | IOException e) {
//      Log.e(TAG, e.getMessage());
//    }
//    return null;
//  }
//
//  /**
//   * checks if node is a folder, then gets children-folder and children-songs
//   * and creates a folder
//   * @param node
//   * @return
//   */
//  private static Folder getFolderFromNode(Node node){
//    Folder folder = null;
//    if(node.getNodeName().equals("folder")){
//      String name = node.getAttributes().getNamedItem("path").getNodeValue();
//      List<Folder> childrenFolders = new ArrayList<>();
//      if(DEBUG)Log.d(TAG,"getFolderFromNode: folder1: "+name+" children: "+node.getChildNodes().getLength());
//      for(int i=0; i<node.getChildNodes().getLength(); i++){ //load all child folder recursively
//        Node childNode = node.getChildNodes().item(i);
//        if(childNode.getNodeName().equals("folder")){
//          if(DEBUG)Log.d(TAG,"getFolderFromNode: folder: "+name+" has child: "+childNode.getAttributes().getNamedItem("path").getNodeValue());
//          Folder childFolder = getFolderFromNode(node.getChildNodes().item(i));
//          if(childFolder!=null){
//            childrenFolders.add(childFolder);
//            if(DEBUG)Log.d(TAG,"getFolderFromNode subfolder loaded: "+childFolder.getName());
//          }
//        }
//      }
//
//      List<Song> childrenSongs = new ArrayList<>();
//      for(int i=0; i<node.getChildNodes().getLength(); i++){
//        Node childNode = node.getChildNodes().item(i);
//        if(childNode.getNodeName().equals("song")){
//          Song childSong = getSongFromNode(node.getChildNodes().item(i));
//          if(childSong!=null)childrenSongs.add(childSong);
//        }
//      }
//
//      Folder[] childFolderArray = childrenFolders.toArray(new Folder[childrenFolders.size()]);
//      if(childrenFolders.size() == 0)childFolderArray = null;
//      Song[] childSongArray = childrenSongs.toArray(new Song[childrenSongs.size()]);
//      if(childrenSongs.size() == 0)childSongArray = null;
//
//
//      if(DEBUG)Log.d(TAG,"getFolderFromNode parsed Folder ("+folder.getName()+"):\n////"+folder.toString());
//
//      return folder;
//    } else { //node is not a folder
//      return null;
//    }
//  }
//
//  /**
//   * <song
//   *        path="root/child1/bla.mp3"
//   *        id=-1
//   *        title="..."
//   *        artist="..."
//   *        uri="..."
//   *        duration_ms=...
//   *        lastPosition=...
//   *        genre="..."
//   *        lyrics="..."
//   *        meaning="..."/>
//   * @param node
//   * @return
//   */
//  private static Song getSongFromNode(Node node){
//    Song song;
//    if(node.getNodeName().equals("song")) {
//      String id = node.getAttributes().getNamedItem("id").getNodeValue();
//      String title = node.getAttributes().getNamedItem("title").getNodeValue();
//      String artist = node.getAttributes().getNamedItem("artist").getNodeValue();
//      String uri = node.getAttributes().getNamedItem("uri").getNodeValue();
//      String duration_ms = node.getAttributes().getNamedItem("duration_ms").getNodeValue();
//      String lastPosition = node.getAttributes().getNamedItem("lastPosition").getNodeValue();
//      String genre = node.getAttributes().getNamedItem("genre").getNodeValue();
//      String lyrics = node.getAttributes().getNamedItem("lyrics").getNodeValue();
//      String meaning = node.getAttributes().getNamedItem("meaning").getNodeValue();
//      if(DEBUG)Log.d(TAG,"getSongFromNode: "+uri);
//
//      song = new Song(Integer.valueOf(id),
//          title,
//          artist,
//          uri,
//          Integer.valueOf(duration_ms),
//          Integer.valueOf(lastPosition),
//          genre,
//          lyrics,
//          meaning);
//      return song;
//    } else {
//      return null;
//    }
//  }
//
//  private static String convertStreamToString(InputStream inputStream) {
//    InputStream in;
//    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//    StringBuilder stringBuilder = new StringBuilder();
//    String line = "";
//    try {
//      while ((line = bufferedReader.readLine()) != null) {
//        stringBuilder.append(line).append('\n////');
//      }
//    } catch (IOException ex) {
//      ex.printStackTrace();
//    } finally {
//      try{
//        inputStream.close();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
//    return stringBuilder.toString();
//  }
//
//  /** transforms Document to string.
//   * source: https://stackoverflow.com/a/2567428.
//   * @param doc
//   * @return
//   */
//  public static String documentToString(Document doc) {
//    try {
//      StringWriter sw = new StringWriter();
//      TransformerFactory tf = TransformerFactory.newInstance();
//      Transformer transformer = tf.newTransformer();
//      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//
//      transformer.transform(new DOMSource(doc), new StreamResult(sw));
//      return sw.toString();
//    } catch (Exception ex) {
//      throw new RuntimeException("Error converting to String", ex);
//    }
//  }
//
//  public static void saveDocument(Document document, String appFileDir) {
//    try {
//      //domsource create
//      DOMSource domSource = new DOMSource(document);
//
//      System.out.println(documentToString(document));
//
//      // create the xml file
//      StreamResult streamResult = new StreamResult(new File(appFileDir+"/"+ xml_filename));
//
//      // transform the DOM Object to an XML File
//      TransformerFactory transformerFactory = TransformerFactory.newInstance();
//      Transformer transformer = transformerFactory.newTransformer();
//      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
//      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//
//      transformer.transform(domSource, streamResult);
//      if(DEBUG)Log.d(TAG,"saved file to: "+appFileDir+"/"+xml_filename);
//    } catch (Exception ex) {
//      throw new RuntimeException("Error converting to String", ex);
//    }
//  }
//
//
//}
//