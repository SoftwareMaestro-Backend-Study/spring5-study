# AOP 프로그래밍
책의 내용과 살짝 다를 수 있습니다.

### 핵심 기능들에 부가 기능을 추가할 때

책에서의 예는 **factorial을 계산하는 기능**(핵심 기능)에 **실행 시간**(부가 기능)을 추가하였다.

핵심 기능들에 부가 기능을 추가할 때 코드의 중복을 피할 수 있을까?

추가적으로, 핵심 기능과 부가 기능을 분리할 수 있을까?

답은 AOP 프로그래밍이다.

## AOP란?

여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법

쉽게 말해, 핵심 기능에 부가 기능을 추가할 수 있는 기법인데, 핵심 기능과 부가 기능을 분리해서 

개발자가 핵심 기능에만 집중할 수 있도록 만들어주는 기법

핵심 기능과 부가 기능이 분리되므로, 단일 책임 원칙을 지킬 수 있다 !

스프링을 사용할 때는, 런타임에 **프록시 객체**를 생성해서 공통 기능을 삽입하는 방법을 이용한다.

스프링은 프록시 객체를 자동으로 만들어준다.

근데 어떻게 만들어줄까?

이를 알기 전에 일단 용어를 먼저 정리한다.

3가지만 알고가자.

| Advice | 핵심 로직에 적용할 공통 기능 |
| --- | --- |
| Pointcut | Advice가 적용되는 지점을 의미한다. 즉, 공통 기능이 적용 되는 지점 |
| Advisor | Advice + Pointcut = Advisor / 하나의 공통 기능과 하나의 적용 지점을 세트로 묶어 놓은 것 |

 

간단하게 작성한 스프링 AOP의 동작 흐름 

1. Bean으로 등록된 **Advisor들**을 스프링이 조회한다.
2. Bean으로 등록된 객체들에 대해 Advisor 안의 **Pointcut**을 이용하여 공통 기능이 적용되는 객체인지 확인한다.
3. 공통 기능이 적용되는 객체는 **Bean Post Processor (빈 후처리기)**를 통해 프록시 객체로 변환되어 Bean으로 등록된다.
4. 따라서 프록시로 등록된 Bean을 사용하면 부가 기능까지 사용할 수 있게 된다!

스프링은 Bean Post Processor를 이용하여 Pointcut에 맞는 Bean 객체를 프록시로 변환해준다!!

---

## Pointcut, Advice, Advisor

예를 들어보자.

우리가 자주 사용하는 @Transactional annotation 도 AOP를 이용하는 대표적인 예시이다.

| 트랜잭션 시작 |
| --- |
| 핵심 기능 |
| 트랜잭션 종료 |

우리는 개발을 할 때 핵심 기능을 작성하고 @Transactional annotation만 붙이면 트랜잭션이라는 부가 기능을 사용할 수 있다.

@Transactional annotation이 붙은 Bean 객체를 프록시로 변환하기 위해 어떤 Advisor가 필요할까?

아래는 간단한게 구현한 Advisor이다.

```java
@Aspect
public class TransactionalAspect {

    @Around("@within(org.springframework.transaction.annotation.Transactional)")
    public Object transactional(ProceedingJoinPoint joinPoint) {
        ...
        **트랜잭션.시작();**

        ...        

        **final Object result = joinPoint.proceed();**

        ...
        
        **트랜잭션.종료();**
        
        ...
    }
}
```

## Pointcut

`@within(org.springframework.transaction.annotation.Transactional)` 

이 Pointcut은 Transactional annotation이 붙은 메소드를 찾는다. 

즉, 이 Pointcut을 통해 부가 기능을 적용할 대상(프록시로 변환할 대상)을 찾는다.

나는 Pointcut을 `프록시를 적용할 대상을 걸러주는 조건` 이라고 이해하고 있다. 

## Advice

Pointcut으로 걸러낸 대상들에 적용할 부가 기능을 의미한다.

여기서는 트랜잭션의 시작, 종료 등의 로직이라고 생각하면 편하다.

### Advisor

간단하게 TransactionalAspect 클래스를 Advisor라고 생각하자.

하나의 Pointcut과 하나의 Advice 세트

이 Advisor를 Bean으로 등록하면 

스프링이 알아서 Pointcut으로 Bean 객체를 거르고, Advice를 적용한 프록시 객체를 만들어 주는 것이다.

우리는 이러한 방식으로 AOP를 이용하고 있었다.
