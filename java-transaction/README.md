# Java-transaction
트랜잭션 관리를 위한 java 코드 예제
## 트랜잭션 격리 수준에 따라 발생 가능한 부정합 문제
| Isolation Level  | Dirty Read | Non-Repeatable Read | Phantom Read      |
| ---------------- | ---------- | ------------------- | ----------------- |
| READ UNCOMMITTED | 발생         | 발생                  | 발생                |
| READ COMMITTED   | 없음         | 발생                  | 발생                |
| REPEATABLE READ  | 없음         | 없음                  | 발생  |
| SERIALIZABLE     | 없음         | 없음                  | 없음                |

## 부정합 문제 발생 시나리오
### 1. Dirty Read
