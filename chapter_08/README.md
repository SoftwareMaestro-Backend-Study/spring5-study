# Chapter 08. DB 연동

## JDBC 프로그래밍의 단점을 보완하는 스프링

JDBC API 단점

- DB 연동에 필요한 Connection을 구한 다음 쿼리를 실행하기 위한 PreparedStatement를 생성한다. 쿼리 실행 후에는 finally 블록에서 ResultSet, PreparedStatement,
  Connection을 닫아야하는 구조적 반복이 발생

스프링 장점

- 구조적 반복을 줄이기 위해 템플릿 메소드 패턴과 전략 패턴을 엮은 JdbcTemplate 클래스를 제공

- 트랜잭션 관리 쉬움

    - 트랜잭션을 적용하고 싶은 메소드에 `@Transactional` 애노테이션을 붙이기만 하면 된다.

> ### 커넥션 풀
> 최초 연결에 따른 응답 속도 저하와 동시 접속자가 많을 때 발생하는 부하를 줄이기 위해 사용
> DB 커넥션 풀 제공 모듈: Tomcat JDBC, HikariCP, DBCP, c3p0 등

## 트랜잭션 처리

트랜잭션(transaction)

- 두 개 이상의 쿼리를 한 작업으로 실행해야 할 때 사용한다.
- 여러 쿼리를 논리적으로 하나의 작업으로 묶어준다.
- 한 트랜잭션으로 묶인 쿼리 중 하나라도 실패하면 전체 쿼리를 실패고 간주하고 실패 이전에 실행한 쿼리를 취소한다.
- 트랜잭션을 시작하면 트랜잭션을 커밋하거나 롤백할 때까지 실행한 쿼리들이 하나의 작업 단위가 된다.
    - JDBC는 Connection의 `setAutoCommit(false)`를 이용해 트랜잭션을 시작하고 `commit()`과 `rollback()`을 이용해 트랜잭션을 커밋하거나 롤백한다.

      ```java
      Connection connection = null;
      try {
        connection = DriverManager.getConnection(jdbcUrl, user, password);
        connection.setAutoCommit(false);    // 트랜잭션 범위 시작
        ...
        connection.commit();    // 트랜잭션 범위 종료: 커밋
      } catch (SQLException e) {
        if (connection != null) {
            // 트랜잭션 범위 종료: 롤백
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
      } finally {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
      }
      ```
      코드로 직접 트랜잭션 범위를 관리하면 트랜잭션을 커밋하거나 롤백하는 코드를 누락하기 쉽고, 구조적인 중복이 발생한다.

롤백(rollback): 쿼리 실행 결과를 취소하고 DB를 기존 상태로 되돌리는 것

커밋(commit): 트랜잭션으로 묶인 모든 쿼리가 성공해서 쿼리 결과를 DB에 실제로 반영하는 것

### @Transactional을 이용한 트랜잭션 처리

스프링이 제공하는 `@Transactional` 애노테이션을 트랜잭션 범위에서 실행하고 싶은 메소드에 붙임으로써 트랜잭션 범위를 쉽게 지정할 수 있다.

`@Transactional` 애노테이션이 제대로 동작하기 위해 스프링 설정에 추가해야할 내용

1. 플랫폼 트랜잭션 매니저(PlatformTransactionManager) 빈 설정
    - PlatformTransactionManager는 스프링이 제공하는 트랜잭션 매니저 인터페이스
    - 스프링은 구현기술에 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해 이 인터페이스 사용
2. `@Transactional` 애노테이션 활성화 설정
    - `@EnableTransactionManagement` 애노테이션은 `@Transactional` 애노테이션이 붙은 메소드를 트랜잭션 범위에서 실행하는 기능 활성화

### @Transactional과 프록시

스프링은 `@Transactional` 애노테이션을 이용해 트랜잭션을 처리하기 위해 내부적으로 AOP를 사용한다. => 트랜잭션 처리도 프록시를 통해 이루어짐

1. `@EnableTransactionManagement` 태그 사용 시 스프링은 `@Transactional` 애노테이션이 적용된 빈 객체 찾아 알맞은 프록시 객체 생성
2. `getBean()` 메소드 실행 시 트랜잭션 처리를 위한 프록시 객체 리턴
3. 프록시 객체에서 `@Transactional` 애노테이션이 붙은 메소드 호출 시 PlatformTransactionManager를 사용해 트랜잭션 시작
4. 트랜잭션 시작 후 실제 객체 메소드 호출
5. 성공적으로 실행 시 트랜잭션 커밋, RuntimeException 발생 시 롤백

### @Transactional 적용 메소드의 롤백 처리

별도 설정 추가하지 않으면 RuntimeException 발생 시 트랜잭션을 롤백한다.

JdbcTemplate은 DB 연동 과정에서 문제 발생 시 DataAccessException이 발생하는데, DataAccessException 역시 RuntimeException을 상속받고 있어
JdbcTemplate의 기능 실행 중 익셉션 발생해도 프록시는 트랜잭션을 롤백한다.

#### rollbackFor & noRollbackFor

`@Transactional`의 rollbackFor 속성에 익센션을 지정하면 해당 익셉션 발생 시 트랜잭션을 롤백한다.

noRollbackFor 속성에 지정된 익셉션은 발생해도 트랜잭션이 롤백되지 않는다.

### @Transactional의 주요 속성

value

- String 타입
- 트랜잭션 관리 시 사용할 PlatformTransactionManager 빈의 이름 지정
- 기본값 " "

propagation

- Propagation 타입
- 트랜잭션 전파 타입 지정
- 기본값 Propagation.REQUIRED

isolation

- Isolation 타입
- 트랜잭션 격리 레벨 지정
- 기본값 Isolation.DEFAULT

timeout

- int 타입
- 트랜잭션 제한 시간 지정(초 단위 지정)
- 기본값 -1(데이터베이스의 타임아웃 시간 사용)

#### Propagation 열거 타입

|값|설명|
|:---:|---|
|REQUIRED|메소드 수행 시 트랜잭션 필요. 현재 진행 중인 트랜잭션 존재하면 해당 트랜잭션 사용, 존재하지 않으면 새로운 트랜잭션 생성|
|MANDATORY|메소드 수행 시 트랜잭션 필요. REQUIRED와 달리 진행 중인 트랜잭션 존재하지 않을 경우 익셉션 발생|
|REQUIRES_NEW|항상 새로운 트랜잭션 시작. 진행 중인 트랜잭션 존재하면 기존 트랜잭션 일시 중지하고 새로운 트랜잭션 시작. 새로 시작된 트랜잭션 종료 후 기존 트랜잭션 계속|
|SUPPORTS|메소드가 트랜잭션을 필요로 하지는 않지만, 진행 중인 트랜잭션이 존재하면 트랜잭션을 사용. 진행 중인 트랜잭션이 존재하지 않더라도 메소드는 정상 동작|
|NOT_SUPPORTED|메소드가 트랜잭션을 필요로 하지 않음. SUPPORTS와 달리 진행 중인 트랜잭션이 존재할 경우 메소드가 실행되는 동안 트랜잭션은 일시 중지되고 메소드 실행 종료 후 트랜잭션 계속 진행|
|NEVER|메소드가 트랜잭션을 필요로 하지 않음. 만약 진행 중인 트랜잭션이 존재하면 익셉션 발생|
|NESTED|진행 중인 트랜잭션 존재하면 기존 트랜잭션에 중첩된 트랜잭션에서 메소드를 실행. 진행 중인 트랜잭션이 존재하지 않으면 REQUIRED와 동일하게 동작(JDBC 3.0 드라이버 사용 or JTA Provider가 해당 기능 지원 시 적용 가능)|

#### Isolation 열거 타입

|값|설명|
|:---:|---|
|DEFAULT|기본 설정 사용|
|READ_UNCOMMITTED|다른 트랜잭션 커밋하지 않은 데이터 읽기 가능|
|READ_COMMITTED|다른 트랜잭션이 커밋한 데이터 읽기 가능|
|REPEATABLE_READ|처음에 읽어 온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 가짐|
|SERIALIZABLE|동일한 데이터에 대해 동시에 두 개 이상의 트랜잭션 수행 불가능|

> 트랜잭션 격리 레벨은 동시에 DB에 접근할 때 그 접근을 어떻게 제어할지에 대한 설정
> 격리 레벨에 대해 잘 모르는 초보 개발자는 격리 레벨 설정을 건드리지 말고 격리 레벨 설정이 필요한지 선배 개발자에게 물어보자

### @EnableTransactionManagement 애노테이션의 주요 속성

proxyTargetClass

- 클래스를 이용해서 프록시를 생성할지 여부 지정
- 기본값 false(인터페이스를 이용해서 프록시 생성)

order

- AOP 적용 순서 지정
- 기본값은 가장 낮은 우선순위인 int의 최댓값

### 트랜잭션 전파

```java
public class PostService {

    private CommentService commentService;

    public PostService(CommentService commentService) {
        this.commentService = commentService;
    }

    @Transactional
    public void delete() {
        commentService.deleteAll();
    }
}

public class CommentService {

    @Transactional
    public void deleteAll() { ...}
}
```

`@Transactional`의 속성 중 Propagation이 기본값인 REQUIRED인 경우 아래와 같은 일이 일어난다.

1. PostService의 `delete()` 메소드 호출 시 트랜잭션 시작
2. `delete()` 메소드 내부에서 `deleteAll()` 메소드를 호출하면 이미 트랜잭션이 존재하므로 트랜잭션을 새로 생성하지 않는다.

```java
public class PostService {

    private CommentService commentService;

    public PostService(CommentService commentService) {
        this.commentService = commentService;
    }

    @Transactional
    public void delete() {
        commentService.deleteAll();
    }
}

public class CommentService {

    // @Transactional 없음
    public void deleteAll() { ...}
}
```

트랜잭션이 시작되고, 내부에서 호출하는 메소드의 `@Transactional`이 붙어있지 않아도 JdbcTemplate 클래스때문에 트랜잭션 범위에서 쿼리를 실행할 수 있다.

JdbcTemplate은 진행 중인 트랜잭션이 존재하면 해당 트랜잭션 범위에서 쿼리를 실행한다.