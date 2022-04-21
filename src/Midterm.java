import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

public class Midterm {
    private String input_file;
    private String query;
    private int numberOfDoc;
    public Midterm(String file, String query){
        this.input_file = file;
        this.query = query;
    }

    public void printResult(String[] result){
        int count = 0;
        for(int i = 0 ; i < 3; i++){
            if(Double.parseDouble(result[i*2+1])!=0.00){
                System.out.println(""+result[i*2] + " : " + result[i*2+1]);
                count++;
            }
        }
        if(count == 0)
            System.out.println("검색된 문서가 없습니다.");
    }

    public String[] showSnippet() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document doc = docBuilder.parse("./collection.xml");//id 갯수를 셀 index.xml 파일 열기

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//docs/doc", doc, XPathConstants.NODESET);
        numberOfDoc = nodes.getLength();

        //형태소 분석을 위한 객체 생성
        KeywordList kl;
        KeywordExtractor ke = new KeywordExtractor();
        kl = ke.extractKeyword(query, true);//query 형태소 분석
        Keyword kwrd;
        String[] keywordOfQuery = new String[kl.size()];

        for (int i = 0; i < kl.size(); i++) {
            kwrd = kl.get(i);
            keywordOfQuery[i] = kwrd.getString();
        }
        String bodyData[] = new String[numberOfDoc];
        for (int i = 0 ; i < numberOfDoc ; i++) {
            nodes = (NodeList) xpath.evaluate("//docs/doc[@id = '" + i + "']/body", doc, XPathConstants.NODESET);
            bodyData[i] = nodes.item(0).getTextContent();
        }
        String[] result = new String[numberOfDoc*3];
        int[] resultIndex = new int[numberOfDoc];
        int[] resultPoint = new int[numberOfDoc];
        int[] temp_resultPoint = new int[numberOfDoc];
        String[] snippet = new String[numberOfDoc];
        String check;
        String temp;
        for(int i = 0 ; i < numberOfDoc ; i++) {
            int move = 0;
            int c = 0;
            check = bodyData[i].substring(move, move + 29);
            temp = check;
            for (int j = 0; j < kl.size(); j++) {
                while (temp.contains(keywordOfQuery[j])) {
                    int index = temp.indexOf(keywordOfQuery[j]);
                    temp_resultPoint[i]++;
                    temp = check.substring(index + 1);
                }
            }
            if(temp_resultPoint[i]>resultPoint[i]) {
                resultPoint[i]=temp_resultPoint[i];
                resultIndex[i] = move;
            }
            move++;
        }
        return result;
    }
}
