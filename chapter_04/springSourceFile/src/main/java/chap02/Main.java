package chap02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppContext.class);
        Greeter greeter1 = applicationContext.getBean("greeter", Greeter.class);
        String message = greeter1.greet("chap3");
        // ch 2.1 hello world!
        System.out.println(message);

        // ch 2.2 Singleton
        Greeter greeter2 = applicationContext.getBean("greeter", Greeter.class);
        System.out.println("(greeter1 == greeter2) = " + (greeter1 == greeter2));

        applicationContext.close();
    }
}
