## Chapter 11. MVC 1 : 요청 매칭, 커맨드 객체, 라다이렉트, 폼 태그, 모델

### 1. 요청 매핑 어노테이션을 이용한 경로 매핑

- @RequestMapping
    - @GetMapping
    - @PostMapping
    - @PutMapping
    - @DeleteMapping
    - @PatchMapping
    
<hr/>

### 2. 요청 파라미터 접근

```java
// HttpServletRequest의 getParameter() 사용
@PostMapping("/register")
public String register(HttpServletRequest request) {
	String paramName = request.getParameter("name");
	...
}

// @RequestParam 사용
@PostMapping("/register")
public String register(@RequestParam String name) {
	String paramName = name;
	...
}
```

- @RequestParam 속성

| 속성 | 타입 | 설명 | 기본값 |
| --- | --- | --- | --- |
| value | String | HTTP 요청 파라미터의 이름을 지정한다. | 매개변수 이름 |
| required | boolean | 필수 여부 | true |
| defaultValue | String | 요청 파라미터가 값이 없을 때 사용할 문자열 값 지정 | 없다. |

→ 스프링 MVC는 파라미터 타입에 맞게 String 값을 변환해준다.

<hr/>

### 3. 리다이렉트 처리

- 컨트롤러에서 특정 페이지로 리다이렉트 하는 방법
    - “redirect:경로”
    
    ```java
    @PostMapping("/register")
    public String register() {
    	...
    	return "redirect:/login"
    }
    ```
    
<hr/>

### 4.  커맨드 객체를 이용해서 요청 파라미터 사용하기

- 커맨드 객체 : 클랑언트가 전달해주는 파라미터 데이터를 주입 받기 위해 사용되는객체
- 스프링에서는 요청 파라미터 값을 커맨드 객체에 담아주는 기능을 제공한다.

```java
@Data
class RegisterRequest {
	private String email;
	private String password;
}

@PostMapping("/register")
public String register(RegisterRequest request) {
	...
	return "redirect:/login"
}
```

<hr/>

### 5. @ModelAttribute

[https://tecoble.techcourse.co.kr/post/2021-05-11-requestbody-modelattribute/](https://tecoble.techcourse.co.kr/post/2021-05-11-requestbody-modelattribute/)

- @ModelAttribute
    
    ```
    POST HTTP1.1 /modelattribute
    Request params: id=13 name=kevin
    ```
    
    - 클라이언트가 전송하는 폼 형태의 HTTP Body와 요청 파라미터들을 생성자나 Setter로 바인딩하기 위해 사용된다.
- @RequestParam
    - 1개의 HTTP 파라미터를 얻기 위해 사용되며 기본값을 지정할 수 있다.
- @RequestBody
    - Json(application/json) 형태의 HTTP Body 데이터를 MessageConverter를 통해 Java 객체로 변환시킨다.
        
        ```
        POST HTTP1.1 /requestbody
        Body:
        { “password”: “1234”, “email”: “kevin@naver.com” }
        ```
        
    - @RequestBody를 사용할 객체는 필드를 바인딩할 생성자나 setter 메서드가 필요없다.
        - 다만 직렬화를 위해 기본 생성자는 필수다.
        - 데이터 바인딩을 위한 필드명을 알아내기 위한 getter는 정의 되어 있어야 한다.

<hr/>

### 6. 컨트롤러 구현 없는 경로 매핑

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	...
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/main").setviewName("main");
	}
}
```

<hr/>

### 7. Model을 통해 컨트롤러에서 뷰 데이터 전달하기

```java
@Controller
public class HelloController {
	
	@GetMapping("/hello")
	public String hello(Model model) {
		...
		model.addAttribute("data", "hi!!");
		return "hello"
	}
}
```

<hr/>

### 8. ModelAndView를 통한 뷰 선택과 모델 전달

```java
@Controller
public class HelloController {
	
	@GetMapping("/hello")
	public ModelAndView hello() {
		
		ModelAndView mav = new ModelAndView();
		mav.addObject("data" , "hi!!");
		mav.setViewName("hello");
		return mav;
	}
}
```
