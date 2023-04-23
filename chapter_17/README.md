## 스프링 profile

개발 환경과 운영 환경을 분리하기 위한 설정이다.

ex)

개발 환경에서 운영 환경의 DB를 가지고 개발할 수 없다.

개발 환경에서는 따로 환경을 구성해야한다.

개발 환경의 DataSource와 운영 환경의 DataSource가 구분되어야한다. 

이때 사용하는 것이 profile이다.

## Profile 설정

1. @Configuration에서 프로필 사용하기

```java
@Configuration
@Profile("dev")
public class config {
	...

	@Bean
	public DataSource dataSource() {
		...
	}
}

```

위 설정 파일을 통해 등록된 DataSource 빈은 `dev` profile이 활성되었을 경우에만 Bean으로 등록된다.

1. application.yml에서 프로필 설정하기

```yaml
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:mysql://${DATABASE_HOST}:3306/${DATABASE_NAME_DEV}?serverTimezone=Asia/Seoul&character_set_server=utf8mb4
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
```

위처럼 profile이 `dev`로 설정되었을 때 yml 에 작성된 DataSource가 빈으로 등록된다.

## Profile 활성화

1. 개발 환경에서 진행할 때

Edit Configuration → Active profiles 에서 원하는 profile 작성

<img width="526" alt="스크린샷 2023-04-23 오후 11 04 09" src="https://user-images.githubusercontent.com/79205414/233845396-eea1ffcc-5f8d-4302-bf22-a10b2be7b7b9.png">
<img width="587" alt="스크린샷 2023-04-23 오후 11 04 49" src="https://user-images.githubusercontent.com/79205414/233845401-9f4705fb-f605-4bb4-ba4d-34c2c2239b85.png">



2. 시스템 프로퍼티 설정

`java -Dspring.profiles.active=dev main.Main`
