FROM golang

WORKDIR /go/src/app

RUN go get -u gopkg.in/confluentinc/confluent-kafka-go.v1/kafka

ADD starredRepo.go /go/src/app
RUN go build starredRepo.go

ENTRYPOINT [ "/go/src/app/starredRepo", "herbrant" ]
