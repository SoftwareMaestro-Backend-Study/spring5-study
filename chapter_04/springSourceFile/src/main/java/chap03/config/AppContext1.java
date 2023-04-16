package chap03.config;

import chap03.spring.MemberDao;
import chap03.spring.MemberPrinter;
import org.springframework.context.annotation.Bean;

public class AppContext1 {
    @Bean
    public MemberDao memberDao(){
        return new MemberDao();
    }

    @Bean
    public MemberPrinter memberPrinter(){
        return new MemberPrinter();
    }

}
