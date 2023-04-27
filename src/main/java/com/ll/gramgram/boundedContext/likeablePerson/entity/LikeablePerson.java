package com.ll.gramgram.boundedContext.likeablePerson.entity;

import com.ll.gramgram.base.BaseEntity;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Getter
public class LikeablePerson extends BaseEntity {

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

    public boolean update(int attractiveTypeCode) {
        if (this.attractiveTypeCode == attractiveTypeCode){
            return false;
        }

        toInstaMember.decreaseLikesCount(fromInstaMember.getGender(), this.attractiveTypeCode);
        toInstaMember.increaseLikesCount(fromInstaMember.getGender(), attractiveTypeCode);

        this.attractiveTypeCode = attractiveTypeCode;
        return true;
    }
}
