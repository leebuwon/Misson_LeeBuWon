package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.BaseEntity;
import com.ll.gramgram.base.appconfig.AppConfig;
import com.ll.gramgram.base.event.EventAfterLike;
import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.event.EventBeforeCancelLike;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    private final ApplicationEventPublisher publisher;

    @Transactional
    public RsData<LikeablePerson> like(Member actor, String username, int attractiveTypeCode) {
        RsData canLikeRsData = canLike(actor, username, attractiveTypeCode);

        if (canLikeRsData.isFail()) return canLikeRsData;

        if (canLikeRsData.getResultCode().equals("S-2")) return modifyAttractive(actor, username, attractiveTypeCode);

        InstaMember fromInstaMember = actor.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();
        log.info("toInstaMember = {}", toInstaMember);
        log.info("attractiveTypeCode = {}", attractiveTypeCode);

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(actor.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .modifyUnlockDate(AppConfig.genLikeablePersonModifyUnlockDate())
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 너가 좋아하는 호감표시 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 호감표시 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        // TODO: 05/01 likeablePerson 추가시 쿨타임 체크하기


        // 좋아요 수 증가
        publisher.publishEvent(new EventAfterLike(this, likeablePerson));

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
        // 너가 생성한 좋아요가 사라졌어.
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

        // 너가 받은 좋아요가 사라졌어.
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);

        // 좋아요 수 감소
        publisher.publishEvent(new EventBeforeCancelLike(this, likeablePerson));

        likeablePersonRepository.delete(likeablePerson);

        String likeCanceledUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(likeCanceledUsername));

    }

    public RsData canDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId) {
            return RsData.of("F-2", "권한이 없습니다.");
        }

        // TODO: likeablePerson 추가시 쿨타임 체크하기
        LocalDateTime modifyDateTime = likeablePerson.getModifyUnlockDate();

        RsData rsDataModifyCoolTime = checkCoolTime(modifyDateTime);
        if (rsDataModifyCoolTime.getResultCode().equals("F-1")) {
            return rsDataModifyCoolTime;
        }

        return RsData.of("S-1", "삭제가능합니다.");
    }

    private RsData canLike(Member actor, String username, int attractiveTypeCode) {
        if (!actor.hasConnectedInstaMember()){
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (fromInstaMember.getUsername().equals(username)){
            return RsData.of("F-2", "본인을 호감상대로 추가할 수 없습니다.");
        }

        // 액터가 생성한 '좋아요' 가져오기
        List<LikeablePerson> fromLikeablePeople = fromInstaMember.getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople.stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        //TODO: case 4 - 이미 좋아요 된 사람에게 또 좋아요하는 것을 방지하는 로직
        if (fromLikeablePerson != null && fromLikeablePerson.getAttractiveTypeCode() == attractiveTypeCode){
            return RsData.of("F-3", "이미 %s님에 대해서 호감표시를 했습니다.".formatted(username));
        }

        if (fromLikeablePerson != null){
            return RsData.of("S-2", "%s님에 대해서 호감표시가 가능합니다.".formatted(username));
        }

        //TODO: case 5 - 좋아요 목록의 사람이 10명이 넘어가면 에러메시지 출력
        long likeablePersonFromMax = AppConfig.getLikeablePersonFromMax();

        if ( fromLikeablePeople.size() >= likeablePersonFromMax){
            return RsData.of("F-4", "최대 %d명에 대해서만 호감표시가 가능합니다.".formatted(likeablePersonFromMax));
        }

        return RsData.of("S-1", "%s님에 대해서 호감표시가 가능합니다.".formatted(username));

    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, Long id, int attractiveTypeCode) {
        Optional<LikeablePerson> likeablePersonOptional = findById(id);

        if (likeablePersonOptional.isEmpty()) {
            return RsData.of("F-1", "존재하지 않는 호감표시입니다.");
        }

        LikeablePerson likeablePerson = likeablePersonOptional.get();

        return modifyAttractive(actor, likeablePerson, attractiveTypeCode);
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, LikeablePerson likeablePerson, int attractiveTypeCode) {
        RsData canModifyRsData = canModifyLike(actor, likeablePerson);

        if (canModifyRsData.isFail()) {
            return canModifyRsData;
        }

        String oldAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();
        String username = likeablePerson.getToInstaMember().getUsername();

        modifyAttractionTypeCode(likeablePerson, attractiveTypeCode);

        String newAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();

        return RsData.of("S-3", "%s님에 대한 호감사유를 %s에서 %s(으)로 변경합니다.".formatted(username, oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName), likeablePerson);
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, String username, int attractiveTypeCode) {
        // 액터가 생성한 `좋아요` 들 가져오기
        List<LikeablePerson> fromLikeablePeople = actor.getInstaMember().getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (fromLikeablePerson == null) {
            return RsData.of("F-7", "호감표시를 하지 않았습니다.");
        }

        return modifyAttractive(actor, fromLikeablePerson, attractiveTypeCode);
    }

    public Optional<LikeablePerson> findByFromInstaMember_usernameAndToInstaMember_username(String fromInstaMemberUsername, String toInstaMemberUsername) {
        return likeablePersonRepository.findByFromInstaMember_usernameAndToInstaMember_username(fromInstaMemberUsername, toInstaMemberUsername);
    }

    private void modifyAttractionTypeCode(LikeablePerson likeablePerson, int attractiveTypeCode) {
        int oldAttractiveTypeCode = likeablePerson.getAttractiveTypeCode();
        RsData rsData = likeablePerson.update(attractiveTypeCode);

        if (rsData.isSuccess()) {
            publisher.publishEvent(new EventAfterModifyAttractiveType(this, likeablePerson, oldAttractiveTypeCode, attractiveTypeCode));
        }
    }

    public RsData canModifyLike(Member actor, LikeablePerson likeablePerson) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (!Objects.equals(likeablePerson.getFromInstaMember().getId(), fromInstaMember.getId())) {
            return RsData.of("F-2", "해당 호감표시를 취소할 권한이 없습니다.");
        }

        // TODO: likeablePerson 추가시 쿨타임 체크하기
        LocalDateTime modifyDateTime = likeablePerson.getModifyUnlockDate();

        RsData rsDataModifyCoolTime = checkCoolTime(modifyDateTime);
        if (rsDataModifyCoolTime.getResultCode().equals("F-1")) {
            return rsDataModifyCoolTime;
        }

        return RsData.of("S-1", "호감표시취소가 가능합니다.");
    }

    // 쿨타임 체크
    private RsData checkCoolTime(LocalDateTime modifyDateTime) {
        LocalDateTime nowDateTime = LocalDateTime.now();
        log.info("nowDateTime = {} ", nowDateTime);

        log.info("modifyDateTime = {}", modifyDateTime);

        Duration duration = Duration.between(LocalDateTime.now(), modifyDateTime);
        long seconds = duration.toSeconds();
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (nowDateTime.isBefore(modifyDateTime)){
            return RsData.of("F-1", "해당 호감사유변경 및 호감취소는 %d시 %d분 %d초 동안 수정이 불가능힙니다.".formatted(hours, remainingMinutes, remainingSeconds));
        }

        return RsData.of("S-1", "수정 가능합니다.");
    }

    //TODO : 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현 (필수미션)
    public List<LikeablePerson> filterGender(List<LikeablePerson> likeablePeople, String gender) {
        List<LikeablePerson> filterGender = likeablePeople.stream()
                .filter(p -> p.getFromInstaMember().getGender().equals(gender))
                .collect(Collectors.toList());

        return filterGender;
    }

    //TODO : 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현 (선택미션)
    public List<LikeablePerson> filterAttractiveTypeCode(List<LikeablePerson> likeablePeople, String attractiveTypeCode) {
        int toAttractiveTypeCode = Integer.parseInt(attractiveTypeCode);

        List<LikeablePerson> filterAttractiveTypeCode = likeablePeople.stream()
                .filter(p -> p.getAttractiveTypeCode() == toAttractiveTypeCode)
                .collect(Collectors.toList());

        return filterAttractiveTypeCode;
    }

    //TODO : 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능
    public List<LikeablePerson> filterSort(List<LikeablePerson> likeablePeople, String sortCode) {
        int toSortCode = Integer.parseInt(sortCode);
        log.info("toSortCode = {}", sortCode);

        if (toSortCode == 2){
            List<LikeablePerson> filterSortCode = likeablePeople.stream()
                    .sorted(Comparator.comparing(LikeablePerson::getCreateDate))
                    .collect(Collectors.toList());

            log.info("filterSortCode = {}", filterSortCode);

            return filterSortCode;
        }

        if (toSortCode == 3){
            List<LikeablePerson> filterSortCode = likeablePeople.stream()
                    .sorted(Comparator.comparing((LikeablePerson likeablePerson) -> likeablePerson.getFromInstaMember().getToLikeablePeople().size()).reversed())
                    .collect(Collectors.toList());

            log.info("filterSortCode = {}", filterSortCode);

            return filterSortCode;
        }

        if (toSortCode == 4){
            List<LikeablePerson> filterSortCode = likeablePeople.stream()
                    .sorted(Comparator.comparing((LikeablePerson likeablePerson) -> likeablePerson.getFromInstaMember().getToLikeablePeople().size()))
                    .collect(Collectors.toList());

            log.info("filterSortCode = {}", filterSortCode);

            return filterSortCode;
        }

        if (toSortCode == 5){
            List<LikeablePerson> filterSortCode = likeablePeople.stream()
                    .sorted(Comparator.comparing((LikeablePerson likeablePerson) -> likeablePerson.getFromInstaMember().getGender().equals("W") ? 1 : 0)
                            .thenComparing(LikeablePerson::getCreateDate).reversed())
                    .collect(Collectors.toList());

            log.info("filterSortCode = {}", filterSortCode);

            return filterSortCode;
        }

        if (toSortCode == 6){
            List<LikeablePerson> filterSortCode = likeablePeople.stream()
                    .sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode)
                            .thenComparing(Comparator.comparing(BaseEntity::getCreateDate).reversed()))
                    .collect(Collectors.toList());

            return filterSortCode;
        }

        // default 1이 넘어오니깐 따로 if문을 설정해주지 않았다.
        List<LikeablePerson> filterSortCode = likeablePeople.stream()
                .sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed())
                .collect(Collectors.toList());

        log.info("filterSortCode = {}", filterSortCode);

        return filterSortCode;

    }
}

