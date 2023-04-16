# Chapter 04. 의존 자동 
작성자 : 조윤호

## 4.2 @Autowired 애노테이션을 이용한 자동 주입

Before
```java
public class ChangePasswordService {
    private MemberDao memberDao;
    
    public ChangePasswordService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}

@Configuration
public class AppContext {
    ...

    @Bean
    public ChangePasswordService changePasswordService() {
        return new ChangePasswordService(memberDao());
    }
    
    ...
}
```

After
```java
public class ChangePasswordService {
    @Autowired
    private MemberDao memberDao;
    
    public ChangePasswordService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}

@Configuration
public class AppContext {
    ...

    @Bean
    public ChangePasswordService changePasswordService() {
        // 의존을 주입하지 않아도 자동 할당된다
        return new ChangePasswordService(); 
    }
    
    ...
}
```

@Autowired
- 스프링이 해당 타입의 bean 객체를 찾아 필드에 할당

@Autowired 를 사용할 수 있는 곳
1. 필드

```java
@Autowired
private MemberDao memberDao;
```
2. 메서드

```java
@Autowired
public void setMemberDao (MemberDao memberDao) {
    this.memberDao = memberDao;
}
```

3. 생성자(?????)
- 생성자에서도 @Autowired를 붙일 수 있는 것으로 알고 있고, 생성자 주입을 보다 권장하는 걸로 알고 있었는데
- 책에서는 생성자 주입에 관한 내용이 나와있지 않다...?!?
- [생성자 주입을 @Autowired를 사용하는 필드 주입보다 권장하는 하는 이유](https://madplay.github.io/post/why-constructor-injection-is-better-than-field-injection)

### 4.2.1 예외상황
1. @Autowired 애노테이션을 적용한 대상에 일치하는 빈이 없는 경우
> Exception in thread "main" org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'memberRegisterService': Unsatisfied dependency expressed through field 'memberDao': No qualifying bean of type 'chap04.spring.dao.MemberDao' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}

2. @Autowired 에 일치하는 빈이 두 개 이상인 경우
> Exception in thread "main" org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'memberInfoPrinter': Unsatisfied dependency expressed through method 'setPrinter' parameter 0: No qualifying bean of type 'chap04.spring.printer.MemberPrinter' available: expected single matching bean but found 2: **memberPrinter,memberPrinter2**
- memberPrinter,memberPrinter2 두 개의 빈이 존재한다는 예외가 발생한다.

## 4.3 @Qualifier 애노테이션을 이용한 의존 객체 선택

@Bean, @Autowired와 함께 @Qualifier("이름")을 붙여서 의존할 bean 객체를 지정해준다.

```java
@Bean
@Qualifier("printer")
public MemberPrinter memberPrinter(){
    return new MemberPrinter();
}
```
```java
@Autowired
@Qualifier("printer")
public void setMemberPrinter(MemberPrinter memberPrinter) {
    this.memberPrinter = memberPrinter;
}
```

## 4.4 상위/하위 타입 관계와 자동 주입

다음과 같이 부모/자식 관계의 클래스가 있고, 부모 클래스를 불러왔을 때 예외가 발생한다.
```java
public class AppContext {
    @Bean
    public MemberPrinter memberPrinter1() {return new MemberPrinter();}

    @Bean
    // MemberSummeryPrinter extends MemberPrinter
    public MemberSummeryPrinter memberPrinter2() {return new MemberSummeryPrinter();}
    ...
}


public class MemberListPrinter {
    ...

    @Autowired
    public void setMemberPrinter(MemberPrinter memberPrinter) {
        this.memberPrinter = memberPrinter;
    }
}
```
해결방안
1. @Qualifier 애노테이션을 이용하여 가져올 bean 객체를 한정한다.
2. 자동 주입 대상이 한 개로 정해지는 MemberSummeryPrinter를 주입받는다.

```java
public class MemberListPrinter {
    ...
    @Autowired
    public void setMemberPrinter(MemberSummeryPrinter memberPrinter) {
        this.memberPrinter = memberPrinter;
    }
}
```

## 4.5 @Autowired 애노테이션의 필수 여부

@Autowired가 필수가 아닌 경우
- `@Autowired(required = false)`
  - bean 객체가 없을 경우 -> setter 메소드 호출 x
  - 해당 필드에 값이 할당되지 않음
- `@Nullable`이용
  - bean 객체가 없을 경우 -> 파라미터가 null인 채로 setter 메소드 호출된다.
  - 해당 필드에 null 값이 할당됨
```java
@Autowired
public void setDateFormatter(@Nullable DateTimeFormatter dateFormatter) {
        ...
}
```
- Optional 사용
```java
@Autowired
public void setDateFormatter(Optional<DateTimeFormatter> dateFormatter) {
    if (dateFormatter.isPresent()) {
        ...
    } else {
        ...
    }
}
```

## 4.6 자동 주입과 수동 주입(명시적 의존 주입) 코드가 모두 적혀있는 경우

```java
@Configuration
public class AppContext {    
    ...
    @Bean
    public MemberInfoPrinter memberInfoPrinter() {
        MemberInfoPrinter memberInfoPrinter = new MemberInfoPrinter();
        memberInfoPrinter.setPrinter(memberPrinter2());
        return memberInfoPrinter
    }
    ...
}

public class MemberInfoPrinter {
    ...
    @Autowired
    @Qualifier("memberPrinter1")
    public void setPrinter(MemberPrinter printer) {this.printer = printer;}
}
```
- 자동 주입에서는 memberPrinter1을, 수동 주입에서는 memberPrinter2를 주입하도록 되어 있다.
> **무엇이 주입될까요???**
- 자동 주입의 설정을 따라 bean 객체가 주입된다.
- => 자동주입 기능을 사용하자
  - 디버깅의 편의성을 위해 왠만하면 자동주입 기능 이용하기
  - 자동주입이 어려운 일부 케이스에만 수동주입 사용
  - 자동 주입, 수동 주입 코드 둘다 작성하지 않기


# 정리!
1. 자동 주입 시, 동일한 클래스의 빈 객체가 **두개 이상** 등록되어 있을 경우, 에러가 발생한다. 이때 해결방법은?
2. 다음과 같이 부모와 자식 클래스가 모두 빈 객체로 등록된 경우, 에러가 발생한다. 이때 해결 방법 두가지?
```java
public class AppContext {
    @Bean
    public 부모클래스 빈객체_내놔_1() {...}
    @Bean
    public 자식클래스 빈객체_내놔_2() {...}
}
public class SomeClass {
    @Autowired
    private 부모클래스 gackChe;
    ...
}
```
3. @Autowired할 객체가 필수가 아닌 경우 코드 작성 방법 3가지? 각각의 특징 및 장단점
4. 자동 주입 코드와 수동 주입 코드 모두 작성되어 있을 경우, 무엇이 우선 적용될까?
