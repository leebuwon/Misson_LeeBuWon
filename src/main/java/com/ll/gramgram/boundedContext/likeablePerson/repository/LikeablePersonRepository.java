package com.ll.gramgram.boundedContext.likeablePerson.repository;

import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeablePersonRepository extends JpaRepository<LikeablePerson, Long>, LikeablePersonRepositoryCustom  {
    List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId);

    Optional<LikeablePerson> findByFromInstaMemberAndToInstaMember(InstaMember fromInstaMember, InstaMember toInstaMember);

    Optional<LikeablePerson> findByFromInstaMemberAndToInstaMemberAndAttractiveTypeCode(InstaMember fromInstaMember, InstaMember toInstaMember, int AttractiveTypeCode);

    List<LikeablePerson> findByToInstaMember_username(String instaUsername);

    LikeablePerson findByFromInstaMemberIdAndToInstaMember_username(long id, String instaUsername);

    Optional<LikeablePerson> findByFromInstaMember_usernameAndToInstaMember_username(String fromInstaMemberUsername, String toInstaMemberUsername);
}
