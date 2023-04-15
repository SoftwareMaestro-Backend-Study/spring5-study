package chap02;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {

    @Bean
    public Greeter greeter() {
        Greeter greeter = new Greeter();
        greeter.setFormat("%s, hello!");
        return greeter;
    }

    @Bean
    public Greeter greeter1() {
        Greeter greeter = new Greeter();
        greeter.setFormat("hello, %s!");
        return greeter;
    }
}
