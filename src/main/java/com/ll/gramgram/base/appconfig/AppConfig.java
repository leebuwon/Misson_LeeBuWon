package com.ll.gramgram.base.appconfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Getter
    private static long likeablePersonFromMax;

    @Value("${custom.likeablePerson.from.max}")
    public void setLikeablePersonFromMax(long likeablePersonFromMax){
        AppConfig.likeablePersonFromMax = likeablePersonFromMax;
    }
    @Getter
    private static long likeablePersonCoolTime;

    @Value("${custom.likeablePerson.modifyCoolTime}")
    public void setLikeablePersonModifyCoolTime(long likeablePersonCoolTime){
        AppConfig.likeablePersonCoolTime = likeablePersonCoolTime;
    }
}
