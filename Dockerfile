FROM alpine:latest
LABEL authors="offz"

RUN apk add gcompat

COPY build/native/nativeCompile/discord-role-picker /
ENTRYPOINT ["/discord-role-picker"]
