FROM gcr.io/distroless/java21
LABEL maintainer="Team Bidrag" \
      email="bidrag@nav.no"

COPY --from=busybox /bin/sh /bin/sh
COPY --from=busybox /bin/printenv /bin/printenv

WORKDIR /app

COPY ./target/app.jar app.jar

EXPOSE 8080
ENV LANG=nb_NO.UTF-8 LANGUAGE='nb_NO:nb' LC_ALL=nb_NO.UTF-8 TZ="Europe/Oslo"
ENV SPRING_PROFILES_ACTIVE=nais
ENTRYPOINT ["/bin/sh", "-c", "java $JAVA_PROXY_OPTIONS -jar app.jar"]