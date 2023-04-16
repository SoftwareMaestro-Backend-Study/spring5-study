# Chapter 5. 컴포넌트 스캔

## 0. 들어가며

### 컨포넌트 스캔이란

컴포넌트 스캔은 스프링이 직접 클래스를 검색해서 빈으로 등록해주는 기능이다. 
“@Component” 어노테이션을 붙여주면 되는데, 우리가 흔히 사용하는 @Controller, @Service, @Configuration내에 @Component 어노테이션이 포함되어 있기 때문에 스프링이 해당 클래스를 빈으로 등록할 수 있던 것이다.

<aside>
💡 Without 컴포넌트 스캔 

	자바 코드의 @Bean 이나 XML에 직접 등록할 스프링 빈을 나열해야 한다. (**ch04 AppCtx** 참고)
	이를 개발자가 직접 관리하는 것은 어렵기 때문에, 설정 정보가 없어도 스프링에서 자동으로 빈을 등록할 수 있도록 컴포넌트 스캔 기능을 제공한다.

</aside>

---

## 1. @Component 어노테이션으로 스캔 대상 지정

스프링이 자동으로 빈을 등록하기 위해서는 등록하려는 클래스에 @Component 어노테이션을 붙여야 한다. 

```java
@Component
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    
    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
  }
}
```

### 특징

**빈 이름 전략**

- 기본 : 수동으로 이름을 지정하지 않을 시, 가장 앞 문자를 소문자로 바꾼 것이 빈 이름이 된다.
- 수동 지정 : **`@Component("지정할 이름")`**
    - ex) @Component(”listPrinter”)

**기본적인 컴포넌트 스캔 대상**

- **`@Component` : 컴포넌트 스캔**에서 사용
- **`@Controller` : 스프링 MVC 컨트롤러**에서 사용
- **`@Service` : 스프링 비즈니스 로직**에서 사용
- **`@Repository` : 스프링 데이터 접근 계층**에서 사용
- **`@Configuration` : 스프링 설정 정보**에서 사용

**컴포넌트 스캔 범위**

- **`@ComponentScan` 어노테이션이 있는 파일의 패키지 아래**를 찾는다.
- **`basePackages` / `basePackageClasses`로 지정도 가능**
- **권장 방법** : 구성파일에 등록시 **프로젝트 최상단**에 두기 → **`@SpringBootApplication`에 포함되어있어서 자동으로 최상단으로 유지**된다


---


## 2. @ComponentScan 어노테이션으로 스캔 설정

### @ComponentScan

![image](https://user-images.githubusercontent.com/83508073/232289890-bdcf67a9-fcb0-4f7c-960f-bbfc26b15a7c.png)

- `@ComponentScan`은 `@Component`가 붙은 모든 클래스를 스프링 빈으로 등록한다.
- 빈 이름: (1) 클래스 명에서 맨 앞글자만 소문자로 변경 (2) 수동 지정

<aside>
💡 @ComponentScan을 해주지 않아도 등록되었던 이유

	→ main 메소드에 @SpringbootApplication 어노테이션을 붙였기 때문이다.
	해당 어노테이션에 @ComponentScan이 포함되어있음을 확인할 수 있다.

</aside>

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { 
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) 
})
public @interface SpringBootApplication
```


---


## 3. 스캔 대상에서 제외하거나 포함하기

 

**탐색할 패키지의 시작 위치 지정: basePackages, basePackagesClasses**

모든 자바 클래스를 다 컴포넌트 스캔하면 비효율적이다. 따라서 꼭 필요한 위치부터 탐색할 수 있도록 시작 위치를 지정할 수 있다.

```java
@ComponentScan(
    basePackages = "hello.core"
)
```

- 만약 basePackagesClasses를 따로 지정하지 않으면, @ComponentScan이 붙은 설정 정보 클래스의 패키지가 탐색 시작 위치가 된다.
- 위에서도 언급했지만,  **패키지 위치를 지정하지 않고, 설정 정보 클래스를 프로젝트 최상단에 두는 것이다.**
    - 프로젝트 최상단에 설정정보 위치시키면 **해당 패키지를 포함한 하위 패키지는 모두 자동으로 컴포넌트 스캔의 대상**이 된다.

**Filter 속성 : 컴포넌트 스캔 대상 추가/제외**

해당 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 추가/ 제외할 수 있다.

- **includeFilters : 컴포넌트 스캔 대상으로 추가**
- **excludeFilters : 컴포넌트 스캔 대상에서 제외**

- **FilterType 옵션**
    - **`ANNOTATION` : 기본값, 어노테이션을 인식해 동작**
    - `ASSIGNABLE_TYPE` : 지정한 타입과 자식 타입을 인식해 동작
    - `ASPECTJ` : AspectJ 패턴 사용
    - `REGEX` : 정규 표현식
    - `CUSTOM` : TypeFilter이라는 인터페이스를 구현해서 처리
    

<aside>
	💡 실제로 @ComponentScan에 직접 filter을 지정할 일은 거의 없다.

</aside>
