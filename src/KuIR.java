import org.xml.sax.SAXException;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class KuIR {
    public static void main(String args[]) throws ParserConfigurationException, IOException, TransformerException, XPathExpressionException, SAXException {
        String command = args[0];
        String path = args[1];

        if(command.equals("-c")) {
            MakeCollection collectionMaker = new MakeCollection(path);
            collectionMaker.writeCollectionXml();
        }
        else if(command.equals("-k")) {
            MakeKeyword keywordMaker = new MakeKeyword(path);
            keywordMaker.writeIndexXml();
        }
    }
}