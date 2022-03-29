import org.xml.sax.SAXException;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class KuIR {
    public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, XPathExpressionException, SAXException, ClassNotFoundException {

        String command = args[0];
        String output_path = args[1];

        if(command.equals("-c")) {
            String path = args[2];
            MakeCollection collectionMaker = new MakeCollection(path, output_path); //path:읽어올 html 파일들이 존재하는 디렉토리, output_path:output될 파일을 저장할 디렉토리
            collectionMaker.writeCollectionXml();
        } else if(command.equals("-k")) {
            MakeKeyword keywordMaker = new MakeKeyword(output_path); //output_path:output된 파일들이 저장되어 있는 디렉토리
            keywordMaker.writeIndexXml();
        } else if(command.equals("-i")){
            Indexer indexer = new Indexer(output_path); //output_path:output된 파일들이 저장되어 있는 디렉토리
            indexer.makeHash();
        }
    }
}