# 레거시 코드 리팩토링
---
## 리팩토링 기간
> 기간 2024.09.26 ~ 2024.10.11

# 🚀 LV3-12 AWS 마스터
### S3 정책

![image](https://github.com/user-attachments/assets/6af4f1f3-ce6a-4b81-910a-a87604e40c01)


# 🚀 LV3-13 대용량 데이터 조회
## 🍰 개선 방법
1. JPA 메서드 사용
2. 출력 필드를 최소화 ( JPA 메서드 )
3. JPQL 로 전환
4. INDEX 적용 ( JPQL )

## 🍰 결과
- 출력필드를 3개에서 1개로 줄게한 시도에서는 눈에 띄는 변화가 느껴지지 않음.
- JPQL 전환 시 약 60 ms 조회 시간이 단축됨.
- INDEX 적용 시 11 ms 라는 엄청난 조회시간이 나옴.



| 개선 방법 | JPA 메서드 사용 | 출력 필드를 최소화 | JPQL 로 전환 | JPQL + INDEX 적용 |
|--------|--------|--------|--------|--------|
| (ms)  | 537 ms  | 550 ms  | 479 ms  | 11 ms  |



### 기존 방식 조회 결과 ( JPA 메서드 ) : 537 ms

![image](https://github.com/user-attachments/assets/29990a6f-44ca-4a30-923b-5c3498db10f7)

JPA 메서드를 활용한 기존 방식 그대로 속도를 기록함.


### 1. 출력 필드를 최소화 : 550 ms

![image](https://github.com/user-attachments/assets/2302a325-68c8-4970-a66d-64e5612d30e2)

출력되는 필드를 최소화시키는 방법을 시도해보았으나, 해당 케이스에서는 유의미한 변화가 보이지 않음.



### 2. JQPL 으로 전환 : 479 ms

![image](https://github.com/user-attachments/assets/79088c69-e093-4793-a3b4-ea1d80233276)

닉네임 조회 기능을 수행하는 JPA 메서드 대신 JPQL 로 전환 후 최적화 시켰더니 약 60 ms 조회 속도가 줄어드는 효과를 보임.



### 3. JQPL 전환 + INDEX 적용 : 11 ms

![image](https://github.com/user-attachments/assets/3e37b869-277c-4714-94cd-1274334ffdc7)

JPQL 전환 후 nickname 에 Index 가 적용된 방식으로 조회했더니 11 ms 라는 믿기 힘든 조회 속도 단축 효과가 보임.
