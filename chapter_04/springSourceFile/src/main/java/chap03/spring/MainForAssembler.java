package chap03.spring;

import chap03.assembler.Assembler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainForAssembler {
    private static Assembler assembler = new Assembler();

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("명령어를 입력하세요");
            String command = reader.readLine();
            if (command.equals("exit")) {
                System.out.println("종료합니다.");
                break;
            }

            if (command.startsWith("new ")) {
                processNewCommand(command.split(" "));
                continue;
            } else if (command.startsWith("change ")) {
                processChangeCommand(command.split(" "));
                continue;
            }
            else
                printHelp();
        }
    }

    private static void processNewCommand(String[] arg) {
        if (arg.length != 5) {
            printHelp();
            return;
        }
        MemberRegisterService memberRegisterService = assembler.getMemberRegisterService();
        RegisterRequest request = new RegisterRequest();
        request.setEmail(arg[1]);
        request.setName(arg[2]);
        request.setPassword(arg[3]);
        request.setComfirmPassword(arg[4]);

        if (!request.isPasswordEqualToConfirmPassword()) {
            System.out.println("암호와 확인이 일치하지 않습니다.");
            return;
        }
        try {
            memberRegisterService.regist(request);
            System.out.println("등록했습니다.");
        } catch (DuplicateMemberException e) {
            System.out.println("이미 존재하는 이메일입니다.");
        }
    }

    private static void processChangeCommand(String[] arg) {
        if (arg.length != 4) {
            printHelp();
            return;
        }
        ChangePasswordService changePasswordService = assembler.getChangePasswordService();

        try {
            changePasswordService.changePassword(arg[1], arg[2], arg[3]);
            System.out.println("암호를 변경했습니다.");
        } catch (MemberNotFoundException e) {
            System.out.println("존재하지 않는 이메일입니다.");
        } catch (WrongIdPasswordException e) {
            System.out.println("이메일과 암호가 일치하지 않습니다.");
        }
    }

    private static void printHelp(){
        System.out.println("잘못된 명령입니다. 아래 명령어 사용법을 확인하세요");
        // todo : p.73 따라서 작성
    }
}
