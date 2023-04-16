package chap03.config;

import chap03.spring.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {
    @Bean
    public MemberDao memberDao(){
        return new MemberDao();
    }

    @Bean
    public MemberRegisterService memberRegisterService(){
        return new MemberRegisterService(memberDao());
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        return new ChangePasswordService(memberDao());
    }

    @Bean
    public MemberPrinter memberPrinter(){
        return new MemberPrinter();
    }

    @Bean
    public MemberListPrinter memberListPrinter(){
        return new MemberListPrinter(memberDao(), memberPrinter());
    }

    @Bean
    public MemberInfoPrinter memberInfoPrinter(){
        MemberInfoPrinter memberInfoPrinter = new MemberInfoPrinter();
        memberInfoPrinter.setPrinter(memberPrinter());
        memberInfoPrinter.setMemberDao(memberDao());
        return memberInfoPrinter;
    }

    @Bean
    public VersionPrinter versionPrinter() {
        VersionPrinter versionPrinter = new VersionPrinter();
        versionPrinter.setMajorVersion(5);
        versionPrinter.setMinorVersion(0);
        return versionPrinter;
    }
}
