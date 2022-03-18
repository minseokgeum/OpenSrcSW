import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;


public class SimpleIR {
    public static void main(String args[]) throws ParserConfigurationException, IOException, TransformerException {

        String path_data = "/Users/minseok_geum/Desktop/오픈소스SW입문1/실습/SimpleIR/data";
        HtmlParser parser = new HtmlParser();

        File[] file = parser.findHtml(path_data);//file은 html확장자를 가지는 파일들의 배열
        parser.writeDocs(file);
        parser.writeXml(path_data);
    }
}