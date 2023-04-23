# Chapter 10. 스프링 MVC 프레임워크 동작 방식
### 핸들러란?

- 이벤트가 발생했을 때, 해당 이벤트에 맞는 동작을 수행하는 객체라고 생각하면 된다. 
핸들러가 컨트롤러의 상위 개념


## 1. 스프링 MVC 핵심 구성 요소

![image](https://user-images.githubusercontent.com/83508073/233844723-76e19126-7bff-43a5-94b1-52802387eff1.png)


1. 요청 전송
2. 요청 URL과 매칭되는 핸들러(컨트롤러)를 **HandlerMapping**에서 검색
3. 해당 핸들러를 처리할 수 있는 **HandlerAdapter** 조회
4. 핸들러 어댑터를 통해 **핸들러(컨트롤러)**를 호출
5. 결과를 받아서 ModelAndView로 변환해서 리턴
6. 컨트롤러의 실행 결과를 보여줄 View를 **viewResolver**를 호출하여 검색
7. 해당 View을 반환하고, render(model) 호출
8. Html 응답
- 빨간색 글자로 표시한 것은 모두 스프링 빈으로 등록해야 한다.
- 컨트롤러는 개발자가 직접 구현해야 한다.
- **DispatcherServlet**이 모든 연결을 담당한다. - 클라이언트의 **요청 전달**, **흐름 제어**
    - 요청이 들어왔을 때, 이를 처리할 컨트롤러 객체를 직접 찾지 않고, **HandlerMapping**이라는 빈 객체에게 검색을 요청한다.
    - Handler를 찾은 이후, 바로 실행하지 않고 이를 처리할 **HandlerAdaptor** 빈 객체에게 요청 처리를 위임한다.
    - **HandlerAdaptor**는 요청에 맞는 핸들러 객체의 메서드를 호출해서 요청을 처리하고 그 결과를 **DispatcherServlet**에게 전달한다.
    - 요청 처리 결과가 ModelAndView 타입이라면, **DispatcherServlet**은 결과를 보여줄 뷰를 찾기 위해서 **ViewResolver** 빈 객체를 호출한다.

> Q. HandlerAdapter의 존재 이유 (+ “ControllerMapping”이 아닌 “HandlerMapping”인 이유)
>
> Ans:  @Controller 뿐만 아니라 Controller 인터페이스, HttpRequestHandler 인터페이스로 만들어진 handler들도 함께 처리할 수 있도록 설계되었기 때문이다.
> 

> Q. 컨트롤러의 결과 값이 View가 아니라 http 응답 패킷 body에 Json을 담는 rest API의 경우는?
>
> Ans: @RestController 어노테이션을 사용하는 경우는 요청을 처리하는 흐름이 위 사진과 다르다.
> @RestController는 @Controller와 @ResponseBody를 합친 것이다.
> 
> 
> **@Controller의 실행 흐름**
> Client -> Request -> Dispatcher Servlet -> Handler Mapping ->Controller -> View -> Dispatcher Servlet -> Response -> Client
> 
> **@RestController의 실행 흐름**
> Client -> HTTP Request -> Dispatcher Servlet -> Handler Mapping ->RestController (자동 ResponseBody 추가)-> HTTP Response -> Client
> 
> **@ResponseBody**
> 는 메소드가 반환하는 객체를 Http response body로 전송하도록 지정한다. 이때, Spring은 해당 객체를 Jackson 라이브러리를 사용하여 JSON 형태로 변환하여 Http response 패킷의 body에 넣는다.
> 

## 2. DispatcherServlet과 스프링 컨테이너

<aside>
💡 스프링 컨테이너는 DispatcherServlet을 init하는 시점에 생성된다!

</aside>

### DispatcherServlet 생성 시점

- DispatcherServlet는 하나의 서블릿이며, Tomcat이 실행되어 서블릿 컨테이너가 **서블릿 컨텍스트를 초기화하는 시점**에 옵션에 따라서 **lazy loading** 혹은 **pre-loading** 방식으로 생성
- **ServletContext**
    
    ![image](https://user-images.githubusercontent.com/83508073/233848873-c205f77e-013e-4da8-9fce-7d9fcb4ee567.png)
    
    - Tomcat이 실행되면서 서블릿과 서블릿 컨테이너 간 연동을 위해 사용
    - 하나의 웹 애플리케이션마다 하나의 서블릿 컨텍스트를 가진다
    - 참고: [https://velog.io/@suhongkim98/CGI와-서블릿-JSP의-연관관계-알아보기](https://velog.io/@suhongkim98/CGI%EC%99%80-%EC%84%9C%EB%B8%94%EB%A6%BF-JSP%EC%9D%98-%EC%97%B0%EA%B4%80%EA%B4%80%EA%B3%84-%EC%95%8C%EC%95%84%EB%B3%B4%EA%B8%B0)
- lazy loading 방식
    - Tomcat 구동 시 서블릿 인스턴스를 생성하지 않고, 클라이언트로부터 최초로 요청을 받을 때 서블릿 컨테이너는 DispatcherServlet 인스턴스를 생성
- Pre-loading 방식
    - 서블릿 컨텍스트를 초기화하는 시점에 미리 DispatcherServlet 인스턴스를 생성
- 기존에는 web.xml을 기반으로 서블릿 컨텍스트 초기화를 진행했지만, 현재는 ServletContainerInitializer API을 통해 가능

### 스프링 컨테이너(Application Context) 생성 시점

- DispatcherServlet을 init하는 시점에 생성
- 다양한 방법의 DispatcherServlet + 스프링 컨테이너 생성 방법이 존재
    - 스프링 부트는 `WebApplicationInitializer` 구현+ `AnnotationConfigWebApplicationContext` 방식을 사용!

```java
public class WebInitializer implements WebApplicationInitializer{
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {        
        ...
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation("com.studyspring.basic.config");
        
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DispatcherServlet", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        ...
        ...
    }    
}
```

- 톰캣의 서블릿 컨테이너가 구동되면, 웹 애플리케이션의 서블릿 컨텍스트를 생성하고 초기화 하기위해 `WebApplicationInitializer`  인터페이스를 구현한 클래스를 찾아 `onStartUp`  메서드를 호출한다.
- `AnnotationConfigWebApplicationContext` 을 통해 스프링 컨테이너를 생성하고, 여기에 설정 파일 위치를 지정한다.
- DispatcherServlet을 생성하고 서블릿 컨텍스트에 추가한다.

⇒ **즉, DispatcherServlet을 초기화하는 시점에 스프링 컨테이너가 생성된다!**

## 3. @Controller를 위한 HandlerMapping과 HandlerAdapter

<aside>
💡 DispatcherServlet은 요청을 처리할 핸들러 객체를 찾기 위해 HandlerMapping을 사용하고, 핸들러를 실행하기 위해서 HandlerAdapter를 사용한다. 해당 타입의 빈을 스프링 컨테이너에서 찾아서 사용하기 때문에 등록이 되어야 한다.

</aside>

⇒ @EnableWebMvc 어노테이션을 통해 등록된다!

이때, RequestMappingHandlerMapping과 RequestMappingHandlerAdapter도 포함된다.

```java
@Configuration
@EnableWebMvc
public class WebMvcConfig {
}
```

- @Enable로 시작하는 애노테이션을 @Configuration이 붙은 설정 클래스에 붙임으로써 설정을 자동화한다.

### RequestMappingHandlerMapping

- **@Controller 어노테이션이 적용**된 객체에서 **요청 매핑 어노테이션** (ex-@GetMapping)을 이용해서 클라이언트의 요청을 처리할 **컨트롤러 빈을 찾는다.**

### RequestMappingHandlerAdapter

- **컨트롤러의 메소드를 알맞게 실행**하고, 그 결과를 **ModelAndView** 객체로 변환해서 **DispatcherServlet에 리턴한다**.
- 컨트롤러 메소드의 결과 값이 String 타입이면, 해당 값을 View 이름으로 갖는 ModelAndView 객체를 생성하여 DispatcherServlet에 리턴한다.

## 4. WebMvcConfigurer 인터페이스와 설정

우리는 스프링이 제공해주는 자동 설정들 외에 추가의 설정이 필요할 수 있다. 그래서 스프링에서는 @Enable로 적용되는 인프라 빈에 대해 추가적인 설정을 할 수 있도록 **~Configurer로 끝나는 인터페이스(빈 설정자)를 제공**하고 있다.

<aside>
💡 대표적으로 @EnableWebMvc의 빈 설정자는 WebMvcConfigurer이며, 이를 구현한 클래스를 만들고 @Configuration을 붙여 빈으로 등록해주면 된다.

</aside>

```java
public interface WebMvcConfigurer {

	default void configurePathMatch(PathMatchConfigurer configurer) {
	}

	default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
	}

	default void configureAsyncSupport(AsyncSupportConfigurer configurer) {
	}

	default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
	}

	default void addFormatters(FormatterRegistry registry) {
	}

	default void addInterceptors(InterceptorRegistry registry) {
	}

	default void addResourceHandlers(ResourceHandlerRegistry registry) {
	}

	default void addCorsMappings(CorsRegistry registry) {
	}

	default void addViewControllers(ViewControllerRegistry registry) {
	}

	default void configureViewResolvers(ViewResolverRegistry registry) {
	}

	default void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
	}

	default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> handlers) {
	}

	default void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
	}

	default void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
	}

	default void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
	}

	default void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
	}

	@Nullable
	default Validator getValidator() {
		return null;
	}

	@Nullable
	default MessageCodesResolver getMessageCodesResolver() {
		return null;
	}

}
```

- WebMvcConfigurer을 상속한 설정 클래스에서 필요한 메소드만 오버라이드해서 구현하면 된다.
- 예를 들면, 스프링이 기본적으로 제공해주는 Json 기반의 메세지 컨버터 구성에 더해 XML 기반의 메세지 컨버터가 필요한 상황
    - 기존의 메시지 컨버터를 확장하기 때문에 extendMessageConverters를 오버라이딩한다.
    

## 5. ViewResolver

<aside>
💡 ViewResolver는 컨트롤러가 리턴한 View 이름에 해당하는 View를 찾는 역할을 한다.

</aside>

- 컨트롤러의 실행 결과를 받은 DispatcherServlet은 ViewResolver에게 뷰 이름에 해당하는 View 객체를 요청한다.
- 종류
    - ResourceBundleViewResolver
        - .properties 에서 뷰 이름에 해당하는 콤포넌트를 찾는다.
    - InternalResouceViewResolver
        - 미리 지정된 접두사, 접미사를 사용하여 뷰이름으로 콤포넌트를 찾는다.
        - ex) 뷰 이름: “hello” → “/WEB-INF/view/hello.jsp” 경로를 뷰 코드로 사용하는 InternalResourceView 객체를 리턴한다.
