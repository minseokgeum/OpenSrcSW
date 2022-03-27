import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class MakeCollection {
    private String data_path;
    private String output_file;

    public MakeCollection(String path) {
        this.data_path = path;
        this.output_file = data_path+"/collection.xml";
    }

    private int _count = 0;
    private File _result[] = new File[1];

    private void findHtml(String dirPath) {//html확장자를 가진 파일들을 찾는 함수
        File dir = new File(dirPath);
        File files[] = dir.listFiles();//모든 파일들의 목록을 배열에 저장

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {//만약 찾은 파일이 디렉토리라면 다시 함수가 자신을 다시 호출
                findHtml(file.getPath());
            } else if (file.getName().endsWith(".html")) {//.html로 끝나는 파일이면 전역변수에 있는 파일 배열에 저장
                if (_count == _result.length) {//전역변수로 선언된 result에 파일을 저장할 배열의 크기가 모자라면 크기를 늘려주는 효과를 주는 과정을 거친다.
                    File[] temp = Arrays.copyOf(_result, _result.length + 1);//원본배열의 내용을 복사하고 배열의 크기가 하나 더 큰 임시배열을 만든다.
                    _result = temp;//result에 크기가 큰 임시배열을 다시 입력한다.
                }
                _result[_count] = file;
                _count++;
            }
        }
    }

    //html 파일 리스트와 xml 파일을 생성할 경로를 입력받으면 하나의 통합된 xml파일을 해당 경로에 생성
    public void writeCollectionXml() throws IOException, ParserConfigurationException, TransformerException {
        findHtml(data_path);
        //문서 생성
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document document = docBuilder.newDocument(); // Document 생성
        //문서 내용 작성
        Element docs = document.createElement("docs");// element 객체 docs 생성
        document.appendChild(docs);//  document 아래에 docs를 붙인다.

        for(int i = 0 ; i < _result.length ; i++) {//html파일의 갯수만큼 반복
            org.jsoup.nodes.Document html = Jsoup.parse(_result[i],"UTF-8");//html 파일 파싱
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
        StreamResult result = new StreamResult(new FileOutputStream(new File(output_file)));
        trformer.transform(source, result);
    }
}