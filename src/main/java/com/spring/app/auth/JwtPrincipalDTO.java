package com.spring.app.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/* ===== (#JWT-NOTICE-01) ===== */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtPrincipalDTO {

    private String principalType;   // MEMBER / ADMIN / GUEST 구분
    private Long principalNo;       // member_no 또는 admin_no
    private String loginId;         // 로그인 아이디
    private String name;            // 이름
    private String adminType;       // HQ / BRANCH
    private Long hotelId;           // 지점 관리자 소속 호텔번호
    private List<String> roles;     // 권한목록
}