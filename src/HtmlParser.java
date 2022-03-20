import org.jsoup.Jsoup;
import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class HtmlParser {

    int count = 0;
    File result[] = new File[1];

    public File[] findHtml(String dirPath) {//html확장자를 가진 파일들을 찾는 함수
        File dir = new File(dirPath);
        File files[] = dir.listFiles();//모든 파일들의 목록을 배열에 저장

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {//만약 찾은 파일이 디렉토리라면 다시 함수가 자신을 다시 호출
                findHtml(file.getPath());
            } else if (file.getName().endsWith(".html")){//.html로 끝나는 파일이면 전역변수에 있는 파일 배열에 저장
                if(count == result.length) {//전역변수로 선언된 result에 파일을 저장할 배열의 크기가 모자라면 크기를 늘려주는 효과를 주는 과정을 거친다.
                    File[] temp = Arrays.copyOf(result, result.length +1);//원본배열의 내용을 복사하고 배열의 크기가 하나 더 큰 임시배열을 만든다.
                    result = temp;//result에 크기가 큰 임시배열을 다시 입력한다.
                }
                result[count] = file;
                count++;
            }
        }
        return result;//.html로 끝나는 파일들의 배열을 반환한다.
    }

    //html 파일 리스트와 xml 파일을 생성할 경로를 입력받으면 하나의 통합된 xml파일을 해당 경로에 생성
    public void writeXml(File[] file, String path) throws IOException, ParserConfigurationException, TransformerException {
        //문서 생성
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document document = docBuilder.newDocument(); // Document 생성

        //문서 내용 작성
        Element docs = document.createElement("docs");// element 객체 docs 생성
        document.appendChild(docs);//  document 아래에 docs를 붙인다.

        for(int i = 0 ; i < file.length ; i++) {//html파일의 갯수만큼 반복
            org.jsoup.nodes.Document html = Jsoup.parse(file[i],"UTF-8");//html 파일 파싱
            String titleData = html.title();//title 내용 저장
            String bodyData = html.body().text();//body 내용 저장

            Element doc = document.createElement("doc");//element 객체 doc 생성
            docs.appendChild(doc);//doc 하위에 붙인다.
            doc.setAttribute("id",Integer.toString(i));/// 속성 부여

            Element title = document.createElement("title");//element 객체 title 생성
            title.appendChild(document.createTextNode(titleData));
            doc.appendChild(title);//doc 하위에 붙인다.

            Element body = document.createElement("body");//element 객체 body 생성
            body.appendChild(document.createTextNode(bodyData));
            doc.appendChild(body);//doc 하위에 붙인다.
        }

        //xml 파일로 쓰기
        TransformerFactory trfFactory = TransformerFactory.newInstance();
        Transformer trformer = trfFactory.newTransformer();

        trformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
        trformer.setOutputProperty(OutputKeys.INDENT, "yes");
        trformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new FileOutputStream(new File(path)));

        trformer.transform(source, result);
    }

    //xml파일의 바디 태그의 내용을 형태소만 남기고 각 형태소의 등장횟수들로 내용을 수정한다.
    //가공할 xml파일의 경로를 입력받으면 해당 xml파일을 가공하여 같은 경로에 다시 저장한다.
    public void processXml(String path) throws TransformerException, ParserConfigurationException, XPathExpressionException, IOException, SAXException {
        //문서생성
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document doc = docBuilder.parse(path);

        NodeList nodes = null;
        String bodyData = "";
        KeywordList kl;
        KeywordExtractor ke = new KeywordExtractor();
        XPath xpath = XPathFactory.newInstance().newXPath();

        for (int i = 0; i < result.length; i++) {//html 파일의 갯수만큼 문서 안에 doc이 생성되었기 때문에 해당 횟수만큼 반복
            nodes = (NodeList) xpath.evaluate("//docs/doc[@id = '" + i + "']/body", doc, XPathConstants.NODESET);//docs아래의 doc중 id가 i인 body를 노드에 넣는다
            bodyData = nodes.item(0).getTextContent();//해당 노드의 내용을 bodyData에 넣는다.
            kl = ke.extractKeyword(bodyData, true);//bodyData 형태소 분석
            bodyData = "";
            for (int j = 0; j < kl.size(); j++) {//분석된 형태소를 문자열로 가공
                Keyword kwrd = kl.get(j);
                bodyData = bodyData.concat(kwrd.getString() + ":" + kwrd.getCnt());
                if(j!=kl.size()-1) bodyData = bodyData.concat("#");
            }
            nodes.item(0).setTextContent(bodyData);//최종 결과를 body의 내용으로 입력
        }
        // 수정된 xml파일 저장
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(new DOMSource(doc), new StreamResult(new File(path)));
    }
}

