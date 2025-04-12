# 가이드
## 설치
### minikube
- minikube
    - 로컬 개발 환경에서 k8s 클러스터를 간단하게 실행할 수 있게 해주는 도구

```bash
brew install minikube
```

- minikube start
```bash
minikube start --kubernetes-version=v1.23.1
```

-minikube ip
Minikube 클러스터의 가상 머신 또는 컨테이너 IP의 주소
```bash
minikube ip
```


### k8s 실행
```bash
kubectl apply -f wordpress-k8s.yml
```

### k8s 정보 보기
```bash
kubectl get all
```

```bash
(base) yjlee@Yuje-Lee-MacBook index % kubectl get all
NAME                                  READY   STATUS    RESTARTS   AGE
pod/wordpress-74757b6ff-m4fck         1/1     Running   0          8m9s
pod/wordpress-mysql-5447bfc5b-s7kp7   1/1     Running   0          8m9s

NAME                      TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
service/kubernetes        ClusterIP   10.96.0.1        <none>        443/TCP        13m
service/wordpress         NodePort    10.102.244.199   <none>        80:30797/TCP   8m9s
service/wordpress-mysql   ClusterIP   10.110.71.72     <none>        3306/TCP       8m9s

NAME                              READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/wordpress         1/1     1            1           8m9s
deployment.apps/wordpress-mysql   1/1     1            1           8m9s

NAME                                        DESIRED   CURRENT   READY   AGE
replicaset.apps/wordpress-74757b6ff         1         1         1       8m9s
replicaset.apps/wordpress-mysql-5447bfc5b   1         1         1       8m9s
```

- service/wordpress PORT: 30797
- minikube ip: 192.168.49.2

=> http://192.168.49.2:30797에 접속하면 워드프레스 화면 조회 가능.

