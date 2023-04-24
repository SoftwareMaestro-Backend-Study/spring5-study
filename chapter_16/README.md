## Chapter 16. Json 응답과 요청 처리

### 1. Json 개요

- Json : 간단한 형식을 갖는 문자열로 데이터 교환에 주로 사용한다.
    - 중괄호를 사용하여 객체 표현
    - 객체는 (이름, 값) 쌍을 갖는다. 이름과 값은 콜론(:)으로 구분한다.
    - 특수문자는 역슬래시를 사용하여 포현한다.
    
    ```json
    {
    	"name" : "Tom",
    	"birthday" : "1998-08-03"
    }
    ```
    
- Jackson : 자바 객체와 JSON 형식 문자열 간 변환을 처리하는 라이브러리

<hr/>

### 2. @RestController

- @RestController는 @Controller에 @ResponseBody가 추가된 것
- 주 용도는 Json 형태로 객체 데이터를 반환하는 것
- @Controller는 View를 반환하기 위해 ViewResolver를 사용하는 반면에
    
    @RestController 경우 HttpMessageConverter를 사용하여 데이터를 반환한다.
    
    - 단순 문자열인 경우 StringHttpMessageConverter가 사용되고, 객체인 경우 MappingJackson2HttpMessageConverter가 사용된다.

<hr/>

### 3. @JsonIgnore, @JsonFormat

- @JsonIgnore : JSON 응답에 포함시키지 않을 대상에 붙인다.
- @JsonFormat : JSON 출력에 대한 필드 및/또는 속성의 형식을 지정하는 방법을 지정하는 데 사용
    - SimpleDateFormat 형식 에 따라 *날짜* 및 *달력* 값의 형식을 지정하는 방법을 지정
    
    참고 : [https://jojoldu.tistory.com/361](https://jojoldu.tistory.com/361)
    
<hr/>

### 4. @RequestBody로 JSON 요청 처리

- Json(application/json) 형태의 HTTP Body 데이터를 MessageConverter를 통해 Java 객체로 변환시킨다.
    
    ```
    POST HTTP1.1 /requestbody
    Body:
    { “password”: “1234”, “email”: “kevin@naver.com” }
    ```
    
- @RequestBody를 사용할 객체는 필드를 바인딩할 생성자나 setter 메서드가 필요없다.
    - 다만 직렬화를 위해 기본 생성자는 필수다.
    - 데이터 바인딩을 위한 필드명을 알아내기 위한 getter는 정의 되어 있어야 한다.
- @Valid와 @Validated를 통해서 검증 가능

<hr/>

### 5. ResponseEntity로 객체 리턴하고 응답 코드 지정하기

- ResponseEntity를 사용하여 JSON 응답 전송하기
    - 스프링 MVC는 리턴 타입이 ResponseEntity이면 ResponseEntity의 body로 지정한 객체를 사용해서 변환을 처리한다.
    - status로 지정한 값은 응답 상태 코드로 사용
    
    ```java
    ResponseEntity.status(상태코드).body(객체)
    ```
    
    - 200 응답코드와 body 데이터를 생성할 경우
    
    ```java
    ResponseEntity.ok(객체)
    ```
    
    - body가 없다면
    
    ```java
    ResponseEntity.status(상태코드).build()
    ResponseEntity.notFound().build()
    ```
    
- @ExceptionHandler
    - 글로벌하게 에러 응답을 처리하여 중복을 없앨 수 있다.
    
    ```java
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleError() {
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).bulid();
    }
    ```
    
- @RestControllerAdvice
    - 에러 처리 코드를 별도 클래스로 분리하여 관리할 수 있다.
    
    ```java
    @RestControllerAdvice
    public class ApiExceptionAdvice {
    
    	@ExceptionHandler(UserNotFoundException.class)
    	public ResponseEntity<ErrorResponse> handleError() {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).bulid();
    	}
    }
    ```
