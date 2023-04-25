# Chapter 13. MVC 3 : 세션, 인터셉터, 쿠키

## 상태 유지

상태를 유지하는 방법

1. 세션
2. 쿠키

### 컨트롤러에서 HttpSession 사용하기

1. HttpSession 파라미터 추가

   ```java
   @PostMapping
   public String form(LoginCommand loginCommand, Errors errors, HttpSession httpSession) { ... }
   ```

    - 항상 HttpSession 생성

2. HttpServletRequest 파라미터 추가 후 HttpServletRequest 이용해 HttpSession 구함

   ```java
   @PostMapping
   public String form(LoginCommand loginCommand, Errors errors, HttpServletRequest httpServletRequest) {
      HttpSession httpSession = httpServletRequest.getSession();
      ... 
   }
   ```

    - 필요 시에만 HttpSession 생성

세선 추가: `httpSession.setAttribute(key, value)`

세션 삭제: `httpSession.invalidate()`

세션 정보 가져오기: `httpSession.gtAttribute(key)`

- 주의 - 서버 재시작 시 세션 정보가 유지되지 않기 때문에 세션 추가없이 세션 정보 조회 시 null을 반환

### 컨트롤러에서 쿠키 사용하기

1. `@CookieValue` 애노테이션 사용

```java
@GetMapping
public String form(LoginCommand loginCommand, @CookieValue(value = "REMEMBER", required = false)Cookie cookie) { ... }
```

- 쿠키를 파라미터로 전달받을 수 있음

@CookieValue 속성

- value: 쿠키 이름 지정

- required: 존재 의무 여부 지정(기본값: true)

2. HttpServletResponse 파라미터 추가 후 쿠키 생성

```java
@PostMapping
public String submit(LoginCommand loginCommand, Errors errors, HttpSession httpSession, HttpServletResponse httpServletResponse) { ... }
```

쿠키 생성

```java
@PostMapping
public String submit(LoginCommand loginCommand, Errors errors, HttpSession httpSession, HttpServletResponse httpServletResponse) {
    ...
    Cookie cookie = new Cookie("REMEMBER", loginCommand.getEmail());
    cookie.setPath("/");
    cookie.setMaxAge(60 * 60 * 24 * 30);    // 만료기간 설정 - 30일
    httpServletResponse.addCookie(cookie);
}
```

쿠키 삭제: `cookie.setMaxAge(0);`

## 인터셉터

다수의 컨트롤러에 대해 동일한 기능 적용 시 HandlerInterceptor 사용 가능

## HandlerInterceptor 인터페이스 구현하기

`org.springframework.web.HandlerInterceptor` 인터페이스 사용 시 세 시점에 공통 기능 넣기 가능

1. 컨트롤러(핸들러) 실행 전
   
   `boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception;`

   - `preHandle()` 메소드가 false 리턴 시 컨트롤러(또는 다음 HandlerInterceptor) 실행하지 않음

2. 컨트롤러(핸들러) 실행 후, 뷰 실행 전

   `void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, ModelAndView modelAndView) throws Exception;`

   - 컨트롤러에서 익셉션 발생 시 `postHandle()` 메소드는 실행되지 않음

3. 뷰 실행 후

   `void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception exception) throws Exception;`

   - 뷰가 클라이언트에 응답 전송 후 실행
   - 컨트롤러 실행 과정에서 익셉션 발생 시 네 번째 파라미터(Exception exception)로 전달, 발생하지 않으면 null 전달
   - 컨트롤러 실행 후 예기치 않게 발생한 익셉션 로그 남기거나 실행 시간 기록 등 **후처리**에 적합
   
## HandlerInterceptor 설정하기

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    
    ...
    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(authCheckInterceptor())
                .addPathPatterns("/edit/**")    // 적용할 경로
                .excludePathPatterns("/edit/help/**", "/admin/**"); // 제외할 경로
    }
}
```

### Ant 경로 패턴

인터셉터를 적용 또는 제외할 경로는 Ant 경로 패턴을 사용한다.

Ant 패턴은 *, **, ? 세 가지 특수 문자를 이용해 경로를 표현한다.

- *: 0개 이상의 글자
- ?: 1개 이상의 글자
- **: 0개 이상의 폴더 경로