# 베이스 이미지 자바11
FROM openjdk:11
LABEL authors="OSKA"

# 작업 디렉토리 설정. 어플리케이션 JAR 파일을 여기에 복사
WORKDIR /oauth

# 호스트 머신의 현재 디렉토리에 있는 JAR 파일을 컨테이너로 복사
COPY build/libs/oauth-app-1.0.jar /oauth/oauth-app-1.0.jar

# 어플리케이션의 포트를 노출(수신대기)
EXPOSE 7979

# 컨테이너가 시작될 때 실행할 명령 지정. 아래는 JAR 파일 실행
CMD ["java", "-jar", "oauth-app-1.0.jar"]