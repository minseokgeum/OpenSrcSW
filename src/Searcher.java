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
}