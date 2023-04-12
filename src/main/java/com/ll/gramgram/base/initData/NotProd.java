package com.ll.gramgram.base.initData;

import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "test"})
public class NotProd {
    @Bean
    CommandLineRunner initData(
            MemberService memberService,
            InstaMemberService instaMemberService,
            LikeablePersonService likeablePersonService
    ) {
        return args -> {
            Member memberAdmin = memberService.join("admin", "1234").getData();
            Member memberUser1 = memberService.join("user1", "1234").getData();
            Member memberUser2 = memberService.join("user2", "1234").getData();
            Member memberUser3 = memberService.join("user3", "1234").getData();
            Member memberUser4 = memberService.join("user4", "1234").getData();

            Member memberUser5ByKakao = memberService.whenSocialLogin("KAKAO", "KAKAO__2733187710").getData();
            Member memberUser6ByGoogle = memberService.whenSocialLogin("GOOGLE", "GOOGLE__116152084938460177380").getData();

            instaMemberService.connect(memberUser2, "bw1111", "M");
            instaMemberService.connect(memberUser3, "bw2222", "W");
            instaMemberService.connect(memberUser4, "bw3333", "M");
            instaMemberService.connect(memberUser5ByKakao, "bw1611", "M");

            likeablePersonService.like(memberUser5ByKakao, "bw1111", 1);
            likeablePersonService.like(memberUser5ByKakao, "bw2222", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw3333", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw4444", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw5555", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw6666", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw7777", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw8888", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw9999", 2);
            likeablePersonService.like(memberUser5ByKakao, "bw1010", 2);
        };
    }
}
