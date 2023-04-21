# Chapter 10. 스프링 MVC 프레임워크 동작 방식
### 핸들러란?

- 이벤트가 발생했을 때, 해당 이벤트에 맞는 동작을 수행하는 객체라고 생각하면 된다. 
핸들러가 컨트롤러의 상위 개념


## 1. 스프링 MVC 핵심 구성 요소

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/6654bcbb-0a1c-4820-b5fa-fafd4dddc529/Untitled.png)

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
- **DispatcherServlet**이 모든 연결을 담당한다. - 클라이언트의 요청을 전달하는 창구 역할
    - 요청이 들어왔을 때, 이를 처리할 컨트롤러 객체를 직접 찾지 않고, **HandlerMapping**이라는 빈 객체에게 검색을 요청한다.
    - Handler를 찾은 이후, 바로 실행하지 않고 이를 처리할 **HandlerAdaptor** 빈 객체에게 요청 처리를 위임한다.
    - **HandlerAdaptor**는 컨트롤러에서 요청에 맞는 메서드를 호출해서 요청을 처리하고 그 결과를 **DispatcherServlet**에게 전달한다.
    - 요청 처리 결과가 ModelAndView 타입이라면, **DispatcherServlet**은 결과를 보여줄 뷰를 찾기 위해서 **ViewResolver** 빈 객체를 호출한다.

> Q. HandlerAdapter의 존재 이유 (+ “ControllerMapping”이 아닌 “HandlerMapping”인 이유)

Ans:  @Controller 뿐만 아니라 Controller 인터페이스, HttpRequestHandler 인터페이스로 만들어진 handler들도 함께 처리할 수 있도록 설계되었기 때문이다.
>
