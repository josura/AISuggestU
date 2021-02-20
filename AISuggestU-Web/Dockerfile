FROM golang

RUN mkdir -p /app/
ADD . /go/src/aisuggestu/

ENV GO111MODULE=on

RUN cd /go/src/aisuggestu/ && go build -o aisuggestu

WORKDIR /go/src/aisuggestu/

ENTRYPOINT [ "/go/src/aisuggestu/aisuggestu" ]