## 체크리스트

- [x] likeablePerson
    - [x] 중복된 좋아요에 대한 add 예외처리(attractiveTypeCode는 신경쓰지 않았음)
    - [x] 11명 이상의 사람이 좋아요 됐을 경우 예외처리(size()로 해결 -> 추후 리팩토링)
    - [x] 실패한다면 rq.historyBack(createRsData) 통하여 redirectWithMsg 완료
    - [x] 성공 시 RsData 이용하여 성공 메시지 출력

---
## 1주차 미션 요약

### [접근 방법]

- 1, 우선 fromInstaMember멤버의 username과 toInstaMember의 username을 repository에서 조회한다.
- 2, repository에서 가져온 데이터를 isPresent() 이용하여 이미 존재한다면 RsData를 이용하여 fail을 던저준다.


### [특이사항]
