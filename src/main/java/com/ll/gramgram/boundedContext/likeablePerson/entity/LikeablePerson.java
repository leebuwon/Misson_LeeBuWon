package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.base.BaseEntity;
import com.ll.gramgram.base.appconfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.standard.util.Ut;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadPoolExecutor;

@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Getter
public class LikeablePerson extends BaseEntity {
    private LocalDateTime modifyUnlockDate;

    @ManyToOne(fetch = FetchType.LAZY) // Eager -> Lazy
    @ToString.Exclude
    private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
    private String fromInstaMemberUsername; // 혹시 몰라서 기록

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
    private String toInstaMemberUsername; // 혹시 몰라서 기록

    private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)

    public String getAttractiveTypeDisplayName() {
        return switch (attractiveTypeCode) {
            case 1 -> "외모";
            case 2 -> "성격";
            default -> "능력";
        };
    }
    // 추후 수정될 코드 html코드가 entity에 있으면 좋지 않다.
    public String getAttractiveTypeDisplayNameWithIcon() {
        return switch (attractiveTypeCode) {
            case 1 -> "<i class=\"fa-solid fa-person-rays\"></i>";
            case 2 -> "<i class=\"fa-regular fa-face-smile\"></i>";
            default -> "<i class=\"fa-solid fa-people-roof\"></i>";
        } + "&nbsp;" + getAttractiveTypeDisplayName();
    }

    public String getJdenticon() {
        return Ut.hash.sha256(fromInstaMember.getId() + "_likes_" + toInstaMember.getId());
    }

    public RsData update(int attractiveTypeCode) {
        if (this.attractiveTypeCode == attractiveTypeCode){
            return RsData.of("F-1", "이미 설정되었습니다.");
        }

        this.attractiveTypeCode = attractiveTypeCode;
        this.modifyUnlockDate = AppConfig.genLikeablePersonModifyUnlockDate();

        return RsData.of("S-1", "성공");
    }

    public boolean isModifyUnlocked() {
        return modifyUnlockDate.isBefore(LocalDateTime.now());
    }

    // 초 단위에서 올림 해주세요.
    public String getModifyUnlockDateRemainStrHuman() {
        return "5분";
    }
}
