package chap03.spring;

public class Main {
    public static void main(String[] args) {
        MemberDao memberDao = new MemberDao();
        MemberRegisterService memberRegisterService = new MemberRegisterService(memberDao);
        ChangePasswordService changePasswordService = new ChangePasswordService(memberDao);

    }
}
