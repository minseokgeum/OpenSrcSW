# 2022 오픈소스 SW SimpleIR

2022-1학기 건국대학교 오픈소스 SW 강의에서 실습하는 프로젝트입니다.

**KuIR.java**가 프로젝트의 메인 소스 코드로 사용되고,

현재 5주차 **MakeCollection.java**, **MakeKeyword.java**, **Indexer.java**, **Searcher.java** 파일을 메인 함수의 인자값(String[] args)에 따라 객체를 생성하고 함수를 실행합니다.

## 파일 구조

```bash
├── README.md
├── collection.xml
├── index.xml
├── index.post
├── out
│   └── production
│       └── SimpleIR
├── jars
├── data
│   ├── 떡.html
│   ├── 라면.html
│   ├── 아이스크림.html
│   ├── 초밥.html
│   └── 파스타.html
└── src
    ├── KuIR.java
    ├── MakeCollection.java
    ├── MakeKeyword.java
    ├── Indexer.java
    └── Searcher.java
```

## 인코딩

**Encoding : UTF-8**

## 디렉토리 설명

**src** : .java 소스 파일이 저장되는 디렉토리

**out/production/SimpleIR** : 컴파일된 .class 바이너리 파일이 저장되는 디렉토리

**data** : 프로그램 실행 시 data로 사용될 .html 파일들이 저장되어 있는 디렉토리

**jars/** : 외부 jar 파일이 저장되는 디렉토리

## 컴파일 명령어

### MAC

`javac -cp (외부 jar 파일 이름 1):(외부 jar 파일 이름 2):,,,, src/*.java -d out/production/SimpleIR (-encoding UTF8)`

ex) `javac -cp jars/jsoup-1.14.3.jar:jars/kkma-2.1.jar src/*.java -d out/production/SimpleIR -encoding UTF8`

## 실행 명령어

### MAC

`java -cp (외부 jar 파일 이름 1):(외부 jar 파일 이름 2):,,,,:out/production/SimpleIR KuIR (args 1) (args 2) ,,, (args n)`

-c command) `java -cp ./jars/jsoup-1.14.3.jar:./jars/kkma-2.1.jar:out/production/SimpleIR KuIR -c ./collection.xml data`

-k command) `java -cp ./jars/jsoup-1.14.3.jar:./jars/kkma-2.1.jar:out/production/SimpleIR KuIR -k ./index.xml`

-i command) `java -cp ./jars/jsoup-1.14.3.jar:./jars/kkma-2.1.jar:out/production/SimpleIR KuIR -i ./index.post`

-s command) `java -cp ./jars/jsoup-1.14.3.jar:./jars/kkma-2.1.jar:out/production/SimpleIR KuIR -s ./index.post -q "Query"`
