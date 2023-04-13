package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appconfig.AppConfig;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if (member.hasConnectedInstaMember() == false) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-2", "본인을 호감상대로 등록할 수 없습니다.");
        }

        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();
        log.info("toInstaMember = {}", toInstaMember);
        log.info("attractiveTypeCode = {}", attractiveTypeCode);

        //TODO: case 4 - 이미 좋아요 된 사람에게 또 좋아요하는 것을 방지하는 로직
        Optional<LikeablePerson> existingLikeablePersonData = likeablePersonRepository.findByFromInstaMemberAndToInstaMemberAndAttractiveTypeCode(fromInstaMember, toInstaMember, attractiveTypeCode);
        if (existingLikeablePersonData.isPresent()) {
            return RsData.of("F-3", "이미 좋아요된 사람에게는 같은 호감유형으로 좋아요 추가할 수 없습니다.");
        }

        //TODO: case 6 - 케이스 4 가 발생했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우에는 성공으로 처리한다.
        Optional<LikeablePerson> existingLikeablePerson = likeablePersonRepository.findByFromInstaMemberAndToInstaMember(fromInstaMember, toInstaMember);
        if (existingLikeablePerson.isPresent()) {
            LikeablePerson updateLikeablePerson = existingLikeablePerson.get(); // 1, 정보를 가져온다.
            updateLikeablePerson.update(attractiveTypeCode); // 2, set -> get으로 업데이트 수정
            likeablePersonRepository.save(updateLikeablePerson); // 3, 다시 저장해준다.

            return RsData.of("S-2", "입력하신 인스타유저(%s)의 호감유형을 수정하였습니다.".formatted(username), updateLikeablePerson);
        }

        //TODO: case 5 - 좋아요 목록의 사람이 10명이 넘어가면 에러메시지 출력
        log.info("member.getInstaMember().getFromLikeablePeople().size() = {}", member.getInstaMember().getFromLikeablePeople().size());

        if (member.getInstaMember().getFromLikeablePeople().size() >= AppConfig.getLikeablePersonFromMax()) {
            return RsData.of("F-4", "좋아요 한 사람은 10명을 넘길 수 없습니다.");
        }

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {
        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));
    }

    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId)
            return RsData.of("F-2", "권한이 없습니다.");

        return RsData.of("S-1", "삭제가능합니다.");
    }

}
