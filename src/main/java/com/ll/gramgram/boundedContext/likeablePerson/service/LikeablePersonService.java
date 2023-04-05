package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if ( member.hasConnectedInstaMember() == false ) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(member.getInstaMember()) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    /**
     * instaMemberId로 삭제하는 방법
     */
    @Transactional // 항상 기억하자.. 이거 때문에 오래걸렸다...
    public RsData<LikeablePerson> delete(Long likeableId, InstaMember instaMember, Long instaMemberId) {
        LikeablePerson likeablePerson = likeablePersonRepository.findById(likeableId).orElse(null);
        log.info("likeablePerson = {}", likeablePerson);
        log.info("instaMemberId = {}", instaMemberId);

        if (likeablePerson == null) {
            return RsData.of("F-2", "입력하신 아이디에 해당하는 '좋아요' 누른 사람이 존재하지 않습니다.");
        }

        if (!instaMember.getId().equals(instaMemberId)){
            return RsData.of("F-1", "본인의 계정으로 '좋아요' 누른 사람만 취소할 수 있습니다.");
        }

        likeablePersonRepository.delete(likeablePerson); // 삭제

        return RsData.of("S-1", "선택하신 인스타유저를 호감상대에서 취소하였습니다.");
    }

    /**
     * instaUsername으로 삭제하는 방법
     */
//    @Transactional
//    public RsData<LikeablePerson> delete(Long likeableId, InstaMember instaMember, String username) {
//        LikeablePerson likeablePerson = likeablePersonRepository.findById(likeableId).orElse(null);
//        log.info("likeablePerson = {}", likeablePerson);
//
//        if (likeablePerson == null) {
//            return RsData.of("F-2", "입력하신 아이디에 해당하는 '좋아요' 누른 사람이 존재하지 않습니다.");
//        }
//
//        if (!instaMember.getUsername().equals(username)){
//            return RsData.of("F-1", "본인의 계정으로 '좋아요' 누른 사람만 취소할 수 있습니다.");
//        }
//
//        likeablePersonRepository.delete(likeablePerson); // 삭제
//
//        return RsData.of("S-1", "선택하신 인스타유저를 호감상대에서 취소하였습니다.");
//    }
}
