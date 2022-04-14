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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class Searcher {
    private String input_file;
    private String query;
    private int numberOfDoc;

    public Searcher(String file, String query){
        this.input_file = file;
        this.query = query;
    }

    private void mergeSort(double[] arrToSort, double[] targetArr,int start, int end) {
        if(start >= end) {
            return;
        }
        int mid = (start + end) / 2;
        this.mergeSort(arrToSort, targetArr, start, mid);
        this.mergeSort(arrToSort, targetArr, mid+1, end);

        this.merging(arrToSort, targetArr, start, mid, mid+1, end);
    }
    // 배열의 두 구간을 구간의 시작지점부터 차례로 값을 비교하며 정렬하여 합치는 함수
    private void merging(double[] arrToSort, double[] targetArr, int start1, int end1, int start2, int end2) {
        //start1~end1이 왼쪽 절반, start2~end2는 오른쪽 절반
        int p, q; // 왼쪽과 오른쪽의 현재 최소값을 가리키는 변수
        double temp[] = new double[(end2-start1)+1]; //합쳐진 결과를 저장하는 임시배열
        double temp2[] = new double[((end2-start1)+1)*2];
        int tempIndex = 0;
        p = start1;
        q = start2;
        // p 와 q 가 모두 각각의 범위안에 있어야 한다.
        while(p <= end1 && q <= end2) {
            if(arrToSort[q] <= arrToSort[p]) {
                temp[tempIndex] = arrToSort[p];
                temp2[tempIndex*2] = targetArr[p*2];
                temp2[tempIndex*2+1] = targetArr[p*2+1];
                tempIndex++; p++;
            }else {
                temp[tempIndex] = arrToSort[q];
                temp2[tempIndex*2] = targetArr[q*2];
                temp2[tempIndex*2+1] = targetArr[q*2+1];
                tempIndex++; q++;

            }
        }
        //한쪽 인덱스가 범위를 초과 했을때 나머지 한쪽의 값들을 전부 temp 배열에 추가(한쪽이 전부 정렬되어 합쳐졌을 때)
        if(p > end1) { //왼쪽은 전부 정렬된 상태, 오른쪽 나머지 temp 배열에 추가
            for(int i=q; i<=end2; i++) {
                temp[tempIndex] = arrToSort[i];
                temp2[tempIndex*2] = targetArr[i*2];
                temp2[tempIndex*2+1] = targetArr[i*2+1];
                tempIndex++;
            }
        }else { //오른쪽은 전부 정렬된 상태, 왼쪽 나머지 temp 배열에 추가
            for(int i=p; i<=end1; i++) {
                temp[tempIndex] = arrToSort[i];
                temp2[tempIndex*2] = targetArr[i*2];
                temp2[tempIndex*2+1] = targetArr[i*2+1];
                tempIndex++;
            }
        }
        // 합쳐진 temp 완성
        // 인덱스 start1~end2까지 정렬된 결과 temp를 원본배열에 복사
        for(int i=start1; i<=end2; i++) {
            arrToSort[i] = temp[i-start1];
            targetArr[i*2] = temp2[(i-start1)*2];
            targetArr[i*2+1] = temp2[(i-start1)*2+1];
        }
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

    private void innerProduct(double[] result, int[] tfOfQuery, String[] weightListOfQuery, int indexI, int indexJ){
        if (result.length == numberOfDoc)
            result[Integer.parseInt(weightListOfQuery[indexJ])] += tfOfQuery[indexI]*Double.parseDouble(weightListOfQuery[indexJ+1]);
        else if(result.length == 2*numberOfDoc)
            result[2*Integer.parseInt(weightListOfQuery[indexJ])+1] += tfOfQuery[indexI]*Double.parseDouble(weightListOfQuery[indexJ+1]);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "nls"}) //경고무시
    public String[] calcSim() throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException, XPathExpressionException {
        //id 갯수세기
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document doc = docBuilder.parse("./index.xml");//id 갯수를 셀 index.xml 파일 열기
        //Document doc = docBuilder.parse(input_file+"/index.xml");// id 갯수를 셀 index.xml 파일 열기

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//docs/doc", doc, XPathConstants.NODESET);
        numberOfDoc = nodes.getLength();

        //형태소 분석을 위한 객체 생성
        KeywordList kl;
        KeywordExtractor ke = new KeywordExtractor();
        kl = ke.extractKeyword(query, true);//query 형태소 분석
        Keyword kwrd;

        //index.post 해쉬맵 열기
        FileInputStream fileStream = new FileInputStream(input_file);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileStream);

        Object object = objectInputStream.readObject();
        objectInputStream.close();

        HashMap hashMap = (HashMap) object;

        //calculate similarity
        String[] keywordOfQuery = new String[kl.size()];//Query의 키워드값들을 저장하는 배열
        int[] tfOfQuery = new int[kl.size()];//Query의 각 키워드들의 tf값

        double lengthOfQueryVector = 0;//Query의 벡터 크기 계산
        double[] lengthOfIdVector = new double[numberOfDoc];//Query의 각 키워드들에 대한 id 백터의 크기 계산

        double[] resultSimilarity = new double[numberOfDoc * 2];//짝수 인덱스에는 id 값이, 짝수 인덱스 바로 다음 인덱스에는 해당 id의 유사도 값이 저장되어있다.
        double[] arrToSort = new double[numberOfDoc];//정렬용 배열, 인덱스 값이 id 값이며, 배열에 저장된 값은 Query와의 similarity 계산결과이다.

        for (int i = 0; i < numberOfDoc; i++) // id값 저장
            resultSimilarity[2 * i] = i;

        for (int i = 0; i < kl.size(); i++) {//Query의 각각의 키워드들에 대해 id 별 similarity를 계산한다.
            kwrd = kl.get(i);
            keywordOfQuery[i] = kwrd.getString();
            tfOfQuery[i] = kwrd.getCnt();

            lengthOfQueryVector += Math.pow(tfOfQuery[i], 2);

            if (hashMap.containsKey(keywordOfQuery[i])) {
                String[] weightListOfQuery = ((String) hashMap.get(keywordOfQuery[i])).split(" ");//weightListOfQuery : 짝수 인덱스에는 id 값이, 짝수 인덱스 바로 다음에는 해당 id의 weight값이 저장되어있다.
                for (int j = 0; j < weightListOfQuery.length; j += 2) {//현재 탐색하고 있는 Query의 키워드가 존재하는 id의 similarity에 해당 id에서의 weight값과 현재 키워드의 query에서의 tf값을 곱해서 더해준다.
                    innerProduct(resultSimilarity, tfOfQuery, weightListOfQuery, i, j);
                    innerProduct(arrToSort, tfOfQuery, weightListOfQuery, i, j);
                    /*resultSimilarity[2*Integer.parseInt(weightListOfQuery[j])+1] += tfOfQuery[i]*Double.parseDouble(weightListOfQuery[j+1]);
                    arrToSort[Integer.parseInt(weightListOfQuery[j])] += tfOfQuery[i]*Double.parseDouble(weightListOfQuery[j+1]);*/
                    lengthOfIdVector[Integer.parseInt(weightListOfQuery[j])] += Math.pow(Double.parseDouble(weightListOfQuery[j + 1]), 2);//벡터 크기 계산
                }
            }
            if (i == kl.size() - 1) {
                lengthOfQueryVector = Math.sqrt(lengthOfQueryVector);
                for (int j = 0; j < numberOfDoc; j++) {
                    lengthOfIdVector[j] = Math.sqrt(lengthOfIdVector[j]);
                    if (resultSimilarity[2 * j + 1] != 0) {
                        resultSimilarity[2 * j + 1] /= lengthOfIdVector[j] * lengthOfQueryVector;
                        arrToSort[j] /= lengthOfIdVector[j] * lengthOfQueryVector;
                    }
                }
            }
        }

        //Test lines
        /*for(int i = 0 ; i < keywordOfQuery.length ; i++){
            if(hashMap.containsKey(keywordOfQuery[i])){
                String value = (String)hashMap.get(keywordOfQuery[i]);
                System.out.println(keywordOfQuery[i] +" -> "+value);
            }else
                System.out.println(keywordOfQuery[i] + "-> " );//뽑아낸 키워드가 문서에서 존재하지 않을 경우 value를 출력하지않는다.
        }
        System.out.println("");

        for(int i = 0 ; i < numberOfDoc ; i++)
            System.out.println("id "+(int)resultSimilarity[i*2]+ " : " + String.format("%.2f", resultSimilarity[i*2+1]));
        System.out.println();*/
        ///////////////////////
        mergeSort(arrToSort, resultSimilarity, 0, arrToSort.length - 1);//정렬용 결과를 정렬하며, 최종 결과인 resultSimilarity 또한 동시에 내림차순으로 정렬을 한다. 이때 similarity id도 similarity와 함께 동시에 정렬된다.

        String[] resultString = new String[numberOfDoc * 2];

        for (int i = 0; i < numberOfDoc * 2; i++) {
            if (i % 2 == 0) {
                int id = (int) resultSimilarity[i];
                nodes = (NodeList) xpath.evaluate("//docs/doc[@id = '" + id + "']/title", doc, XPathConstants.NODESET);
                resultString[i] = nodes.item(0).getTextContent();
            } else
                resultString[i] = String.format("%.2f", resultSimilarity[i]);
        }
        return resultString;
    }
}