# Chapter 6. 빈 라이프사이클과 범위

### 1. 컨테이너 초기화와 종료

스프링 컨테이너는 초기화와 종료의 라이프 사이클을 갖는다.

```java
// 1. 컨테이너 초기화
AnnotationConfigApplicationContext ctx = 
	new AnnotationConfigApplicationContext(AppContext.class);

// 2. 컨테이너에서 빈 객체 사용
Greeter g = ctx.getBean("greeter", Creeter.class);

// 3. 컨테이너 종료
ctx.close(); 
```

1. **컨테이너 초기화**
    - AnnotationConfigApplicationContext의 생성자를 이용하여 컨테이너 객체를 생성하며, 스프링 컨테이너를 초기화한다.
    - 이후, 설정 클래스에서 정보를 읽어와 알맞는 빈 객체를 생성하고 각 빈을 연결(의존 주입)한다.
2. **컨테이너에서 빈 객체 사용**
    - getBean()과 같은 메서드를 이용하여 컨테이너에 보관된 빈 객체를 구하고 사용한다.
3. **컨테이너 종료**
    - close() 메서드를 통해 컨테이너를 종료할 수 있다.

<br/>

> 스프링 컨테이너는 빈 객체의 생성부터 소멸까지의 라이프 싸이클을 개발자 대신 관리한다.
- 컨테이너 초기화 : 빈 객체 생성, 의존 주입, 초기화
- 컨테이너 종료 : 빈 객체의 소멸
> 

<hr/>

### 2. 빈 객체의 라이프 사이클

스프링 컨테이너는 자바 객체의 생명 주기를 관리한다.

#### 빈 객체의 라이프 사이클
    스프링 컨테이너 생성 → 스프링 빈 생성 → 의존 관계 주입 → 초기화 콜백 → 사용 → 소멸전 콜백 → 스프링 종료
    
*초기화 콜백 : 빈이 생성되고, 빈의 의존관계 주입이 완료된 후 호출    

*소멸전 콜백 : 빈이 소멸되기 직전 호출

*콜백 : 특정 이벤트가 발생했을 때 해당 메서드가 호출 → 조건에 따라 실행될 수도 실행되지 않을 수도 있는 개념
    
<br/>

#### 빈 객체의 초기화와 소멸
1. **InitializingBean와 DisposableBean 상속**

```java
public interface InitializingBean {
  // 스프링 컨테이너 초기화 과정에서 실행
  void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
  // 빈 객체의 소멸 과정에서 실행
  void destory() throws Exception;
}
```

- InitializingBean과 DisposableBean은 각각 afterPropertiesSet()와 destroy()를 오버라이드 하도록 지원한다.

- 자바 표준 인터페이스가 아닌 스프링 전용 인터페이스로써 스프링에 종속적이다.
- 초기화, 소멸 메서드의 이름을 변경할 수 없다.
- 외부 라이브러리에 적용할 수 없다.

→ 인터페이스를 사용하는 방법은 스프링 초창기에 나온 방법으로 잘 사용하지 않는다.

<br/>

2. **@PreDestroy와 @PostConstruct**

스프링에서는 @PreDestroy와 @PostConstruct 사용을 권장한다.

```java
public class Clinet {
	
  @PreDestroy
  public void init() {
      ...
  }

  @PostConstruct
  public void destory() {
      ...
  }

  ...
}
```

- @PreDestroy와 @PostConstruct는 Spring의 어노테이션이 아닌 자바 표준 javax의 어노테이션이다. 따라서, Spring이 아닌 다른 컨테이너에서도 동작 가능하다.
- 어노테이션을 사용하기 때문에 편리하게 이용 가능하다.
- 하지만 외부 라이브러리를 Bean으로 등록하는 경우라면 코드 수정이 불가능하여 다른 방법을 사용해야 한다.

<br/>

3. **initMethod와 destroyMethod 사용**

@Bean 어노테이션에 initMethod와 destroyMethod를 설정하여 초기화, 소멸 메소드를 지정할 수 있다.

```java
public class Client {
  public void init() {
    ...
  }
  public void close() {
    ...
  }
}

@Configuration
public class ClientCofig {

  @Bean(initMehthod = "init", destroyMethod = "close")
  public Client client() {
    ...
  }
}
```

- 메서드 이름을 자유롭게 줄수 있다.
- 코드에 접근할 수 없는 외부 라이브러리에도 초기화, 종료 메소드를 적용할 수 있게 되었다.

> 웬만해서 @PreDestroy와 @PostConstruct 사용을 권장하고, 외부 라이브러리를 빈으로 등록하는 경우 initMethod와 destroyMethod를 사용한다.
> 

<hr/>

### 3. 빈 객체의 생성과 관리 범위

1. 싱글톤
- 스프링 컨테이너는 기본적으로 **빈 객체를 싱글톤으로 관리**한다.
    - 별도 설정을 하지 않으면 빈은 싱글톤 범위를 가진다.

<br/>

2. 프로토타입

```java
@Bean
@Scope("prototype")
public Client client() {
  ...
}
```

- 빈의 범위를 프로토타입으로 지정하면 **빈 객체를 구할 때마다 매번 새로운 객체를 생성**한다.
- **프로토타입 빈의 생성과 의존관계 주입까지만 스프링 컨테이너가 관여하고 그 이후로는 관여하지 않는다.**
    - 종료 메서드가 호출되지 않는다.
    - 따라서 프로토타입의 빈 객체를 사용할 때는 **빈 객체의 소멸 처리 코드를 직접 작성**해야한다.
- 싱글톤은 스프링이 뜰 때 생성되는 반면, 프로토타입은 요청할 때 생성된다.

<br/>

3. 웹 스코프
- request : 각 요청이 들어오고 나갈때까지 유지한다.
- session : 세션이 생성되고 종료될때까지 유지한다.
- application : 웹의 서블릿 컨텍스트와 동일한 생명주기를 갖는 스코프
