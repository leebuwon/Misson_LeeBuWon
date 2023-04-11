## 체크리스트

- [x] likeablePerson
    - [x] 중복된 좋아요에 대한 add 예외처리(attractiveTypeCode는 신경쓰지 않았음)
    - [x] 11명 이상의 사람이 좋아요 됐을 경우 예외처리(size()로 해결 -> 추후 더 좋은방법이 있다면 리팩토링)
    - [x] 실패한다면 rq.historyBack(createRsData) 통하여 redirectWithMsg 완료
    - [x] 성공 시 RsData 이용하여 성공 메시지 출력
    - [x] 케이스 4 가 발생했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우에는 성공으로 처리한다.
    - [ ] 예외처리인 중복된 좋아요가 실행되지 않게 해야함

---
## 1주차 미션 요약

### [접근 방법]

### case 4
- 1, 우선 fromInstaMember멤버의 username과 toInstaMember의 username을 repository에서 조회한다.
- 2, repository에서 가져온 데이터를 isPresent() 이용하여 이미 존재한다면 RsData를 이용하여 fail을 던저준다.

### case 5
- 1, 강사님이 layout에 주신 source 코드를 활용하여 접근하였습니다.
- 2, member.getInstaMember().getFromLikeablePeople().size() 를 통하여 좋아요 한 숫자를 가져왔습니다.
- 3, 좋아요한 숫자가 11이상이면 예외처리가 되도록 member.getInstaMember().getFromLikeablePeople().size() >= 11을 실행하게하였습니다.

### case 6
- 1, case 4를 기반으로 수정하여 isPresent()에서 존재한다면 existingLikeablePerson.get() 이용하여 정보를 가지고 온다.
- 2, 가져온 정보를 updateLikeablePerson.setAttractiveTypeCode(attractiveTypeCode)을 이용하여 수정해준다.
- 3, 마지막으로 repository의 save()를 이용하여 저장해준다.

### [리팩토링]
- [ ] setter를 이용한 의존성 주입방법은 단점이 많기 떄문에 getter만 이용하여 할 수 있도록 수정해준다.
- [ ] 현재는 case 6을 진행하면서 case 4를 사용하지 않게되었는데 AttractiveTypeCode를 이용하여 case 4를 활성화 해준다. 즉 같은 유형의 좋아요가 들어왔을 경우 case 4가 실행되게 해준다.