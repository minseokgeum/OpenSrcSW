import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

public class MakeKeyword {
    private String input_file;
    private String output_file;

    public MakeKeyword(String file) {
        this.input_file = file;
        this.output_file = file;
        //this.output_file = file + "/index.xml";
    }

    //xml파일의 바디 태그의 내용을 형태소만 남기고 각 형태소의 등장횟수들로 내용을 수정한다.
    //가공할 xml파일의 경로 입력하면 해당 xml파일을 가공하여 같은 디렉토리에 새로운 xml파일을 생성한다.
    public void writeIndexXml() throws TransformerException, ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        //문서생성
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document doc = docBuilder.parse("./collection.xml");
        //Document doc = docBuilder.parse(input_file+"/collection.xml");

        String bodyData = "";
        KeywordList kl;
        KeywordExtractor ke = new KeywordExtractor();
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xpath.evaluate("//docs/doc", doc, XPathConstants.NODESET);
        int numberOfDoc = nodes.getLength();

        for (int i = 0; i < numberOfDoc; i++) {//html 파일의 갯수만큼 문서 안에 doc이 생성되었기 때문에 해당 횟수만큼 반복
            nodes = (NodeList) xpath.evaluate("//docs/doc[@id = '" + i + "']/body", doc, XPathConstants.NODESET);//docs아래의 doc중 id가 i인 body를 노드에 넣는다
            bodyData = nodes.item(0).getTextContent();//해당 노드의 내용을 bodyData에 넣는다.
            kl = ke.extractKeyword(bodyData, true);//bodyData 형태소 분석
            bodyData = "";
            for (int j = 0; j < kl.size(); j++) {//분석된 형태소를 문자열로 가공
                Keyword kwrd = kl.get(j);
                bodyData = bodyData.concat("#"+kwrd.getString() + ":" + kwrd.getCnt());
            }
            nodes.item(0).setTextContent(bodyData);//최종 결과를 body의 내용으로 입력
        }
        // 수정된 xml파일 저장
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(doc), new StreamResult(new File(output_file)));
    }
}