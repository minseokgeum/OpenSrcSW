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
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;


public class Indexer{
    private String input_file;
    private String output_file;

    public Indexer(String file){
        this.input_file = file;
        this.output_file = file;
        //this.output_file = file + "/index.post";
    }
    //가중치 계산 코드
    public String calculateWeight(int tf ,int df_count, double N){
        return String.format("%.2f",tf*Math.log(N/df_count));//소수점 둘쨰자리까지 반올림
    }
    //키워드 분석을 하는 기준바디를 id값이 0인 바디부터 doc의 갯수만큼 다음 바디로 차례 지정해간다
    //기준바디로 지정이 되면 해당바디의 모든 키워드를 추출하여 각 키워드별로 기준바디 이후에 나오는 모든 바디에서(id값이 더 큰 바디에서) 해당 키워드에 대한 tf값을 조사하고 이를 hash맵에 저장
    @SuppressWarnings({"rawtypes", "unchecked", "nls"})//경고무시
    //각 키워드들의 가중치를 계산하여 역파일형태의 hashmap 생성
    public void makeHash() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, ClassNotFoundException {
        FileOutputStream fileStream = new FileOutputStream(output_file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileStream);
        HashMap weightMap = new HashMap();//hashmap 생성

        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document doc = docBuilder.parse("./index.xml");//가공할 index.xml 파일 열기
        //Document doc = docBuilder.parse(input_file+"/index.xml");//가공할 index.xml 파일 열기

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xpath.evaluate("//docs/doc", doc, XPathConstants.NODESET);
        double numberOfDoc = nodes.getLength();

        String bodyData = "";
        String[] word_frequency;
        String weightResultString="";
        int[] tfList = new int[(int)numberOfDoc];//하나의 키워드에 대해 해당 키워드가 특정 id에 존재한다면 인덱스 값을 그 id 값으로 하고 해당 id에서의 tf값을 저장

        for (int i = 0 ; i < numberOfDoc ; i++) {//기준태그를 선정
            nodes = (NodeList) xpath.evaluate("//docs/doc[@id = '" + i + "']/body", doc, XPathConstants.NODESET);
            bodyData = nodes.item(0).getTextContent();
            String[] splitedKeyword = bodyData.substring(1).split("#");//내용 중 가장 앞에 #을 제외한 후 #을 구분자로 키워드 분리

            for(int j = 0 ; j < splitedKeyword.length ; j++){//기준바디인 id i 안에 존재하는 모든 키워드를 사용
                weightResultString = "";
                word_frequency = splitedKeyword[j].split(":");
                int df_count = 1;//해당 키워드가 몇개의 doc에서 존재하는지 count

                if(!weightMap.containsKey(word_frequency[0])) {//이미 hashmap에 key로 존재하는 keyword인지 확인
                    String[] anotherKeyword;//찾는 키워드와 일치하는 문자열이 다른 id의 문서에 있을 때 그 값에 대한 tf값을 추출하기 위해 사용하는 문자열 배열
                    tfList[i] = Integer.parseInt(word_frequency[1]);//hashmap에 없는 key이므로 tfList에 첫 값으로 저장

                    for(int k = i + 1; k < numberOfDoc; k++) {//기준바디 i 이후의 다른 모든 id에서 단어 탐색
                        String findingKeyword = "#".concat(word_frequency[0]+":");//정확하게 일치하는 단어를 찾기 위해 "#keyword:"로 다른 문서에서 탐색
                        nodes = (NodeList) xpath.evaluate("//docs/doc[@id = '" + k + "']/body", doc, XPathConstants.NODESET);
                        bodyData = nodes.item(0).getTextContent();

                        if (bodyData.contains(findingKeyword)) {//일치하는 키워드가 있다면 그 키워드의 tf값을 추출하여 tfㅣist의 k번째 인덱스에 값을 입력, df_count도 1 증가
                            df_count++;
                            anotherKeyword = bodyData.substring(bodyData.indexOf(findingKeyword)+1).split("#");
                            anotherKeyword = anotherKeyword[0].split(":");
                            tfList[k] = Integer.parseInt(anotherKeyword[1]);
                        }else tfList[k] = 0;//값이 없다는 것을 표시하기 위해 0을 저장
                    }
                    int count = 0;
                    for(int k = 0 ; k < numberOfDoc ; k++) {
                        if(tfList[k]!=0) {
                            if (count == 0) {
                                weightResultString = weightResultString.concat(Integer.toString(k) + " " + calculateWeight(tfList[k], df_count, numberOfDoc));
                                count++;
                            } else weightResultString = weightResultString.concat(" " + Integer.toString(k) + " " + calculateWeight(tfList[k], df_count, numberOfDoc));
                        }
                        tfList[k]=0;
                    }
                    weightMap.put(word_frequency[0], weightResultString);//hashmap에 저장
                }
            }
        }
        //확인용 출력
        objectOutputStream.writeObject(weightMap);
        objectOutputStream.close();
        FileInputStream fileStream2 = new FileInputStream(output_file);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileStream2);

        Object object = objectInputStream.readObject();
        objectInputStream.close();
        System.out.println("읽어온 객체의 type -> "+object.getClass());

        HashMap hashMap = (HashMap)object;
        Iterator<String> it = hashMap.keySet().iterator();

        while(it.hasNext()){
            String key = it.next();
            String value = (String)hashMap.get(key);
            System.out.println(key+" -> "+value);
        }
    }
}