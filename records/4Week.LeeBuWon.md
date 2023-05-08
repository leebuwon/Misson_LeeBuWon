## 체크리스트

- [x] likeablePerson (필수미션)
  - [x] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현
- [ ] 배포 (필수미션)
  - [ ] 네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용
- [ ] likeablePerson (선택미션)
  - [ ] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현
  - [ ] 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능

---

## 4주차 미션 요약

### [해결 방법]

- [x] likeablePerson (필수미션)
  - 1, @RequestParam(name = "gender", defaultValue = "") String gender 통해 option에서 gender가 W / M 인지 받아온다.
  - 2, if (gender.equals("W") || gender.equals("M")) 을 통해 참이라면 likeablePersonService.filterGender(likeablePeople, gender); 로 들어간다.
  - 3, likeablePeople.stream()의 filter를 통해 들어온 gender랑 getFromInstaMember().getGender()가 같은것만 가져온다.
 
### 필수미션

- 1, 

### 선택미션

- 1,

### [리팩토링]
- [ ] 