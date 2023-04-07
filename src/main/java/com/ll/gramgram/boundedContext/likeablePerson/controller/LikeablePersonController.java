package com.ll.gramgram.boundedContext.likeablePerson.controller;

import com.ll.gramgram.base.rq.Rq;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/likeablePerson")
@RequiredArgsConstructor
@Slf4j
public class LikeablePersonController {
    private final Rq rq;
    private final LikeablePersonService likeablePersonService;

    @GetMapping("/add")
    public String showAdd() {
        return "usr/likeablePerson/add";
    }

    @AllArgsConstructor
    @Getter
    public static class AddForm {
        private final String username;
        private final int attractiveTypeCode;
    }

    @PostMapping("/add")
    public String add(@Valid AddForm addForm) {
        RsData<LikeablePerson> createRsData = likeablePersonService.like(rq.getMember(), addForm.getUsername(), addForm.getAttractiveTypeCode());

        if (createRsData.isFail()) {
            return rq.historyBack(createRsData);
        }

        return rq.redirectWithMsg("/likeablePerson/list", createRsData);
    }

    @GetMapping("/list")
    public String showList(Model model) {
        InstaMember instaMember = rq.getMember().getInstaMember();

        // 인스타인증을 했는지 체크
        if (instaMember != null) {
            List<LikeablePerson> likeablePeople = likeablePersonService.findByFromInstaMemberId(instaMember.getId());
            model.addAttribute("likeablePeople", likeablePeople);
        }

        return "usr/likeablePerson/list";
    }

//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/delete/{likeableId}")
//    public String deleteLikeablePerson(@PathVariable Long likeableId){
//        //TODO: likeablePerson 아이디를 통해 삭제하며, InstaMemberId가 일치해야만 삭제 가능
//        log.info("likeablePerson 삭제 시작");
//        RsData<LikeablePerson> removeRsData = likeablePersonService.deleteLikeablePerson(likeableId, rq.getMember().getInstaMember(), rq.getMember().getInstaMember().getId());
//        if (removeRsData.isFail()){
//            return rq.historyBack(removeRsData);
//        }
//
//        return rq.redirectWithMsg("/likeablePerson/list", removeRsData);
//    }

    /**
     * V2
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{likeableId}")
    public String deleteLikeablePersonV2(@PathVariable Long likeableId){
        //TODO: likeablePerson 아이디를 통해 삭제하며, InstaMemberId가 일치해야만 삭제 가능
        log.info("likeablePerson 삭제 시작");

        LikeablePerson likeablePerson = likeablePersonService.findById(likeableId).orElse(null);

        RsData canActorDeleteRsData = likeablePersonService.canActorDelete(rq.getMember(), likeablePerson);

        if (canActorDeleteRsData.isFail()) return rq.historyBack(canActorDeleteRsData);

        RsData deleteRsData = likeablePersonService.deleteLikeablePersonV2(likeablePerson);

        if (deleteRsData.isFail()){
            return rq.historyBack(deleteRsData);
        }

        return rq.redirectWithMsg("/likeablePerson/list", deleteRsData);
    }
}
