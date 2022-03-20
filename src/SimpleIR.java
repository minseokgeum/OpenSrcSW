import org.xml.sax.SAXException;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


public class SimpleIR {
    public static void main(String args[]) throws ParserConfigurationException, IOException, TransformerException, XPathExpressionException, SAXException {

        String path_data = "/Users/minseok_geum/Desktop/오픈소스SW입문1/실습/SimpleIR/data";
        HtmlParser parser = new HtmlParser();
        File[] file = parser.findHtml(path_data);//file은 html 확장자를 가지는 파일들의 배열

        parser.writeXml(file, path_data+"/collection.xml");
        parser.processXml(path_data+"/collection.xml");
    }
}