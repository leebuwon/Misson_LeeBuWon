## 체크리스트

- [x] likeablePerson (필수미션)
  - [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현
- [ ] 배포 (필수미션)
  - [ ] 네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용
- [x] likeablePerson (선택미션)
  - [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현
  - [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능

---

## 4주차 미션 요약

### [해결 방법]

### 필수미션

- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현 (필수미션)
  - 1, @RequestParam(name = "gender", defaultValue = "") String gender 통해 option에서 gender가 W / M 인지 받아온다.
  - 2, if (gender.equals("W") || gender.equals("M")) 을 통해 참이라면 likeablePersonService.filterGender(likeablePeople, gender) 로 service에 들어간다.
  - 3, likeablePeople.stream()의 filter를 통해 들어온 gender랑 getFromInstaMember().getGender()가 같은것만 가져온다.

### 선택미션

- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현 (선택미션)
  - 1, @RequestParam(name = "attractiveTypeCode", defaultValue = "") String attractiveTypeCode 통해 option에서 attractiveTypeCode가 1 / 2 / 3 인지 받아온다.
  - 2,  if (!attractiveTypeCode.isEmpty()) 을 통해 Empty상태가 아니라면 likeablePersonService.filterAttractiveTypeCode(likeablePeople, attractiveTypeCode) 로 service에 들어간다.
  - 3, likeablePerson에 attractiveTypeCode는 int형으로 되있으니 @RequestParam을 통해 String으로 받아온 것을 Integer.parseInt를 통해 int형으로 변환해준다.
  - 4, filter(p -> p.getAttractiveTypeCode() == toAttractiveTypeCode)를 통해 알맞은 값을 가지고 온다.

- [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능 (선택미션)


### [수정해야할 부분]
- [x] 수정완료 ( .thenComparing(Comparator.comparing(BaseEntity::getCreateDate).reversed())) )
- 현재 선택미션2번의 6번이 글이 최신순으로 나오지 않는다. likeablePeople.stream()
  .sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode)
  .thenComparing(BaseEntity::getCreateDate)) 에서 reverse()를 해놓으면 AttractiveTypeCode까지 정렬이 잘못되기 때문에 아직 해결하지 못하였다.

### [리팩토링]
- [ ] 