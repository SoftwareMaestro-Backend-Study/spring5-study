# Chapter 03. 스프링 DI

의존(Dependency)

= 한 클래스가 다른 클래스의 메소드를 실행할 때

= 변경에 의해 영향을 받는 관계

의존 객체를 구하는 방법

1. 클래스 내부에서 직접 의존 객체 생성

2. DI -> 스프링과 관련

3. 서비스 로케이터

## DI를 통한 의존 처리

의존 주입(DI, Dependency Injection)

= 의존하는 객체를 전달받는 방식

-> DI를 통해 변경의 유연함을 얻을 수 있다.

## DI와 의존 객체 변경의 유연함

DI를 사용하면 객체를 사용하는 클래스가 여러 개여도 변경할 곳은 의존 주입 대상이 되는 객체를 생성하는 코드 한 곳 뿐이다.

ex) MemberDao 클래스를 이용해 회원 데이터를 데이터베이스에 저장할 때, 캐시를 적용한 CachedMemberDao를 적용해야한다면 DI 방식은 단 한 줄만 수정하면 된다.

```java
public class MemberRegisterService {
    private MemberDao memberDao;

    public MemberRegisterService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    
    ...
}

public class ChangePasswordService {
    private MemberDao memberDao;

    public ChangePasswordService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    
    ...
}

public class Application {
    public static void main(String[] args) {
        MemberDao memberDao = new MemberDao();  // 변경 후: MemberDao memberDao = new CachedMemberDao();
        MemberRegisterService memberRegisterService = new MemberRegisterService(memberDao);
        ChangePasswordService changePasswordService = new ChangePasswordService(memberDao);
    }
}
```

## 객체 조립기(assembler)

= 객체를 생성하고 의존 객체를 주입

특정 객체가 필요한 곳에 객체 제공

| Assembler.java

```java
package assembler;

import spring.ChangePasswordService;
import spring.MemberDao;
import spring.MemberRegisterService;

public class Assembler {
    private MemberDao memberDao;
    private MemberRegisterService memberRegisterService;
    private ChangePasswordService changePasswordService;

    public Assembler() {
        memberDao = new MemberDao();
        memberRegisterService = new MemberRegisterService(memberDao);
        changePasswordService = new ChangePasswordService();
        changePasswordService.setMemberDao(memberDao);
    }

    public MemberDao getMemberDao() {
        return memberDao;
    }

    public MemberRegisterService getMemberRegisterService() {
        return memberRegisterService;
    }

    public ChangePasswordService getChangePasswordService() {
        return changePasswordService;
    }
}
```

## 스프링의 DI 설정

스프링

= DI를 지원하는 조립기

= 필요한 객체를 생성하고 생성한 객체에 의존을 주입

= 범용 조립기

### 스프링을 이용한 객체 조립과 사용

`@Configuration`: 스프링 설정 클래스 의미

`@Bean`: 해당 메소드가 생성한 객체 스프링 빈으로 설정

- 메소드 이름을 빈 객체의 이름으로 사용

ex) 설정 클래스를 이용해 컨테이너 생성 및 객체 사용

| AppContext.java

```java
package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import spring.ChangePasswordService;
import spring.MemberDao;
import spring.MemberRegisterService;

@Configuration
public class AppContext {

    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public MemberRegisterService memberRegisterService() {
        return new MemberRegisterService(memberDao());
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        ChangePasswordService changePasswordService = new ChangePasswordService();
        changePasswordService.setMemberDao(memberDao());
        return changePasswordService;
    }
}
```

| Application.java

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import config.ApplicationContext;
import spring.MemberRegisterService;

public class Application {
    private static ApplicationContext applicationContext = null;

    public static void main(String[] args) {
        applicationContext = new AnnotationConfigApplicationContext(ApplicationContext.class);
        
        ...
        // 스프링 컨테이너로부터 이름이 "memberRegisterService"인 빈 객체 가져옴
        MemberRegisterService memberRegisterService = applicationContext.getBean("memberRegisterService", MemberRegisterService.class);
    }
}
```

### DI 방식

1. 생성자 방식

생성자를 통해 의존 객체를 주입받아 필드에 할당

```java
public class MemberRegisterService {
    private MemberDao memberDao;

    public MemberRegisterService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Long register(RegisterRequest registerRequest) {
        Member member = memberDao.selectByEmail(registerRequest.getEmail());
        
        ...
        memberDao.insert(newMember);
        return newMember.getId();
    }
}
```

- 생성자에 전달할 의존 객체가 두개 이상이어도 동일한 방식으로 주입 가능

2. setter 메소드 방식

setter 메소드를 이용해 객체를 주입받아 필드에 할당

- setter 메소드 규칙

    - 메소드 이름이 set으로 시작
    - set 뒤에 첫 글자는 대문자로 시작
    - 파라미터 1개
    - 리턴 타입 void

| MemberInfoPrinter.java

```java
package spring;

public class MemberInfoPrinter {
    private MemberDao memberDao;
    private MemberPrinter memberPrinter;

    public void printMemberInfo(String email) {
        Member member = memberDao.selectByEmail(email);
        ...
        memberPrinter.print(member);
    }

    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public void setMemberPrinter(MemberPrinter memberPrinter) {
        this.memberPrinter = memberPrinter;
    }
}
```

| ApplicationContext.java

```java
import spring.MemberInfoPrinter;
...

@Configuration
public class AppContext {
    
    ...

    @Bean
    public MemberInfoPrinter memberInfoPrinter() {
        MemberInfoPrinter memberInfoPrinter = new MemberInfoPrinter();
        memberInfoPrinter.setMemberDao(memberDao());
        memberInfoPrinter.setMemberPrinter(memberPrinter());
        return memberInfoPrinter;
    }
}
```

#### 생성자 vs setter

1. 생성자 방식

장점

- 빈 객체를 생성하는 시점에 모든 의존 객체 주입

- 객체를 사용할 때 완전한 상태로 사용 가능

단점

- 생성자의 파라미터 개수가 많을 경우 각 인자가 어떤 의존 객체를 설정하는지 직접 확인해야 함

2. setter

장점

- 이름을 통해 어떤 의존 객체가 주입되는지 확인 가능

단점

- 필요한 의존 객체를 전달하지 않아도 빈 객체가 생성되기 때문에 객체 사용 시점에 NullPointerException 발생 가능

## @Configuration 설정 클래스의 @Bean 설정과 싱글톤

스프링 컨테이너가 생성한 빈은 싱글톤 객체다.

스프링 컨테이너는 @Bean이 붙은 메소드에 대해 한 개의 객체만 생성한다.

-> 스프링은 설정 클래스를 그대로 사용하지 않고, 설정 클래스를 상속한 새로운 설정 클래스를 사용하기 때문에 가능

## 두 개 이상의 설정 파일 사용하기

`@Autowired`: 스프링 빈에 의존하는 다른 빈을 자동으로 주입(해당 타입의 빈을 찾아서 필드에 할당)

| ApplicationContext2.java

```java
package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Bean;
import org.springframework.beans.factory.annotation.Configuration;

import spring.MemberRegisterService;
...

@Configuration
public class ApplicationContext2 {

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private MemberPrinter memberPrinter;

    @Bean
    public MemberRegisterService memberRegisterService() {
        return new MemberRegisterService(memberDao);
    }
    ...
}
```

설정 클래스가 두 개 이상일 때, 스프링 컨테이너 생성 시 파라미터로 설정 클래스 목록을 콤마(,)로 구분해서 전달하면 된다.

```java
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(ApplicationContext.class,ApplicationContext2.class);
```

## @Import 어노테이션 사용

두 개 이상의 설정 파일 사용 시 `@Import` 어노테이션 사용 가능

```java

@Configuration
@Import(ApplicationContext2.class)
public class ApplicationConfigImport {
```

배열을 이용해 두 개 이상의 설정 클래스도 지정 가능

```java

@Configuration
@Import({ApplicationContext.class, ApplicationContext2.class})
public class ApplicationConfigImport {
```

## getBean() 메소드 사용

`getBean()` 메소드: 사용할 빈 객체 가져옴

- 첫 번째 인자: 빈의 이름
- 두 번째 인자: 빈의 타임

```java
VersionPrinter versionPrinter = apllicationContext.getBean("versionPrinter", VersionPrinter.class);
```

빈 이름을 지정하지 않고 타입만으로 빈 가져오기 가능
(같은 타입의 빈 객체가 두 개 이상 존재하거나 없을 경우 예외 발생)

```java
VersionPrinter versionPrinter = apllicationContext.getBean(VersionPrinter.class);
```

## 주입 대상 객체를 빈 객체 설정 여부

주입할 객체가 꼭 스프링 빈이 아니어도 된다.

스프링 컨테이너는 자동 주입, 라이프사이클 관리 등 단순 객체 생성 외에 객체 관리를 위한 다양한 기능을 제공하는데 빈으로 등록한 객체에만 기능을 적용한다.

스프링 컨테이너가 제공하는 관리 기능이 필요없고 `getBean()` 메소드로 구할 필요가 없다면 빈 객체로 등록하지 않아도 된다.

> 최근에는 의존 자동 주입 기능을 프로젝트 전반에 걸쳐 사용하는 추세이기 때문에 보통 의존 주입 대상은 스프링 빈으로 등록한다.