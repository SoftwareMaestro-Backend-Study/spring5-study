package chap03.assembler;

import chap03.spring.ChangePasswordService;
import chap03.spring.MemberDao;
import chap03.spring.MemberRegisterService;

public class Assembler {

    private MemberDao memberDao;
    private MemberRegisterService memberRegisterService;
    private ChangePasswordService changePasswordService;

    public Assembler() {
        MemberDao memberDao = new MemberDao();
        MemberRegisterService memberRegisterService = new MemberRegisterService(memberDao);
        ChangePasswordService changePasswordService = new ChangePasswordService(memberDao);
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
